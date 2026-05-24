package dev.nandobez.macc.cmd;

import picocli.CommandLine.*;
import java.nio.file.*;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static dev.nandobez.macc.cmd.Tui.*;
import static java.nio.file.StandardWatchEventKinds.*;

@Command(name = "watch",
    description = "Watch src/main/java and re-run `macc codegen` on every change to a @Page/@Model class.")
public class WatchCmd implements Callable<Integer> {

    @Option(names = {"-d", "--dir"}, defaultValue = ".") Path projectRoot;
    @Option(names = "--frontend", defaultValue = "src/main/frontend") Path frontend;

    public Integer call() throws Exception {
        Path root = projectRoot.toAbsolutePath();
        Path src  = root.resolve("src/main/java");
        if (!Files.exists(src)) { error("src/main/java not found"); return 2; }
        banner("macc watch", src.toString());
        info("press Ctrl-C to stop · also compiles on change");

        WatchService ws = FileSystems.getDefault().newWatchService();
        try (Stream<Path> w = Files.walk(src)) {
            w.filter(Files::isDirectory).forEach(p -> {
                try { p.register(ws, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE); } catch (Exception ignored) {}
            });
        }

        long lastTrigger = 0;
        while (true) {
            WatchKey key = ws.take();
            for (var ev : key.pollEvents()) {
                String name = ev.context().toString();
                if (!name.endsWith(".java")) continue;
                long now = System.currentTimeMillis();
                if (now - lastTrigger < 400) continue;        // debounce
                lastTrigger = now;
                System.out.println();
                System.out.println("  " + CYN + "↻" + R + " " + DIM + name + R + " changed");
                long t0 = System.currentTimeMillis();
                int rc = new ProcessBuilder("mvn", "-q", "compile").directory(root.toFile()).inheritIO().start().waitFor();
                if (rc != 0) { error("mvn compile failed"); continue; }
                rc = new ProcessBuilder("java", "-jar", jarPath(), "codegen", "--dir", root.toString(),
                                        "--frontend", frontend.toString())
                    .directory(root.toFile()).inheritIO().start().waitFor();
                long ms = System.currentTimeMillis() - t0;
                if (rc == 0) System.out.println("    " + GRN + "✓" + R + " regenerated" + DIM + "  (" + ms + "ms)" + R);
            }
            key.reset();
        }
    }

    private static String jarPath() {
        return WatchCmd.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }
}
