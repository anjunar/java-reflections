package com.anjunar.reflections.members;

import com.anjunar.reflections.annotations.Annotated;
import com.anjunar.reflections.nodes.NodeSymbol;
import com.anjunar.reflections.nodes.NodeVisitor;
import com.anjunar.reflections.types.ClassSymbol;
import com.anjunar.reflections.types.TypeResolver;
import com.anjunar.reflections.types.TypeSymbol;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class ExecutableSymbol extends MemberSymbol {

    private final Executable underlying;

    private ParameterSymbol[] parameters;

    private Annotation[] annotations;

    public ExecutableSymbol(Executable underlying, ClassSymbol owner) {
        super(underlying, owner);
        this.underlying = underlying;
    }

    public abstract ExecutableSymbol[] getHidden();

    public ParameterSymbol[] getParameters() {
        if (Objects.isNull(parameters)) {
            Parameter[] parameters = underlying.getParameters();
            this.parameters = Arrays.stream(parameters)
                    .map(param -> ParameterSymbol.newInstance(param, this))
                    .toArray(ParameterSymbol[]::new);
        }
        return parameters;
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return underlying.getDeclaredAnnotations();
    }

    @Override
    public Annotation[] getAnnotations() {
        if (Objects.isNull(annotations)) {
            annotations = Stream.concat(Stream.of(this), Arrays.stream(getHidden()))
                    .flatMap(method -> Arrays.stream(method.getDeclaredAnnotations()))
                    .toArray(Annotation[]::new);
        }
        return annotations;
    }

    public static class ParameterSymbol extends NodeSymbol implements Annotated {

        private static final Map<Parameter, ParameterSymbol> cache = new HashMap<>();
        private final Parameter underlying;
        private final ExecutableSymbol owner;

        private Annotation[] annotations;

        private TypeSymbol type;

        private ParameterSymbol(Parameter underlying, ExecutableSymbol owner) {
            this.underlying = underlying;
            this.owner = owner;
        }

        public String getName() {
            return underlying.getName();
        }

        public TypeSymbol getType() {
            if (Objects.isNull(type)) {
                type = TypeResolver.resolve(underlying.getParameterizedType(), this);
            }
            return type;
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return underlying.getDeclaredAnnotations();
        }


        @Override
        public Annotation[] getAnnotations() {
            if (Objects.isNull(annotations)) {
                int parameterIndex = Arrays.asList(owner.getParameters()).indexOf(this);
                annotations = Arrays.stream(owner.getHidden())
                        .flatMap(symbol -> Arrays.stream(symbol.getParameters()[parameterIndex].getDeclaredAnnotations()))
                        .toArray(Annotation[]::new);
            }
            return annotations;
        }

        @Override
        public String toString() {
            return STR."\{getType()} \{getName()}";
        }

        @Override
        public void accept(NodeVisitor visitor) {
            visitor.visit(this);
        }

        public static ParameterSymbol newInstance(Parameter parameter, ExecutableSymbol owner) {

            ParameterSymbol symbol = cache.getOrDefault(parameter, new ParameterSymbol(parameter, owner));

            if (cache.containsKey(parameter)) {
                cache.put(parameter, symbol);
            }

            return symbol;
        }
    }
}
