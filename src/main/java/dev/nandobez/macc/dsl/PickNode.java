package dev.nandobez.macc.dsl;

/** pick(cond, a, b) — ternary. cond may be a runtime boolean OR a string expression. */
public final class PickNode implements Element {
    public final Object cond;      // Boolean OR ExprNode OR string
    public final Object ifTrue;
    public final Object ifFalse;
    public final boolean isStaticCond;

    public PickNode(Object cond, Object ifTrue, Object ifFalse, boolean isStaticCond) {
        this.cond = cond;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
        this.isStaticCond = isStaticCond;
    }
}
