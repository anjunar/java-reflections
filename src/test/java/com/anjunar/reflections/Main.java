package com.anjunar.reflections;

import com.anjunar.reflections.bean.BeanModel;
import com.anjunar.reflections.types.ClassSymbol;
import com.anjunar.reflections.types.TypeResolver;
import com.anjunar.reflections.types.TypeSymbol;
import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {

        List<String> paths = Lists.newArrayList("com.anjunar.reflections");

        ClassPath classPath = ClassPath.from(ClassLoader.getSystemClassLoader());

        Set<Class<?>> classes = classPath.getAllClasses()
                .stream()
                .filter(classInfo -> paths.stream().anyMatch(path -> classInfo.getName().startsWith(path)))
                .map(ClassPath.ClassInfo::load)
                .collect(Collectors.toSet());

/*
        Resolver resolver = Reflections.init(classes);

        for (BeanModel bean : resolver.universe().beans()) {
            System.out.println(bean);
            System.out.println("\n");
        }
*/

        ClassSymbol resolved = (ClassSymbol) TypeResolver.resolve(Person.class, null);
        BeanModel beanModel = new BeanModel(resolved);

        System.out.println(beanModel);
    }

}
