package com.anjunar.reflections.members;

import com.anjunar.reflections.annotations.Annotated;
import com.anjunar.reflections.nodes.NodeSymbol;
import com.anjunar.reflections.types.ClassSymbol;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

public abstract class MemberSymbol extends NodeSymbol implements Annotated {

    private final Member underlying;

    private final ClassSymbol owner;

    public MemberSymbol(Member underlying, ClassSymbol owner) {
        this.underlying = underlying;
        this.owner = owner;
    }


}
