package com.anjunar.reflections.types;

import com.anjunar.reflections.annotations.Annotated;
import com.anjunar.reflections.nodes.NodeSymbol;
import com.anjunar.reflections.nodes.NodeVisitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TypeVariableSymbol extends TypeSymbol implements Annotated {

    private static final Map<TypeVariable<?>, TypeVariableSymbol> cache = new HashMap<>();

    private final TypeVariable<?> underlying;

    private TypeSymbol[] bounds;

    public TypeVariableSymbol(TypeVariable<?> underlying, NodeSymbol owner) {
        super(underlying, owner);
        this.underlying = underlying;
    }

    public String getName() {
        return underlying.getName();
    }

    public TypeSymbol[] getBounds() {
        if (Objects.isNull(bounds)) {
            bounds = Arrays.stream(underlying.getBounds()).map(bound -> TypeResolver.resolve(bound, this))
                    .toArray(TypeSymbol[]::new);
        }
        return bounds;
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return underlying.getDeclaredAnnotations();
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public static TypeVariableSymbol newInstance(TypeVariable<?> underlying, NodeSymbol owner) {

        TypeVariableSymbol symbol = cache.getOrDefault(underlying, new TypeVariableSymbol(underlying, owner));

        if (!cache.containsKey(underlying)) {
            cache.put(underlying, symbol);
        }

        return symbol;
    }
}
