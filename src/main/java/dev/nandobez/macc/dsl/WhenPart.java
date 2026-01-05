package dev.nandobez.macc.dsl;

/** when(cond, value) — used inside classes(...) to conditionally include a class string. */
public final class WhenPart {
    public final Object cond;
    public final String value;
    public WhenPart(Object cond, String value) { this.cond = cond; this.value = value; }
}
