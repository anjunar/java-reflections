package com.anjunar.reflections.members;

import com.anjunar.reflections.Utils;
import com.anjunar.reflections.nodes.NodeVisitor;
import com.anjunar.reflections.types.ClassSymbol;
import com.anjunar.reflections.types.TypeResolver;
import com.anjunar.reflections.types.TypeSymbol;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class FieldSymbol extends MemberSymbol {

    private static final Map<Field, FieldSymbol> cache = new HashMap<>();
    private final Field underlying;

    private final ClassSymbol owner;

    private TypeSymbol type;

    private FieldSymbol[] hidden;

    private FieldSymbol(Field underlying, ClassSymbol owner) {
        super(underlying, owner);
        this.underlying = underlying;
        this.owner = owner;
    }

    public Object get(Object object) {
        try {
            return this.underlying.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return underlying.getName();
    }

    public TypeSymbol getGenericType() {
        if (Objects.isNull(type)) {
            type = TypeResolver.resolve(underlying.getGenericType(), this);
        }
        return type;
    }

    public ClassSymbol getType() {
        return Utils.getRawType(getGenericType());
    }

    public FieldSymbol[] getHidden() {
        if (Objects.isNull(hidden)) {
            hidden = Arrays.stream(owner.getHierarchy())
                    .flatMap(Utils::extractRaw)
                    .filter(classSymbol -> {
                        try {
                            return classSymbol.getUnderlying().getDeclaredField(getName()) != null;
                        } catch (NoSuchFieldException e) {
                            return false;
                        }
                    })
                    .map(classSymbol -> {
                        try {
                            return FieldSymbol.newInstance(classSymbol.getUnderlying().getDeclaredField(getName()), owner);
                        } catch (NoSuchFieldException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(FieldSymbol[]::new);
        }
        return hidden;
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return underlying.getDeclaredAnnotations();
    }

    @Override
    public Annotation[] getAnnotations() {
        return Stream.concat(Stream.of(this), Arrays.stream(getHidden()))
                .flatMap(field -> Arrays.stream(field.getDeclaredAnnotations()))
                .toArray(Annotation[]::new);
    }

    @Override
    public String toString() {
        return STR."\{Utils.annotation(getAnnotations())}\{super.toString()}\{getType()} \{getName()} [\{getHidden().length}]";
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public static FieldSymbol newInstance(Field underlying, ClassSymbol classSymbol) {

        final FieldSymbol symbol = cache.getOrDefault(underlying, new FieldSymbol(underlying, classSymbol));

        if (! cache.containsKey(underlying)) {
            cache.put(underlying, symbol);
        }

        return symbol;
    }
}
