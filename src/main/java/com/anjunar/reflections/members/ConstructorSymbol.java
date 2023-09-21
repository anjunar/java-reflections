package com.anjunar.reflections.members;

import com.anjunar.reflections.Utils;
import com.anjunar.reflections.nodes.NodeVisitor;
import com.anjunar.reflections.types.ClassSymbol;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;

public class ConstructorSymbol extends ExecutableSymbol {

    private static final Map<Constructor<?>, ConstructorSymbol> cache = new HashMap<>();
    private final Constructor<?> underlying;

    private ConstructorSymbol(Constructor<?> underlying, ClassSymbol owner) {
        super(underlying, owner);
        this.underlying = underlying;
    }

    @Override
    public String toString() {
        return STR."\{underlying.getDeclaringClass().getSimpleName()}(\{ Utils.collection(getParameters())})";
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public static ConstructorSymbol newInstance(Constructor<?> underlying, ClassSymbol classSymbol) {

        final ConstructorSymbol symbol = cache.getOrDefault(underlying, new ConstructorSymbol(underlying, classSymbol));

        if (! cache.containsKey(underlying)) {
            cache.put(underlying, symbol);
        }

        return symbol;
    }

}
