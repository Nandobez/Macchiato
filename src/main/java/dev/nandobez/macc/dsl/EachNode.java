package dev.nandobez.macc.dsl;

/** {list.map(item => body)} — list iteration. */
public final class EachNode implements Element {
    public final String listExpr;   // TS expression naming the source list (e.g. "users.data")
    public final String itemName;   // lambda parameter name (e.g. "user")
    public final Element body;

    public EachNode(String listExpr, String itemName, Element body) {
        this.listExpr = listExpr;
        this.itemName = itemName;
        this.body = body;
    }
}
