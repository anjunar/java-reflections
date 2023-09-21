package com.anjunar.reflections.members;

import com.anjunar.reflections.Utils;
import com.anjunar.reflections.nodes.NodeVisitor;
import com.anjunar.reflections.types.ClassSymbol;
import com.anjunar.reflections.types.TypeResolver;
import com.anjunar.reflections.types.TypeSymbol;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MethodSymbol extends ExecutableSymbol {

    private static final Map<Method, MethodSymbol> cache = new HashMap<>();
    private final Method underlying;

    private final ClassSymbol owner;
    private TypeSymbol returnType;

    private MethodSymbol[] overridden;

    private MethodSymbol(Method underlying, ClassSymbol owner) {
        super(underlying, owner);
        this.underlying = underlying;
        this.owner = owner;
    }

    public String getName() {
        return underlying.getName();
    }

    public TypeSymbol getReturnType() {
        if (Objects.isNull(returnType)) {
            returnType = TypeResolver.resolve(underlying.getGenericReturnType(), this);
        }
        return returnType;
    }

    @Override
    public MethodSymbol[] getOverridden() {
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
                            return clazz.getUnderlying().getDeclaredMethod(getName(), parameters) != null;
                        } catch (NoSuchMethodException e) {
                            return false;
                        }
                    })
                    .map(clazz -> {
                        try {
                            return MethodSymbol.newInstance(clazz.getUnderlying().getDeclaredMethod(getName(), parameters), owner);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(MethodSymbol[]::new);
        }
        return overridden;
    }

    @Override
    public String toString() {
        return STR."\{Utils.collection(getAnnotations())}\{getReturnType()} \{getName()}(\{ Utils.collection(getParameters())})";
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public static MethodSymbol newInstance(Method underlying, ClassSymbol classSymbol) {

        final MethodSymbol symbol = cache.getOrDefault(underlying, new MethodSymbol(underlying, classSymbol));

        if (! cache.containsKey(underlying)) {
            cache.put(underlying, symbol);
        }

        return symbol;
    }
}
