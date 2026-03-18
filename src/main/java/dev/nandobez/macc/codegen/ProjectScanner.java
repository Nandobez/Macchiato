package dev.nandobez.macc.codegen;

import dev.nandobez.macc.dsl.annotations.*;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/** Loads compiled user classes from target/classes + resolves the deps classpath. */
public class ProjectScanner {

    public record Scan(URLClassLoader loader, List<Class<?>> pages, List<Class<?>> models) {}

    public static Scan scan(Path projectRoot) throws Exception {
        Path classesDir = projectRoot.resolve("target/classes");
        if (!Files.exists(classesDir)) {
            throw new IllegalStateException("target/classes not found — run `mvn compile` first.");
        }

        // Build classpath: project classes + macc-runtime jar (this CLI's own jar) + deps from build-classpath.
        List<URL> urls = new ArrayList<>();
        urls.add(classesDir.toUri().toURL());

        Path cpFile = projectRoot.resolve("target/macc-classpath.txt");
        if (!Files.exists(cpFile)) {
            new ProcessBuilder("mvn", "-q", "-DincludeScope=runtime",
                "dependency:build-classpath", "-Dmdep.outputFile=" + cpFile.toAbsolutePath())
                .directory(projectRoot.toFile()).inheritIO().start().waitFor();
        }
        if (Files.exists(cpFile)) {
            String[] entries = Files.readString(cpFile).trim().split(File.pathSeparator);
            for (String e : entries) if (!e.isBlank()) urls.add(new File(e).toURI().toURL());
        }

        // include this jar so Macc DSL types resolve.
        URL self = ProjectScanner.class.getProtectionDomain().getCodeSource().getLocation();
        urls.add(self);

        URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), ProjectScanner.class.getClassLoader());

        List<Class<?>> pages = new ArrayList<>();
        List<Class<?>> models = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(classesDir)) {
            for (Path p : (Iterable<Path>) walk.filter(x -> x.toString().endsWith(".class"))::iterator) {
                String rel = classesDir.relativize(p).toString().replace(File.separatorChar, '.');
                String cn = rel.substring(0, rel.length() - 6);
                try {
                    Class<?> c = Class.forName(cn, false, loader);
                    if (c.isAnnotationPresent(Page.class)) pages.add(c);
                    if (c.isAnnotationPresent(Model.class)) models.add(c);
                } catch (Throwable ignore) {}
            }
        }
        return new Scan(loader, pages, models);
    }
}
