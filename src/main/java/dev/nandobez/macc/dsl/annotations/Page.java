package dev.nandobez.macc.dsl.annotations;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
public @interface Page { String value(); String path() default ""; }
