package dev.nandobez.macc.dsl.annotations;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
public @interface Effect { String deps() default ""; }
