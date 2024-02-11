package com.anjunar.reflections.bean;

import com.anjunar.reflections.Utils;
import com.anjunar.reflections.annotations.Annotated;
import com.anjunar.reflections.members.FieldSymbol;
import com.anjunar.reflections.members.MethodSymbol;
import com.anjunar.reflections.types.ClassSymbol;
import com.anjunar.reflections.types.TypeResolver;
import com.anjunar.reflections.types.TypeSymbol;
import com.google.common.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BeanProperty implements Annotated {

    private final String name;

    private final TypeSymbol symbol;

    private final FieldSymbol field;

    private final MethodSymbol getter;

    private final MethodSymbol setter;

    public BeanProperty(String name, TypeSymbol symbol, FieldSymbol field, MethodSymbol getter, MethodSymbol setter) {
        this.name = name;
        this.symbol = symbol;
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

    public TypeSymbol getGenericType() {
        Type underlying = getter.getGenericReturnType().getUnderlying();

        TypeToken typeToken = TypeToken.of(underlying);

        if (underlying instanceof TypeVariable<?>) {
            if (symbol.getUnderlying() instanceof ParameterizedType parameterizedType) {
                for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
                    typeToken = typeToken.resolveType(actualTypeArgument);
                }
            }
        }


        return TypeResolver.resolve(typeToken.getType(), symbol);
    }

    public ClassSymbol getType() {
        return Utils.getRawType(getGenericType());
    }

    public TypeToken<?> getTypeToken() {
        return TypeToken.of(getGenericType().getUnderlying());
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
        if (!(object instanceof BeanProperty property)) return false;
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
            return "var " + name + " " + getGenericType();
        } else {
            return "val " + name + " " + getGenericType();
        }

    }
}
