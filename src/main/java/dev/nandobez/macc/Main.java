package dev.nandobez.macc;

import dev.nandobez.macc.cmd.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import static dev.nandobez.macc.cmd.Tui.*;

@Command(
    name = "macc",
    mixinStandardHelpOptions = true,
    version = "macc 0.1.0",
    description = "Macc — write React in Java. DSL + codegen.",
    subcommands = { NewCmd.class, GenerateCmd.class, CodegenCmd.class,
        InstallCmd.class, DevCmd.class, ServeCmd.class,
        DoctorCmd.class, DepsCmd.class }
)
public class Main implements Runnable {

    public void run() { printHelp(); }

    public static void main(String[] args) {
        if (args.length == 0) { printHelp(); System.exit(0); }
        System.out.println();
        int rc = new CommandLine(new Main()).execute(args);
        System.out.println();
        System.exit(rc);
    }

    private static void printHelp() {
        System.out.println();
        System.out.println(BLD + "macc " + R + DIM + "0.1.0" + R + " — write React in Java");
        System.out.println();
        System.out.println("  " + DIM + "USAGE" + R);
        System.out.println();
        System.out.println("    macc new <frontend-dir>          scaffold Vite + React + Tailwind");
        System.out.println("    macc g page <Name>               generate a @Page class");
        System.out.println("    macc g component <Name>          generate a @Component class");
        System.out.println("    macc g model <Name>              generate a @Model record");
        System.out.println("    macc codegen                     scan + emit .tsx + types + routes");
        System.out.println("    macc install                     mvn compile + codegen + npm install + vite build");
        System.out.println("    macc dev                         codegen + vite dev server (hot reload)");
        System.out.println("    macc serve                       backend (xpresso s) + frontend (vite) together");
        System.out.println("    macc doctor                      delegate to " + DIM + "jdp doctor" + R);
        System.out.println("    macc deps                        delegate to " + DIM + "jdp list" + R);
        System.out.println();
    }
}
