package com.anjunar.reflections.nodes;

import com.anjunar.reflections.members.ConstructorSymbol;
import com.anjunar.reflections.members.ExecutableSymbol;
import com.anjunar.reflections.members.FieldSymbol;
import com.anjunar.reflections.members.MethodSymbol;
import com.anjunar.reflections.types.*;

public interface NodeVisitor {
    void visit(ClassSymbol classSymbol);

    void visit(ExecutableSymbol.ParameterSymbol parameterSymbol);

    void visit(GenericArrayTypeSymbol genericArrayTypeSymbol);

    void visit(ParameterizeTypeSymbol parameterizeTypeSymbol);

    void visit(TypeVariableSymbol typeVariableSymbol);

    void visit(WildcardTypeSymbol wildcardTypeSymbol);

    void visit(FieldSymbol fieldSymbol);

    void visit(ConstructorSymbol constructorSymbol);

    void visit(MethodSymbol methodSymbol);
}
