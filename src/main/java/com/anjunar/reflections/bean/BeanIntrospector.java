package com.anjunar.reflections.bean;

import com.anjunar.reflections.types.ClassSymbol;
import com.anjunar.reflections.types.TypeResolver;
import com.anjunar.reflections.types.TypeSymbol;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BeanIntrospector {

    private final static Map<TypeSymbol, BeanModel> cache = new HashMap<>();

    public static BeanModel create(TypeSymbol aClass) {
        BeanModel beanModel = cache.get(aClass);
        if (Objects.isNull(beanModel)) {
            beanModel = new BeanModel(aClass);
            cache.put(aClass, beanModel);
        }

        return beanModel;
    }

}
