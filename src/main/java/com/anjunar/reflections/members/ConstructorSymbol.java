package com.anjunar.reflections.members;

import com.anjunar.reflections.Utils;
import com.anjunar.reflections.nodes.NodeVisitor;
import com.anjunar.reflections.types.ClassSymbol;
import com.anjunar.reflections.types.TypeSymbol;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConstructorSymbol extends ExecutableSymbol {

    private static final Map<Constructor<?>, ConstructorSymbol> cache = new HashMap<>();
    private final Constructor<?> underlying;
    private final ClassSymbol owner;

    private ConstructorSymbol[] hidden;

    private ConstructorSymbol(Constructor<?> underlying, ClassSymbol owner) {
        super(underlying, owner);
        this.underlying = underlying;
        this.owner = owner;
    }

    @Override
    public ConstructorSymbol[] getHidden() {
        if (Objects.isNull(hidden)) {
            TypeSymbol[] hierarchy = owner.getHierarchy();
            Class<?>[] parameters = Arrays
                    .stream(getParameters())
                    .flatMap(param -> Utils.extractRaw(param.getType()).map(ClassSymbol::getUnderlying))
                    .toArray(Class<?>[]::new);

            hidden = Arrays.stream(hierarchy)
                    .flatMap(Utils::extractRaw)
                    .filter(clazz -> {
                        try {
                            return clazz.getUnderlying().getDeclaredConstructor(parameters) != null;
                        } catch (NoSuchMethodException e) {
                            return false;
                        }
                    })
                    .map(clazz -> {
                        try {
                            Constructor<?> declaredConstructor = clazz.getUnderlying().getDeclaredConstructor(parameters);
                            return ConstructorSymbol.newInstance(declaredConstructor, ClassSymbol.newInstance(declaredConstructor.getDeclaringClass(), null));
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(ConstructorSymbol[]::new);
        }
        return hidden;
    }

    @Override
    public String toString() {
        return STR."\{Utils.annotation(getAnnotations())}\{super.toString()}\{underlying.getDeclaringClass().getSimpleName()}(\{ Utils.collection(getParameters(), ", ")}) [\{getHidden().length}]";
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
