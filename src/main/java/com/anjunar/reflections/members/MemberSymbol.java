package com.anjunar.reflections.members;

import com.anjunar.reflections.Utils;
import com.anjunar.reflections.annotations.Annotated;
import com.anjunar.reflections.nodes.NodeSymbol;
import com.anjunar.reflections.types.ClassSymbol;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public AccessFlag[] getModifier() {
        return underlying.accessFlags().toArray(new AccessFlag[]{});
    }

    @Override
    public String toString() {
        if (getModifier().length > 0) {
            return Utils.collection2(getModifier(), " ") + " ";
        }
        return "";
    }
}
