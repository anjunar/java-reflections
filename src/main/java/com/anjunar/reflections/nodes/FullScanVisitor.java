package com.anjunar.reflections.nodes;

import com.anjunar.reflections.members.*;
import com.anjunar.reflections.types.*;

import java.util.HashSet;
import java.util.Set;

public class FullScanVisitor implements NodeVisitor {

    protected final Set<NodeSymbol> cache = new HashSet<>();

    @Override
    public void visit(ClassSymbol symbol) {
        if (!cache.contains(symbol)) {
            cache.add(symbol);

            for (MemberSymbol declaredMember : symbol.getDeclaredMembers()) {
                declaredMember.accept(this);
            }

            for (ClassSymbol declaredClass : symbol.getDeclaredClasses()) {
                declaredClass.accept(this);
            }

            for (TypeSymbol typeSymbol : symbol.getHierarchy()) {
                typeSymbol.accept(this);
            }
        }
    }

    @Override
    public void visit(ExecutableSymbol.ParameterSymbol symbol) {
        if (!cache.contains(symbol)) {
            cache.add(symbol);

            symbol.getType().accept(this);
        }
    }

    @Override
    public void visit(GenericArrayTypeSymbol symbol) {
        if (!cache.contains(symbol)) {
            cache.add(symbol);
            symbol.getType().accept(this);
        }

    }

    @Override
    public void visit(ParameterizeTypeSymbol symbol) {
        if (!cache.contains(symbol)) {
            cache.add(symbol);
            symbol.getType().accept(this);
            for (TypeSymbol argument : symbol.getArguments()) {
                argument.accept(this);
            }
        }

    }

    @Override
    public void visit(TypeVariableSymbol symbol) {
        if (!cache.contains(symbol)) {
            cache.add(symbol);
            for (TypeSymbol bound : symbol.getBounds()) {
                bound.accept(this);
            }
        }

    }

    @Override
    public void visit(WildcardTypeSymbol symbol) {
        if (!cache.contains(symbol)) {
            cache.add(symbol);

            for (TypeSymbol typeSymbol : symbol.getUpper()) {
                typeSymbol.accept(this);
            }

            for (TypeSymbol typeSymbol : symbol.getLower()) {
                typeSymbol.accept(this);
            }
        }

    }

    @Override
    public void visit(FieldSymbol symbol) {
        if (!cache.contains(symbol)) {
            cache.add(symbol);

            symbol.getType().accept(this);
        }

    }

    @Override
    public void visit(ConstructorSymbol symbol) {
        if (!cache.contains(symbol)) {
            cache.add(symbol);

            for (ExecutableSymbol.ParameterSymbol parameter : symbol.getParameters()) {
                parameter.accept(this);
            }
        }

    }

    @Override
    public void visit(MethodSymbol symbol) {
        if (!cache.contains(symbol)) {
            cache.add(symbol);

            for (ExecutableSymbol.ParameterSymbol parameter : symbol.getParameters()) {
                parameter.accept(this);
            }

            symbol.getReturnType().accept(this);
        }

    }
}
