package dev.nandobez.macc.dsl;

/** Factories for external React components and hooks. */
public final class External {
    private External() {}

    public static ExternalComponent from(String importPath, String exportName) {
        return new ExternalComponent(importPath, exportName);
    }

    public static <T> ExternalHook<T> hook(String importPath, String exportName) {
        return new ExternalHook<>(importPath, exportName);
    }
}
