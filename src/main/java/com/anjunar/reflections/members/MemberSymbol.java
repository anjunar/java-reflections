package com.anjunar.reflections.members;

import com.anjunar.reflections.Utils;
import com.anjunar.reflections.annotations.Annotated;
import com.anjunar.reflections.nodes.NodeSymbol;
import com.anjunar.reflections.types.ClassSymbol;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class MemberSymbol extends NodeSymbol implements Annotated {

    public enum Modifiers {
        PRIVATE("private"),
        PROTECTED("protected"),
        PUBLIC("public");

        final String name;
        String getName() {
            return name;
        }
        Modifiers(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final Member underlying;

    private final ClassSymbol owner;

    public MemberSymbol(Member underlying, ClassSymbol owner) {
        this.underlying = underlying;
        this.owner = owner;
    }

    public Modifiers[] getModifier() {
        List<Modifiers> modifiers = new ArrayList<>();
        if (Modifier.isPublic(underlying.getModifiers())) {
            modifiers.add(Modifiers.PUBLIC);
        }
        if (Modifier.isProtected(underlying.getModifiers())) {
            modifiers.add(Modifiers.PROTECTED);
        }
        if (Modifier.isPrivate(underlying.getModifiers())) {
            modifiers.add(Modifiers.PRIVATE);
        }
        return modifiers.toArray(new Modifiers[0]);
    }

    @Override
    public String toString() {
        return Utils.collection(getModifier(), " ") + " ";
    }
}
