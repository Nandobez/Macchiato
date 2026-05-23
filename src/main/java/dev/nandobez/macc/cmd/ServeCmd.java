package dev.nandobez.macc.cmd;

import picocli.CommandLine.*;
import java.nio.file.*;
import java.util.concurrent.Callable;

import static dev.nandobez.macc.cmd.Tui.*;

@Command(name = "serve",
    description = "Run backend (xpresso s) + frontend (vite dev) together. Backend on :8080, frontend on :5173 with proxy /api → :8080.")
public class ServeCmd implements Callable<Integer> {

    @Option(names = {"-d", "--dir"}, defaultValue = ".")
    Path projectRoot;

    @Option(names = "--frontend", defaultValue = "src/main/frontend")
    Path frontend;

    public Integer call() throws Exception {
        Path root = projectRoot.toAbsolutePath();
        Path fe   = root.resolve(frontend);
        banner("macc serve", "backend + frontend in parallel");

        // 1. codegen first (cheap, idempotent)
        info("[1/3] running codegen…");
        run(root, "java", "-jar", jarPath(), "codegen");

        // 2. start backend in background via xpresso
        String xpressoJar = locateXpressoJar();
        if (xpressoJar == null) {
            error("xpresso not installed. Install from https://github.com/Nandobez/Xpresso");
            return 2;
        }
        info("[2/3] starting backend (xpresso s) on :8080…");
        Process backend = new ProcessBuilder("java", "-jar", xpressoJar, "s", "--skip-frontend")
            .directory(root.toFile()).inheritIO().start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (backend.isAlive()) backend.destroy();
        }));

        // 3. start vite dev in foreground
        info("[3/3] starting vite on :5173 (proxies /api → :8080)…");
        int rc = new ProcessBuilder("npm", "run", "dev")
            .directory(fe.toFile()).inheritIO().start().waitFor();

        if (backend.isAlive()) backend.destroy();
        return rc;
    }

    private static String jarPath() {
        return ServeCmd.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    static String locateXpressoJar() {
        for (String c : new String[]{
            System.getenv("XPRESSO_HOME") == null ? null : System.getenv("XPRESSO_HOME") + "/xpresso.jar",
            System.getProperty("user.home") + "/.local/share/xpresso/xpresso.jar",
            "/usr/local/share/xpresso/xpresso.jar",
            "/tmp/xpresso/target/xpresso.jar",
        }) if (c != null && Files.exists(Path.of(c))) return c;
        return null;
    }

    private static int run(Path dir, String... cmd) throws Exception {
        return new ProcessBuilder(cmd).directory(dir.toFile()).inheritIO().start().waitFor();
    }
}
