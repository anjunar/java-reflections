package com.anjunar.reflections.members;

import com.anjunar.reflections.Utils;
import com.anjunar.reflections.nodes.NodeVisitor;
import com.anjunar.reflections.types.ClassSymbol;
import com.anjunar.reflections.types.TypeResolver;
import com.anjunar.reflections.types.TypeSymbol;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    public Object invoke(Object object, Object... params) {
        try {
            return underlying.invoke(object, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean equalSignature(Method method) {
        return underlying.getName().equals(method.getName()) && Arrays.equals(underlying.getParameters(), method.getParameters());
    }

    public String getName() {
        return underlying.getName();
    }

    public TypeSymbol getGenericReturnType() {
        if (Objects.isNull(returnType)) {
            returnType = TypeResolver.resolve(underlying.getGenericReturnType(), this);
        }
        return returnType;
    }

    public ClassSymbol getReturnType() {
        return Utils.getRawType(getGenericReturnType());
    }

    public ClassSymbol getOwner() {
        return owner;
    }

    public MethodSymbol[] getHidden() {
        if (Objects.isNull(overridden)) {
            TypeSymbol[] hierarchy = owner.getHierarchy();
            Class<?>[] parameters = Arrays
                    .stream(getParameters())
                    .flatMap(param -> Utils.extractRaw(param.getType()).map(ClassSymbol::getUnderlying))
                    .toArray(Class<?>[]::new);

            overridden = Arrays.stream(hierarchy)
                    .flatMap(Utils::extractRaw)
                    .filter(clazz -> {
                        try {
                            return clazz.getUnderlying().getDeclaredMethod(getName(), parameters) != null;
                        } catch (NoSuchMethodException e) {
                            return false;
                        }
                    })
                    .map(clazz -> {
                        try {
                            Method declaredMethod = clazz.getUnderlying().getDeclaredMethod(getName(), parameters);
                            return MethodSymbol.newInstance(declaredMethod, ClassSymbol.newInstance(declaredMethod.getDeclaringClass(), null));
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
        return STR."\{Utils.annotation(getAnnotations())}\{super.toString()}\{getReturnType()} \{getName()}(\{Utils.collection(getParameters(), ", ")}) [\{getHidden().length}]";
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
