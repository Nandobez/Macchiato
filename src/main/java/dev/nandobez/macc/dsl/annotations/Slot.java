package dev.nandobez.macc.dsl.annotations;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
public @interface Slot { String value() default "children"; }
