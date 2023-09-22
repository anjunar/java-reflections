package com.anjunar.reflections.types;

import com.anjunar.reflections.nodes.NodeSymbol;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TypeResolver {

    public static TypeSymbol resolve(Type type, NodeSymbol owner) {
        return switch (type) {
            case Class<?> clazz -> ClassSymbol.newInstance(clazz, owner);
            case ParameterizedType parameterizedType -> ParameterizeTypeSymbol.newInstance(parameterizedType, owner);
            case TypeVariable<?> typeVariable -> TypeVariableSymbol.newInstance(typeVariable, owner);
            case GenericArrayType genericArrayType -> GenericArrayTypeSymbol.newInstance(genericArrayType, owner);
            case WildcardType wildcardType -> WildcardTypeSymbol.newInstance(wildcardType, owner);
            default -> throw new IllegalStateException("Unexpected value: " + type.getTypeName());
        };
    }
}
