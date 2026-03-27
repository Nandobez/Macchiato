package dev.nandobez.macc.dsl.annotations;

import java.lang.annotation.*;

/** Declares an HTTP action that Macc emits as a TypeScript handler function.
 *
 *  Example:
 *    @Action(method = "POST", url = "/api/tasks", reloadAfter = true)
 *    void createTask() {}
 *
 *    @Action(method = "DELETE", url = "/api/tasks/{id}", reloadAfter = true)
 *    void remove(Long id) {}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Action {

    /** HTTP method (GET/POST/PUT/PATCH/DELETE). */
    String method() default "POST";

    /** URL — may contain `{paramName}` placeholders matched against the Java parameters. */
    String url();

    /** TS expression used as request body. Defaults to:
     *    - the single non-path parameter (whole object) when present
     *    - else `Object.fromEntries(new FormData(e.currentTarget))` for a form submit. */
    String body() default "";

    /** If true, calls `location.reload()` after the request resolves. */
    boolean reloadAfter() default true;

    /** If true (default for POST with no body), prevents the default form submission. */
    boolean preventDefault() default true;
}
