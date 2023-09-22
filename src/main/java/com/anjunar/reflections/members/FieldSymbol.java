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

    private FieldSymbol[] overridden;

    private FieldSymbol(Field underlying, ClassSymbol owner) {
        super(underlying, owner);
        this.underlying = underlying;
        this.owner = owner;
    }

    public String getName() {
        return underlying.getName();
    }

    public TypeSymbol getType() {
        if (Objects.isNull(type)) {
            type = TypeResolver.resolve(underlying.getGenericType(), this);
        }
        return type;
    }

    public FieldSymbol[] getOverridden() {
        if (Objects.isNull(overridden)) {
            overridden = Arrays.stream(owner.getHierarchy())
                    .flatMap(Utils::extracted)
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
        return overridden;
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return underlying.getDeclaredAnnotations();
    }

    @Override
    public Annotation[] getAnnotations() {
        return Stream.concat(Stream.of(this), Arrays.stream(getOverridden()))
                .flatMap(field -> Arrays.stream(field.getDeclaredAnnotations()))
                .toArray(Annotation[]::new);
    }

    @Override
    public String toString() {
        return STR."\{Utils.collection(getAnnotations())}\{super.toString()}\{getType()} \{getName()}";
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
