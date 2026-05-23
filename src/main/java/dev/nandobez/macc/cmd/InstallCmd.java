package dev.nandobez.macc.cmd;

import picocli.CommandLine.*;
import java.nio.file.*;
import java.util.concurrent.Callable;

import static dev.nandobez.macc.cmd.Tui.*;

@Command(name = "install",
    description = "One-shot: npm install + macc codegen + vite build (output goes to resources/static).")
public class InstallCmd implements Callable<Integer> {

    @Option(names = {"-d", "--dir"}, defaultValue = ".",
        description = "Project root (where pom.xml lives).")
    Path projectRoot;

    @Option(names = "--frontend", defaultValue = "src/main/frontend",
        description = "Frontend directory.")
    Path frontend;

    @Option(names = "--skip-compile", description = "Skip `mvn compile` (use when classes are already built).")
    boolean skipCompile;

    public Integer call() throws Exception {
        Path root = projectRoot.toAbsolutePath();
        Path feDir = root.resolve(frontend);

        banner("macc install", root.toString());

        if (!Files.exists(root.resolve("pom.xml"))) {
            error("no pom.xml at " + root + " — run inside a Maven project.");
            return 2;
        }
        if (!Files.exists(feDir)) {
            error("frontend dir not found: " + feDir + " — run `macc new " + frontend + "` first.");
            return 2;
        }
        if (which("node") == null || which("npm") == null) {
            error("node + npm are required. Install Node.js 18+ first.");
            return 2;
        }

        // 1. compile java so @Page/@Model classes exist
        if (!skipCompile) {
            info("[1/4] mvn -q compile  (so @Page classes exist)");
            int rc = exec(root, "mvn", "-q", "compile");
            if (rc != 0) { error("mvn compile failed."); return rc; }
        }

        // 2. macc codegen — invoke this same jar
        info("[2/4] macc codegen  (Java → TSX)");
        var ccmd = new java.util.ArrayList<String>();
        ccmd.add("java"); ccmd.add("-jar"); ccmd.add(jarPath());
        ccmd.add("codegen"); ccmd.add("--dir"); ccmd.add(root.toString());
        ccmd.add("--frontend"); ccmd.add(frontend.toString());
        int rc = new ProcessBuilder(ccmd).directory(root.toFile()).inheritIO().start().waitFor();
        if (rc != 0) { error("codegen failed."); return rc; }

        // 3. npm install
        if (!Files.exists(feDir.resolve("node_modules"))) {
            info("[3/4] npm install  (first time only)");
            rc = exec(feDir, "npm", "install");
            if (rc != 0) { error("npm install failed."); return rc; }
        } else {
            info("[3/4] node_modules present, skipping npm install");
        }

        // 4. vite build → src/main/resources/static
        info("[4/4] npm run build  (vite → resources/static)");
        rc = exec(feDir, "npm", "run", "build");
        if (rc != 0) { error("vite build failed."); return rc; }

        System.out.println();
        System.out.println("  " + GRN + "✓" + R + " ready. start the server with " + BLD + "mvn spring-boot:run" + R);
        return 0;
    }

    private static String jarPath() {
        return InstallCmd.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    private static int exec(Path dir, String... args) throws Exception {
        return new ProcessBuilder(args).directory(dir.toFile()).inheritIO().start().waitFor();
    }

    private static String which(String cmd) {
        for (String dir : System.getenv("PATH").split(":")) {
            Path p = Path.of(dir, cmd);
            if (Files.isExecutable(p)) return p.toString();
        }
        return null;
    }
}
