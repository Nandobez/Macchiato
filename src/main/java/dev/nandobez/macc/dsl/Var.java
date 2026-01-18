package dev.nandobez.macc.dsl;

import java.util.List;

/** Reactive value wrapper for @Fetch/@State<Var<T>> fields. Methods compile-only — codegen rewrites. */
public class Var<T> {

    /** Marker for codegen — these methods produce TS hook accesses, never called at runtime. */
    public boolean isLoading()  { return false; }
    public boolean hasError()   { return false; }
    public boolean isEmpty()    { return false; }
    public boolean isPresent()  { return false; }
    public boolean isAbsent()   { return false; }
    public boolean isFilled()   { return false; }
    public boolean isReady()    { return false; }
    public String  error()      { return null; }
    public T       value()      { return null; }
    @SuppressWarnings("unchecked")
    public T       data()       { return (T) this; }
}
