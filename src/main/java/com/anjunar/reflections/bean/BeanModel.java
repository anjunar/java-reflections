package com.anjunar.reflections.bean;

import com.anjunar.reflections.Utils;
import com.anjunar.reflections.annotations.Annotated;
import com.anjunar.reflections.members.FieldSymbol;
import com.anjunar.reflections.members.MethodSymbol;
import com.anjunar.reflections.types.ClassSymbol;
import com.anjunar.reflections.types.TypeSymbol;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BeanModel implements Annotated {

    private final Pattern getterRegex = Pattern.compile("^is|get(\\w+)");
    private final TypeSymbol symbol;

    private BeanProperty[] properties;

    public BeanModel(TypeSymbol symbol) {
        this.symbol = symbol;
    }

    public BeanProperty find(String name) {
        return Arrays.stream(getProperties())
                .filter(property -> property.getName().equals(name))
                .findFirst()
                .get();
    }

    public BeanProperty[] getProperties() {
        if (Objects.isNull(properties)) {
            properties = Arrays.stream(Utils.getRawType(symbol).getMethods())
                    .filter(method -> {
                        Matcher matcher = getterRegex.matcher(method.getName());
                        return matcher.matches() && method.getParameters().length == 0 && ! method.getOwner().getUnderlying().equals(Object.class);
                    })
                    .map(getterMethod -> {
                        Matcher matcher = getterRegex.matcher(getterMethod.getName());
                        if (matcher.matches()) {
                            String group = matcher.group(1);
                            String propertyName = group.substring(0, 1).toLowerCase() + group.substring(1);

                            FieldSymbol backedField = Arrays.stream(Utils.getRawType(symbol).getFields())
                                    .filter(field -> field.getName().equals(propertyName))
                                    .findFirst()
                                    .orElse(null);

                            MethodSymbol setterMethod = Arrays.stream(Utils.getRawType(symbol).getMethods())
                                    .filter(method -> method.getName().equals("set" + group) && method.getParameters().length == 1)
                                    .findFirst()
                                    .orElse(null);

                            return new BeanProperty(propertyName, symbol, backedField, getterMethod, setterMethod);
                        }

                        throw new IllegalStateException("no getter found " + getterMethod.getName());
                    })
                    .toArray(BeanProperty[]::new);

        }
        return properties;
    }

    public TypeSymbol getSymbol() {
        return symbol;
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return Utils.getRawType(symbol).getDeclaredAnnotations();
    }

    @Override
    public Annotation[] getAnnotations() {
        return Utils.getRawType(symbol).getAnnotations();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof BeanModel beanModel)) return false;
        return Objects.equals(symbol, beanModel.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

    @Override
    public String toString() {
        return STR."Bean \{symbol.toString()}\n\{Arrays.stream(getProperties()).map(BeanProperty::toString).collect(Collectors.joining("\n"))}";
    }
}
