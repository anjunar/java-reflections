package com.anjunar.reflections.members;

import com.anjunar.reflections.nodes.NodeVisitor;
import com.anjunar.reflections.types.ClassSymbol;
import com.anjunar.reflections.types.TypeResolver;
import com.anjunar.reflections.types.TypeSymbol;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FieldSymbol extends MemberSymbol {

    private static final Map<Field, FieldSymbol> cache = new HashMap<>();
    private final Field underlying;

    private TypeSymbol type;

    private FieldSymbol(Field underlying, ClassSymbol owner) {
        super(underlying, owner);
        this.underlying = underlying;
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

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return underlying.getDeclaredAnnotations();
    }

    @Override
    public String toString() {
        return STR."\{getType()} \{getName()}";
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
