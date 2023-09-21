package com.anjunar.reflections.types;

import com.anjunar.reflections.nodes.NodeSymbol;

import java.lang.reflect.Type;

public abstract class TypeSymbol extends NodeSymbol {

    private final Type underlying;

    private final NodeSymbol owner;

    public TypeSymbol(Type underlying, NodeSymbol owner) {
        this.underlying = underlying;
        this.owner = owner;
    }

    public String getTypeName() {
        return underlying.getTypeName();
    }

    @Override
    public String toString() {
        return getTypeName();
    }
}
