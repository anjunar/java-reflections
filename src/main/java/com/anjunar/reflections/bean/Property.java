package com.anjunar.reflections.bean;

import com.anjunar.reflections.annotations.Annotated;
import com.anjunar.reflections.members.FieldSymbol;
import com.anjunar.reflections.members.MethodSymbol;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Property implements Annotated {

    private final String name;

    private final FieldSymbol field;

    private final MethodSymbol getter;

    private final MethodSymbol setter;

    public Property(String name, FieldSymbol field, MethodSymbol getter, MethodSymbol setter) {
        this.name = name;
        this.field = field;
        this.getter = getter;
        this.setter = setter;
    }

    public Object get(Object object) {
        return getter.invoke(object);
    }

    public void set(Object object, Object param) {
        if (Objects.nonNull(setter)) {
            setter.invoke(object, param);
        } else {
            throw new IllegalStateException("No Setter available");
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        List<Annotation> annotations = new ArrayList<>(List.of(getter.getDeclaredAnnotations()));

        if (Objects.nonNull(field)) {
            annotations.addAll(List.of(field.getDeclaredAnnotations()));
        }
        if (Objects.nonNull(setter)) {
            annotations.addAll(List.of(setter.getAnnotations()));
        }
        return annotations.toArray(Annotation[]::new);
    }

    @Override
    public Annotation[] getAnnotations() {
        List<Annotation> annotations = new ArrayList<>(List.of(getter.getAnnotations()));

        if (Objects.nonNull(field)) {
            annotations.addAll(List.of(field.getAnnotations()));
        }
        if (Objects.nonNull(setter)) {
            annotations.addAll(List.of(setter.getAnnotations()));
        }
        return annotations.toArray(Annotation[]::new);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Property property)) return false;
        return Objects.equals(name, property.name) &&
                Objects.equals(field, property.field) &&
                Objects.equals(getter, property.getter) &&
                Objects.equals(setter, property.setter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, field, getter, setter);
    }

    @Override
    public String toString() {
        if (Objects.nonNull(setter)) {
            return "var " + name + " " + getter.getReturnType();
        } else {
            return "val " + name + " " + getter.getReturnType();
        }

    }
}
