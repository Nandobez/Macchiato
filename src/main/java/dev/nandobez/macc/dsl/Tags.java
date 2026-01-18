package dev.nandobez.macc.dsl;

import java.util.List;

/** Static factories for HTML elements. Import statically: `import static …dsl.Tags.*;` */
public final class Tags {

    private static Tag t(String name) { return new Tag(name); }

    // Structure
    public static Tag div()       { return t("div"); }
    public static Tag div(Element... kids)       { return new Tag("div").children(kids); }
    public static Tag span()      { return t("span"); }
    public static Tag span(String text)          { return new Tag("span").text(text); }
    public static Tag section()   { return t("section"); }
    public static Tag article()   { return t("article"); }
    public static Tag header()    { return t("header"); }
    public static Tag footer()    { return t("footer"); }
    public static Tag main_()     { return t("main"); }
    public static Tag nav()       { return t("nav"); }
    public static Tag aside()     { return t("aside"); }

    // Headings
    public static Tag h1()  { return t("h1"); }
    public static Tag h1(String text)             { return new Tag("h1").text(text); }
    public static Tag h2()  { return t("h2"); }
    public static Tag h2(String text)             { return new Tag("h2").text(text); }
    public static Tag h3()  { return t("h3"); }
    public static Tag h3(String text)             { return new Tag("h3").text(text); }
    public static Tag h4()  { return t("h4"); }
    public static Tag h5()  { return t("h5"); }
    public static Tag h6()  { return t("h6"); }

    // Text
    public static Tag p()                         { return t("p"); }
    public static Tag p(String text)              { return new Tag("p").text(text); }
    public static Tag a()                         { return t("a"); }
    public static Tag a(String href, String text) { return new Tag("a").href(href).text(text); }
    public static Tag strong()                    { return t("strong"); }
    public static Tag em()                        { return t("em"); }
    public static Tag code()                      { return t("code"); }
    public static Tag pre()                       { return t("pre"); }
    public static Tag small()                     { return t("small"); }
    public static Tag label()                     { return t("label"); }

    // Lists
    public static Tag ul()                        { return t("ul"); }
    public static Tag ul(Element... kids)         { return new Tag("ul").children(kids); }
    public static Tag ul(List<Element> kids)      { return new Tag("ul").children(kids); }
    public static Tag ol()                        { return t("ol"); }
    public static Tag ol(Element... kids)         { return new Tag("ol").children(kids); }
    public static Tag li()                        { return t("li"); }
    public static Tag li(String text)             { return new Tag("li").text(text); }
    public static Tag li(Object key, String text) { return new Tag("li").key(key).text(text); }
    public static Tag li(Object key, Element... kids) { return new Tag("li").key(key).children(kids); }
    public static Tag li(Element... kids)         { return new Tag("li").children(kids); }

    // Forms
    public static Tag form()                      { return t("form"); }
    public static Tag form(Element... kids)       { return new Tag("form").children(kids); }
    public static Tag input()                     { return t("input"); }
    public static Tag textarea()                  { return t("textarea"); }
    public static Tag select()                    { return t("select"); }
    public static Tag option()                    { return t("option"); }
    public static Tag option(String value, String text) { return new Tag("option").value(value).text(text); }
    public static Tag button()                    { return t("button"); }
    public static Tag button(String text)         { return new Tag("button").text(text); }
    public static Tag fieldset()                  { return t("fieldset"); }
    public static Tag legend()                    { return t("legend"); }

    // Tables
    public static Tag table()                     { return t("table"); }
    public static Tag thead(Element... kids)      { return new Tag("thead").children(kids); }
    public static Tag tbody(Element... kids)      { return new Tag("tbody").children(kids); }
    public static Tag tr(Element... kids)         { return new Tag("tr").children(kids); }
    public static Tag th(String text)             { return new Tag("th").text(text); }
    public static Tag td()                        { return t("td"); }
    public static Tag td(String text)             { return new Tag("td").text(text); }
    public static Tag td(Object value)            { return new Tag("td").text(String.valueOf(value)); }
    public static Tag td(Element... kids)         { return new Tag("td").children(kids); }

    // Media
    public static Tag img(String src)             { return new Tag("img").src(src); }
    public static Tag video()                     { return t("video"); }
    public static Tag audio()                     { return t("audio"); }
    public static Tag canvas()                    { return t("canvas"); }
    public static Tag svg()                       { return t("svg"); }
    public static Tag path()                      { return t("path"); }

    // Convenience: spinner, errorBanner, emptyState are reusable “component refs”
    // (a real codegen emits Spinner.tsx, etc., on `macc new`).

    private Tags() {}
}
