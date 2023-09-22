package com.anjunar.reflections;

import com.anjunar.reflections.types.ClassSymbol;
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

        Resolver resolver = Reflections.init(classes);

        Collection<ClassSymbol> extendz = resolver.universe().isExtendingFrom(resolver.get(Identity.class));

//        Utils.renderToConsole(resolver.symbols());

        System.out.println(resolver.symbols().size());

    }

}
