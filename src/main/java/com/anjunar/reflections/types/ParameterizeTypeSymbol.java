package com.anjunar.reflections.types;

import com.anjunar.reflections.Utils;
import com.anjunar.reflections.nodes.NodeSymbol;
import com.anjunar.reflections.nodes.NodeVisitor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;

public class ParameterizeTypeSymbol extends TypeSymbol {

    private static final Map<ParameterizedType, ParameterizeTypeSymbol> cache = new HashMap<>();
    private final ParameterizedType underlying;

    private TypeSymbol type;

    private TypeSymbol[] arguments;

    public ParameterizeTypeSymbol(ParameterizedType underlying, NodeSymbol owner) {
        super(underlying, owner);
        this.underlying = underlying;
    }

    public TypeSymbol getType() {
        if (Objects.isNull(type)) {
            type = TypeResolver.resolve(underlying.getRawType(), this);
        }
        return type;
    }

    public TypeSymbol[] getArguments() {
        if (Objects.isNull(arguments)) {
            arguments = Arrays.stream(underlying.getActualTypeArguments())
                    .map(arg -> TypeResolver.resolve(arg, this))
                    .toArray(TypeSymbol[]::new);
        }
        return arguments;
    }

    @Override
    public String toString() {
        return STR."\{getType()}\{Utils.brackets(getArguments())}";
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public static ParameterizeTypeSymbol newInstance(ParameterizedType parameterizedType, NodeSymbol owner) {

        ParameterizeTypeSymbol symbol = cache.getOrDefault(parameterizedType, new ParameterizeTypeSymbol(parameterizedType, owner));

        if (! cache.containsKey(parameterizedType)) {
            cache.put(parameterizedType, symbol);
        }

        return symbol;
    }
}
