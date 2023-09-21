package com.anjunar.reflections.members;

import com.anjunar.reflections.Utils;
import com.anjunar.reflections.nodes.NodeVisitor;
import com.anjunar.reflections.types.ClassSymbol;
import com.anjunar.reflections.types.TypeSymbol;

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
    private final ClassSymbol owner;

    private ConstructorSymbol[] overridden;

    private ConstructorSymbol(Constructor<?> underlying, ClassSymbol owner) {
        super(underlying, owner);
        this.underlying = underlying;
        this.owner = owner;
    }

    @Override
    public ConstructorSymbol[] getOverridden() {
        if (Objects.isNull(overridden)) {
            TypeSymbol[] hierarchy = owner.getHierarchy();
            Class<?>[] parameters = Arrays
                    .stream(getParameters())
                    .flatMap(param -> Utils.extracted(param.getType()).map(ClassSymbol::getUnderlying))
                    .toArray(Class<?>[]::new);

            overridden = Arrays.stream(hierarchy)
                    .flatMap(Utils::extracted)
                    .filter(clazz -> {
                        try {
                            return clazz.getUnderlying().getDeclaredConstructor(parameters) != null;
                        } catch (NoSuchMethodException e) {
                            return false;
                        }
                    })
                    .map(clazz -> {
                        try {
                            return ConstructorSymbol.newInstance(clazz.getUnderlying().getDeclaredConstructor(parameters), owner);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(ConstructorSymbol[]::new);
        }
        return overridden;
    }

    @Override
    public String toString() {
        return STR."\{Utils.collection(getAnnotations())}\{underlying.getDeclaringClass().getSimpleName()}(\{ Utils.collection(getParameters())})";
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
