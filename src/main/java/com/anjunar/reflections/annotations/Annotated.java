package com.anjunar.reflections.annotations;

import java.lang.annotation.Annotation;
import java.util.Arrays;

public interface Annotated {

    Annotation[] getDeclaredAnnotations();

    Annotation[] getAnnotations();

    default <A extends Annotation> A getAnnotation(Class<A> aClass) {
        return (A) Arrays.stream(getAnnotations())
                .filter(annotation -> annotation.annotationType().equals(aClass))
                .findFirst()
                .orElse(null);
    }

    default <A extends Annotation> A getDeclaredAnnotation(Class<A> aClass) {
        return (A) Arrays.stream(getDeclaredAnnotations())
                .filter(annotation -> annotation.annotationType().equals(aClass))
                .findFirst()
                .orElse(null);
    }

}
