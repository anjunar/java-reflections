package com.anjunar.reflections.bean;

import com.anjunar.reflections.types.ClassSymbol;
import com.anjunar.reflections.types.TypeResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BeanIntrospector {

    private final static Map<Class<?>, BeanModel> cache = new HashMap<>();

    public static BeanModel create(ClassSymbol symbol) {
        return create(symbol.getUnderlying());
    }

    public static BeanModel create(Class<?> aClass) {
        BeanModel beanModel = cache.get(aClass);
        if (Objects.isNull(beanModel)) {
            ClassSymbol resolved = (ClassSymbol) TypeResolver.resolve(aClass, null);
            beanModel = new BeanModel(resolved);
            cache.put(aClass, beanModel);
        }

        return beanModel;
    }

}
