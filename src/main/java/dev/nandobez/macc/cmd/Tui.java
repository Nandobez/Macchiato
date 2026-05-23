package dev.nandobez.macc.cmd;

public class Tui {
    public static final String R = "\u001B[0m";
    public static final String BLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String RED = "\u001B[31m";
    public static final String GRN = "\u001B[32m";
    public static final String YLW = "\u001B[33m";
    public static final String CYN = "\u001B[36m";

    public static void wrote(String path)  { System.out.println("    " + GRN + "wrote" + R + "   " + path); }
    public static void info(String s)      { System.out.println("    " + DIM + s + R); }
    public static void error(String s)     { System.out.println("    " + RED + "✗ " + R + s); }
    public static void banner(String title, String subtitle) {
        System.out.println();
        System.out.println("  " + BLD + title + R + DIM + "  " + subtitle + R);
        System.out.println();
    }
}
