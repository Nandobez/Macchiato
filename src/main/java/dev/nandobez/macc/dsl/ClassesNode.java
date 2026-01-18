package dev.nandobez.macc.dsl;

/** classes("a","b", when(cond,"c")) → clsx("a","b", cond && "c"). */
public final class ClassesNode implements Element {
    public final Object[] parts;
    public ClassesNode(Object[] parts) { this.parts = parts; }
}
