package dev.nandobez.macc.dsl;

/** Raw TS expression. Emitter wraps in {} when used in JSX. */
public final class ExprNode implements Element {
    public final String code;
    public ExprNode(String code) { this.code = code; }
}
