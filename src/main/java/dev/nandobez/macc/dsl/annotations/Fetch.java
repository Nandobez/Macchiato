package dev.nandobez.macc.dsl.annotations;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
public @interface Fetch {
    String value() default "";          // url shorthand
    String url() default "";
    String into() default "";
    String empty() default "";
    String error() default "";
    String loading() default "";
}
