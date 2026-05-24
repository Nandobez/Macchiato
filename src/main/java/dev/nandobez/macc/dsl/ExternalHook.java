package dev.nandobez.macc.dsl;

/** Reference to an external React hook (useFoo). Returns a Var<?> placeholder when called. */
public final class ExternalHook<T> {
    public final String importPath;
    public final String exportName;

    ExternalHook(String importPath, String exportName) {
        this.importPath = importPath;
        this.exportName = exportName;
    }

    public Var<T> call() { return new Var<>(); }
}
