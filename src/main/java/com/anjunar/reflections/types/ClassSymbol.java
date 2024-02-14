package com.anjunar.reflections.types;

import com.anjunar.reflections.Utils;
import com.anjunar.reflections.annotations.Annotated;
import com.anjunar.reflections.members.ConstructorSymbol;
import com.anjunar.reflections.members.FieldSymbol;
import com.anjunar.reflections.members.MemberSymbol;
import com.anjunar.reflections.members.MethodSymbol;
import com.anjunar.reflections.nodes.NodeSymbol;
import com.anjunar.reflections.nodes.NodeVisitor;
import com.google.common.reflect.TypeToken;
import javassist.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassSymbol extends TypeSymbol implements Annotated {
    private static final Map<Class<?>, ClassSymbol> cache = new HashMap<>();
    private final Class<?> underlying;

    private final CtClass underlyingAlternative;

    private TypeSymbol superClass;
    private TypeSymbol[] hierarchy;
    private MemberSymbol[] declaredMembers;
    private MemberSymbol[] members;
    private FieldSymbol[] declaredFields;
    private FieldSymbol[] fields;
    private ConstructorSymbol[] declaredConstructors;
    private ConstructorSymbol[] constructors;
    private MethodSymbol[] declaredMethods;
    private MethodSymbol[] methods;
    private ClassSymbol[] declaredClasses;

    private Annotation[] annotations;

    private ClassSymbol(Class<?> underlying, NodeSymbol owner) {
        super(underlying, owner);
        this.underlying = underlying;

        ClassPool classPool = ClassPool.getDefault();
        classPool.insertClassPath(new ClassClassPath(underlying));
        try {
            underlyingAlternative = classPool.get(underlying.getName());
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<?> getUnderlying() {
        return underlying;
    }

    public AccessFlag[] getModifier() {
        return underlying.accessFlags().toArray(new AccessFlag[0]);
    }

    public boolean isAnonymousClass() {
        return underlying.isAnonymousClass();
    }

    public boolean isAssignableFrom(ClassSymbol cls) {
        return underlying.isAssignableFrom(cls.getUnderlying());
    }

    public String getSimpleName() {
        if (isAnonymousClass()) {
            return "<anonymous>";
        }
        return underlying.getSimpleName();
    }

    public String getFullName() {
        if (isAnonymousClass()) {
            return "<anonymous>";
        }
        return underlying.getName();
    }

    public TypeSymbol getSuperClass() {
        if (Objects.isNull(superClass)) {
            if (Objects.nonNull(underlying.getGenericSuperclass())) {
                superClass = TypeResolver.resolve(underlying.getGenericSuperclass(), this);
            }
        }
        return superClass;
    }

    public TypeSymbol[] getInterfaces() {
        return Arrays
                .stream(underlying.getGenericInterfaces())
                .map(inter -> TypeResolver.resolve(inter, this)).toArray(TypeSymbol[]::new);
    }

    public TypeSymbol[] getHierarchy() {
        if (Objects.isNull(hierarchy)) {
            List<TypeSymbol> hierarchy = new ArrayList<>();
            Class<?> cursor = underlying;
            while (cursor != null) {
                if (!cursor.equals(underlying)) {
                    hierarchy.add(ClassSymbol.newInstance(cursor, this));
                }
                hierarchy.addAll(Arrays.stream(cursor.getGenericInterfaces()).map(inter -> TypeResolver.resolve(inter, this)).toList());
                cursor = cursor.getSuperclass();
            }
            this.hierarchy = hierarchy.reversed().toArray(new TypeSymbol[0]);
        }
        return hierarchy;
    }

    public MemberSymbol[] getDeclaredMembers() {
        if (Objects.isNull(declaredMembers)) {
            List<MemberSymbol> symbols = new ArrayList<>();
            symbols.addAll(Arrays.asList(getDeclaredFields()));
            symbols.addAll(Arrays.asList(getDeclaredConstructors()));
            symbols.addAll(Arrays.asList(getDeclaredMethods()));
            declaredMembers = symbols.toArray(new MemberSymbol[0]);
        }
        return declaredMembers;
    }

    public FieldSymbol[] getDeclaredFields() {
        if (Objects.isNull(declaredFields)) {
            try {
                final List<FieldSymbol> fieldSymbols = new ArrayList<>();
                List<CtField> alternativeDeclaredFields = Arrays.stream(underlyingAlternative.getDeclaredFields())
                        .filter(field -> {
                            try {
                                return underlying.getDeclaredField(field.getName()) != null;
                            } catch (NoSuchFieldException e) {
                                return false;
                            }
                        })
                        .toList();
                for (CtField declaredField : alternativeDeclaredFields) {
                    final Field field = underlying.getDeclaredField(declaredField.getName());
                    fieldSymbols.add(FieldSymbol.newInstance(field, this));
                }
                declaredFields = fieldSymbols.toArray(new FieldSymbol[0]);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        return declaredFields;
    }

    public FieldSymbol[] getFields() {
        if (Objects.isNull(fields)) {
            FieldSymbol[] allFieldSymbols = Stream.concat(Arrays.stream(getHierarchy()), Stream.of(this))
                    .flatMap(Utils::extractRaw)
                    .flatMap(clazz -> Arrays.stream(clazz.getDeclaredFields()))
                    .toArray(FieldSymbol[]::new);

            List<FieldSymbol> hidden = Arrays.stream(allFieldSymbols)
                    .filter(field -> field.getHidden().length > 0)
                    .flatMap(field -> Arrays.stream(field.getHidden()))
                    .toList();

            fields = Arrays.stream(allFieldSymbols)
                    .filter(field -> ! hidden.contains(field))
                    .toArray(FieldSymbol[]::new);
        }
        return fields;
    }

    public ConstructorSymbol[] getDeclaredConstructors() {
        if (Objects.isNull(declaredConstructors)) {
            try {
                final List<ConstructorSymbol> constructorSymbols = new ArrayList<>();
                for (CtConstructor declaredConstructor : underlyingAlternative.getDeclaredConstructors()) {
                    Class<?>[] parameters = getParameters(declaredConstructor.getParameterTypes());
                    final Constructor<?> constructor = underlying.getDeclaredConstructor(parameters);
                    constructorSymbols.add(ConstructorSymbol.newInstance(constructor, this));
                }
                declaredConstructors = constructorSymbols.toArray(new ConstructorSymbol[0]);
            } catch (NotFoundException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return declaredConstructors;
    }

    public ConstructorSymbol[] getConstructors() {
        if (Objects.isNull(constructors)) {
            ConstructorSymbol[] allConstructorSymbols = Stream.concat(Arrays.stream(getHierarchy()), Stream.of(this))
                    .flatMap(Utils::extractRaw)
                    .flatMap(clazz -> Arrays.stream(clazz.getDeclaredConstructors()))
                    .toArray(ConstructorSymbol[]::new);

            List<ConstructorSymbol> hidden = Arrays.stream(allConstructorSymbols)
                    .filter(constructor -> constructor.getHidden().length > 0)
                    .flatMap(constructor -> Arrays.stream(constructor.getHidden()))
                    .toList();

            constructors = Arrays.stream(allConstructorSymbols)
                    .filter(constructor -> ! hidden.contains(constructor))
                    .toArray(ConstructorSymbol[]::new);
        }
        return constructors;
    }


    public MethodSymbol[] getDeclaredMethods() {
        if (Objects.isNull(declaredMethods)) {
            try {
                List<CtMethod> methods = Arrays
                        .stream(underlyingAlternative.getDeclaredMethods())
                        .filter(method -> !method.getName().startsWith("lambda$"))
                        .toList();

                final List<MethodSymbol> methodSymbols = new ArrayList<>();
                for (CtMethod declaredMethod : methods) {
                    Class<?>[] parameters = getParameters(declaredMethod.getParameterTypes());
                    final Method method = underlying.getDeclaredMethod(declaredMethod.getName(), parameters);
                    if (! method.isBridge()) {
                        methodSymbols.add(MethodSymbol.newInstance(method, this));
                    }
                }
                declaredMethods = methodSymbols.toArray(new MethodSymbol[0]);
            } catch (NotFoundException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return declaredMethods;
    }

    public MethodSymbol[] getMethods() {
        if (Objects.isNull(methods)) {
            MethodSymbol[] allMethodSymbols = Stream.concat(Arrays.stream(getHierarchy()), Stream.of(this))
                    .flatMap(Utils::extractRaw)
                    .flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods()))
                    .toArray(MethodSymbol[]::new);

            List<MethodSymbol> hidden = Arrays.stream(allMethodSymbols)
                    .filter(method -> method.getHidden().length > 0)
                    .flatMap(method -> Arrays.stream(method.getHidden()))
                    .toList();

            methods = Arrays.stream(allMethodSymbols)
                    .filter(method -> ! hidden.contains(method))
                    .toArray(MethodSymbol[]::new);
        }
        return methods;
    }

    public MethodSymbol[] getMethod(String name) {
        return Arrays.stream(getMethods())
                .filter(method -> method.getName().equals(name))
                .toArray(MethodSymbol[]::new);
    }


    public ClassSymbol[] getDeclaredClasses() {
        if (Objects.isNull(declaredClasses)) {
            try {
                final List<ClassSymbol> classSymbols = new ArrayList<>();
                for (CtClass declaredClass : underlyingAlternative.getDeclaredClasses()) {
                    final Class<?> aClass = Class.forName(declaredClass.getName());
                    classSymbols.add(ClassSymbol.newInstance(aClass, this));
                }
                declaredClasses = classSymbols.toArray(new ClassSymbol[0]);
            } catch (NotFoundException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return declaredClasses;
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return underlying.getDeclaredAnnotations();
    }

    @Override
    public Annotation[] getAnnotations() {
        if (Objects.isNull(annotations)) {
            annotations = Stream.concat(Stream.of(this), Arrays.stream(getHierarchy()))
                    .flatMap(Utils::extractRaw)
                    .flatMap(clazz -> Arrays.stream(clazz.getDeclaredAnnotations()))
                    .toArray(Annotation[]::new);
        }
        return annotations;
    }

    public Object[] getEnumConstants() {
        return underlying.getEnumConstants();
    }

    @Override
    public String toString() {
        return STR."\{getSimpleName()}";
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public static ClassSymbol newInstance(Class<?> underlying, NodeSymbol owner) {
        final ClassSymbol classSymbol = cache.getOrDefault(underlying, new ClassSymbol(underlying, owner));

        if (!cache.containsKey(underlying)) {
            cache.put(underlying, classSymbol);
        }
        return classSymbol;
    }

    private static Class<?>[] getParameters(CtClass[] parameters) {
        return Arrays
                .stream(parameters)
                .map(clazz -> {
                    try {
                        if (clazz.isPrimitive()) {
                            switch (clazz.getName()) {
                                case "int" -> {
                                    return int.class;
                                }
                                case "long" -> {
                                    return long.class;
                                }
                                case "double" -> {
                                    return double.class;
                                }
                                case "float" -> {
                                    return float.class;
                                }
                                case "short" -> {
                                    return short.class;
                                }
                                case "boolean" -> {
                                    return boolean.class;
                                }
                                case "byte" -> {
                                    return byte.class;
                                }
                                case "char" -> {
                                    return char.class;
                                }
                                default -> {
                                    throw new IllegalStateException("primitive not found: " + clazz.getName());
                                }
                            }
                        } else {
                            if (clazz.isArray()) {
                                return Class.forName(array(clazz));
                            } else {
                                return Class.forName(clazz.getName());
                            }

                        }
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray((IntFunction<Class<?>[]>) Class[]::new);
    }

    private static String array(CtClass componentType) {
        try {
            if (componentType.isPrimitive()) {
                return switch (componentType.getName()) {
                    case "short" -> "S";
                    case "long" -> "J";
                    case "int" -> "I";
                    case "char" -> "C";
                    case "float" -> "F";
                    case "double" -> "D";
                    case "byte" -> "B";
                    case "boolean" -> "Z";
                    default -> throw new IllegalStateException("Unexpected value: " + componentType.getName());
                };
            } else if (componentType.isArray()) {
                return "[" + array(componentType.getComponentType());
            } else {
                return "L" + componentType.getName() + ";";
            }
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
