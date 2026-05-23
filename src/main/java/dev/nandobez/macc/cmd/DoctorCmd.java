package dev.nandobez.macc.cmd;

import picocli.CommandLine.*;
import java.nio.file.*;
import java.util.concurrent.Callable;

import static dev.nandobez.macc.cmd.Tui.*;

@Command(name = "doctor",
    description = "Run jdp doctor on the host Maven project (CVE + outdated + incompat).")
public class DoctorCmd implements Callable<Integer> {

    @Option(names = "--fix")
    boolean fix;

    public Integer call() throws Exception {
        String jdpJar = locateJdp();
        if (jdpJar == null) {
            error("jdp not installed. Install from https://github.com/Nandobez/jdp");
            return 2;
        }
        banner("macc doctor", "delegating to jdp");
        var cmd = new java.util.ArrayList<String>();
        cmd.add("java"); cmd.add("-jar"); cmd.add(jdpJar); cmd.add("doctor");
        if (fix) cmd.add("--fix");
        return new ProcessBuilder(cmd).inheritIO().start().waitFor();
    }

    static String locateJdp() {
        for (String c : new String[]{
            System.getenv("JDP_HOME") == null ? null : System.getenv("JDP_HOME") + "/jdp.jar",
            System.getProperty("user.home") + "/.local/share/jdp/jdp.jar",
            "/usr/local/share/jdp/jdp.jar",
            "/tmp/jdp/target/jdp.jar",
        }) if (c != null && Files.exists(Path.of(c))) return c;
        return null;
    }
}
