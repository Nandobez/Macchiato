package dev.nandobez.macc.cmd;

import picocli.CommandLine.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.*;
import java.util.stream.Stream;

import static dev.nandobez.macc.cmd.Tui.*;

@Command(name = "add",
    description = "Add a UI component (shadcn-style). Downloads the TSX + generates a Java wrapper class so you can use it from @Page.")
public class UiAddCmd implements Callable<Integer> {

    @Parameters(arity = "1..*", description = "Component names (button, card, dialog, …) or registry URLs.")
    List<String> components;

    @Option(names = "--frontend", defaultValue = "src/main/frontend") Path frontend;
    @Option(names = "--package", defaultValue = "ui.external",
        description = "Java sub-package under the project base package for wrappers.")
    String pkgSuffix;
    @Option(names = "--installer", defaultValue = "auto",
        description = "Which CLI to use: auto | shadcn | none. 'none' skips download and only generates wrappers from existing TSX.")
    String installer;

    private static final Pattern EXPORT_RE = Pattern.compile(
        "export\\s+(?:default\\s+)?(?:function|const|class)\\s+([A-Z][A-Za-z0-9_]*)");
    private static final Pattern HOOK_RE   = Pattern.compile(
        "export\\s+(?:function|const)\\s+(use[A-Z][A-Za-z0-9_]*)");

    public Integer call() throws Exception {
        Path projectRoot = Paths.get(".").toAbsolutePath();
        Path feDir = projectRoot.resolve(frontend);
        Path uiDir = feDir.resolve("src/components/ui");
        Files.createDirectories(uiDir);

        for (String comp : components) {
            banner("macc add " + comp, uiDir.toString());

            // 1. fetch the TSX (via shadcn CLI when available, else fail with a hint)
            Path tsx = fetch(feDir, uiDir, comp);
            if (tsx == null) {
                error("could not obtain TSX for '" + comp + "'. Either install shadcn (npx shadcn@latest init) or pass an existing component name.");
                continue;
            }

            // 2. parse exports
            String body = Files.readString(tsx);
            var components = new LinkedHashSet<String>();
            var hooks      = new LinkedHashSet<String>();
            for (var m = EXPORT_RE.matcher(body); m.find();) components.add(m.group(1));
            for (var m = HOOK_RE.matcher(body);   m.find();) hooks.add(m.group(1));

            if (components.isEmpty() && hooks.isEmpty()) {
                info("no exports detected; component imported but no Java wrapper generated.");
                continue;
            }

            // 3. emit Java wrapper
            String basePackage = detectBasePackage(projectRoot);
            if (basePackage == null) {
                error("could not detect base package — Java wrapper skipped");
                continue;
            }
            String fullPkg = basePackage + "." + pkgSuffix;
            String className = pascal(comp);
            Path javaOut = projectRoot.resolve("src/main/java/" + fullPkg.replace('.', '/') + "/" + className + ".java");
            Files.createDirectories(javaOut.getParent());

            String wrapper = wrapperSource(fullPkg, className, "@/components/ui/" + comp, components, hooks);
            Files.writeString(javaOut, wrapper);
            System.out.println("    " + GRN + "wrote" + R + "   " + javaOut);
            System.out.println();
            info("use from @Page Java with:");
            System.out.println("    " + DIM + "import " + fullPkg + "." + className + ";" + R);
            for (String c : components)
                System.out.println("    " + DIM + className + "." + decap(stripPrefix(c, className)) + "" + R);
        }
        return 0;
    }

    private Path fetch(Path feDir, Path uiDir, String comp) throws Exception {
        Path local = uiDir.resolve(comp + ".tsx");
        if (Files.exists(local)) { info("already present: " + local); return local; }

        if ("none".equalsIgnoreCase(installer)) return null;
        if ("auto".equalsIgnoreCase(installer) || "shadcn".equalsIgnoreCase(installer)) {
            // shadcn requires components.json in the frontend dir. If missing, do a silent init.
            Path cfg = feDir.resolve("components.json");
            if (!Files.exists(cfg)) {
                info("no components.json — initializing shadcn (this writes config files in " + feDir + ")");
                int rc = new ProcessBuilder("npx", "-y", "shadcn@latest", "init", "-d", "-y")
                    .directory(feDir.toFile()).inheritIO().start().waitFor();
                if (rc != 0) { error("shadcn init failed"); return null; }
            }
            info("running: npx shadcn@latest add " + comp);
            int rc = new ProcessBuilder("npx", "-y", "shadcn@latest", "add", "-y", comp)
                .directory(feDir.toFile()).inheritIO().start().waitFor();
            if (rc != 0) { error("shadcn add failed"); return null; }
            if (Files.exists(local)) return local;
            // shadcn may put it elsewhere — search
            try (Stream<Path> s = Files.walk(uiDir)) {
                for (Path p : (Iterable<Path>) s.filter(x -> x.toString().endsWith(comp + ".tsx"))::iterator) return p;
            }
        }
        return null;
    }

    private static String wrapperSource(String pkg, String className, String importPath,
                                        Set<String> exports, Set<String> hooks) {
        var sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import dev.nandobez.macc.dsl.External;\n");
        sb.append("import dev.nandobez.macc.dsl.ExternalComponent;\n\n");
        sb.append("/**\n * Wrapper auto-generated by `macc add`. Source TSX lives at\n");
        sb.append(" * src/main/frontend").append(importPath.substring(1)).append(".tsx and is editable.\n */\n");
        sb.append("public final class ").append(className).append(" {\n");
        sb.append("    private static final String SRC = \"").append(importPath).append("\";\n\n");
        for (String e : exports) {
            String field = decap(stripPrefix(e, className));
            if (field.isEmpty()) field = "root";
            sb.append("    /** ").append(e).append(" — render as JSX child via .of()/.children()/.prop(). */\n");
            sb.append("    public static final ExternalComponent ").append(field)
              .append(" = External.from(SRC, \"").append(e).append("\");\n\n");
        }
        for (String h : hooks) {
            sb.append("    /** ").append(h).append(" — call via .call() to get a Var<?>. */\n");
            sb.append("    public static final dev.nandobez.macc.dsl.ExternalHook<?> ").append(h)
              .append(" = External.hook(SRC, \"").append(h).append("\");\n\n");
        }
        sb.append("    private ").append(className).append("() {}\n}\n");
        return sb.toString();
    }

    private static String detectBasePackage(Path root) throws Exception {
        Path java = root.resolve("src/main/java");
        if (!Files.exists(java)) return null;
        try (Stream<Path> s = Files.walk(java)) {
            for (Path p : (Iterable<Path>) s.filter(x -> x.toString().endsWith("Application.java"))::iterator) {
                return java.relativize(p.getParent()).toString().replace('/', '.');
            }
        }
        return null;
    }

    private static String pascal(String s) {
        StringBuilder sb = new StringBuilder();
        for (String p : s.split("[-_]")) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    private static String decap(String s) {
        if (s.isEmpty()) return s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private static String stripPrefix(String name, String prefix) {
        if (name.startsWith(prefix) && name.length() > prefix.length()) return name.substring(prefix.length());
        return name;
    }
}
