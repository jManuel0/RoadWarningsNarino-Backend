package com.roadwarnings.narino.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para aplicar rate limiting específico en endpoints críticos
 * Se puede usar en métodos de controllers para limitar creación de alertas, comentarios, etc.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    /**
     * Tipo de rate limit a aplicar
     */
    Type value() default Type.DEFAULT;

    enum Type {
        DEFAULT,        // Rate limit general
        ALERT_CREATION, // Rate limit para creación de alertas (5/hora)
        COMMENT_CREATION // Rate limit para creación de comentarios (10/hora)
    }
}
