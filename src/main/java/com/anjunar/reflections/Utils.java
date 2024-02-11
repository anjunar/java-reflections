package com.anjunar.reflections;

import com.anjunar.reflections.types.*;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Stream;

public class Utils {

    public static void renderToConsole(Collection<ClassSymbol> symbols) {

        symbols.forEach(symbol -> {
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
            String modifiers = Utils.collection2(symbol.getModifier(), " ");
            String annotations = Utils.annotation(symbol.getAnnotations());
            String extendz;
            if (symbol.getSuperClass() == null) {
                extendz = "";
            } else {
                extendz = "extends " + symbol.getSuperClass();
            }
            String implementz;
            if (symbol.getInterfaces().length > 0) {
                implementz = "implements " + Utils.collection(symbol.getInterfaces(), ", ");
            } else {
                implementz = "";
            }
            System.out.println(STR."\{ modifiers }\{ annotations } \{symbol.getSimpleName()} \{extendz} \{implementz}");
            System.out.println(String.join("\n", Arrays.stream(symbol.getDeclaredMembers()).map(Object::toString).toList()));
        });

    }

    public static Stream<ClassSymbol> extractRaw(TypeSymbol clazz) {
        return switch (clazz) {
            case ParameterizeTypeSymbol symbol -> extractRaw(symbol.getType());
            case ClassSymbol symbol ->  Stream.of(symbol);
            default -> Stream.empty();
        };
    }

    public static ClassSymbol getRawType(TypeSymbol symbol) {
        return switch (symbol) {
            case ClassSymbol classSymbol -> classSymbol;
            case GenericArrayTypeSymbol genericArrayTypeSymbol -> getRawType(genericArrayTypeSymbol.getType());
            case ParameterizeTypeSymbol typeSymbol -> getRawType(typeSymbol.getType());
            case TypeVariableSymbol typeVariableSymbol -> throw new IllegalStateException("Type Variable cannot be a raw type");
            case WildcardTypeSymbol wildcardTypeSymbol -> getRawType(wildcardTypeSymbol.getLower()[0]);
            default -> throw new IllegalStateException("Unknow Type");
        };
    }

    public static String brackets(Object[] args) {
        String result = "";
        if (args.length > 0) {
            result += "<";
        }
        result += String.join(", ", Arrays.stream(args).map(Object::toString).toList());
        if (args.length > 0) {
            result += ">";
        }
        return result;
    }

    public static String annotation(Annotation[] args) {
        return String.join("", Arrays.stream(args).map(arg -> "@" + arg.annotationType().getSimpleName() + " ").toList());
    }


    public static String collection(Object[] args, String delimiter) {
        return String.join(delimiter, Arrays.stream(args).map(Object::toString).toList());
    }

    public static String collection2(Object[] args, String delimiter) {
        return String.join(delimiter, Arrays.stream(args).map(arg -> arg.toString().toLowerCase()).toList());
    }

}
