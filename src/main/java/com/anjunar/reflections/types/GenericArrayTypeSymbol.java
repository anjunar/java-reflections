package com.anjunar.reflections.types;

import com.anjunar.reflections.Utils;
import com.anjunar.reflections.nodes.NodeSymbol;
import com.anjunar.reflections.nodes.NodeVisitor;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GenericArrayTypeSymbol extends TypeSymbol {

    private static final Map<GenericArrayType, GenericArrayTypeSymbol> cache = new HashMap<>();
    private final GenericArrayType underlying;

    private TypeSymbol type;

    public GenericArrayTypeSymbol(GenericArrayType underlying, NodeSymbol owner) {
        super(underlying, owner);
        this.underlying = underlying;
    }

    public TypeSymbol getType() {
        if (Objects.isNull(type)) {
            type = TypeResolver.resolve(underlying.getGenericComponentType(), this);
        }
        return type;
    }

    @Override
    public String toString() {
        return STR."\{getType()}[]";
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public static GenericArrayTypeSymbol newInstance(GenericArrayType underlying, NodeSymbol owner) {

        GenericArrayTypeSymbol symbol = cache.getOrDefault(underlying, new GenericArrayTypeSymbol(underlying, owner));

        if (! cache.containsKey(underlying)) {
            cache.put(underlying, symbol);
        }

        return symbol;
    }
}
