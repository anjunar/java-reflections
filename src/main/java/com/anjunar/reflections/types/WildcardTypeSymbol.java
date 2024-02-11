package com.anjunar.reflections.types;

import com.anjunar.reflections.Utils;
import com.anjunar.reflections.nodes.NodeSymbol;
import com.anjunar.reflections.nodes.NodeVisitor;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class WildcardTypeSymbol extends TypeSymbol {

    private static final Map<WildcardType, WildcardTypeSymbol> cache = new HashMap<>();
    private final WildcardType underlying;

    private TypeSymbol[] upper;

    private TypeSymbol[] lower;

    public WildcardTypeSymbol(WildcardType underlying, NodeSymbol owner) {
        super(underlying, owner);
        this.underlying = underlying;
    }

    public TypeSymbol[] getUpper() {
        if (Objects.isNull(upper)) {
            upper = Arrays.stream(underlying.getUpperBounds())
                    .map(bound -> TypeResolver.resolve(bound, this))
                    .toArray(TypeSymbol[]::new);
        }
        return upper;
    }

    public TypeSymbol[] getLower() {
        if (Objects.isNull(lower)) {
            lower = Arrays.stream(underlying.getLowerBounds())
                    .map(bound -> TypeResolver.resolve(bound, this))
                    .toArray(TypeSymbol[]::new);
        }
        return lower;
    }

    @Override
    public String toString() {
        String result = "";
        if (getLower().length > 0) {
            result += "? super " + Utils.collection(getLower(), ", ") + " ";
        }
        if (getUpper().length > 0) {
            result += "? extends " + Utils.collection(getUpper(), ", ");
        }
        return result;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public static WildcardTypeSymbol newInstance(WildcardType underlying, NodeSymbol owner) {

        WildcardTypeSymbol symbol = cache.getOrDefault(underlying, new WildcardTypeSymbol(underlying, owner));

        if (! cache.containsKey(underlying)) {
            cache.put(underlying, symbol);
        }

        return symbol;
    }
}
