package cc.thonly.reverie_dreams.api;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ReverieDreamsExtension {
    int priority() default 0;
}
