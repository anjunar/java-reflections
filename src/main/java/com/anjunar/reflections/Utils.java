package com.anjunar.reflections;

import com.anjunar.reflections.types.ClassSymbol;
import com.anjunar.reflections.types.ParameterizeTypeSymbol;
import com.anjunar.reflections.types.TypeSymbol;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Stream;

public class Utils {

    public static void renderToConsole(Collection<ClassSymbol> symbols) {

        symbols.forEach(symbol -> {
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
            System.out.println(STR."\{Utils.collection(symbol.getModifier(), " ")}\{Utils.collection(symbol.getAnnotations(), " ")} \{symbol}");
            System.out.println(String.join("\n", Arrays.stream(symbol.getDeclaredMembers()).map(Object::toString).toList()));
        });

    }

    public static Stream<ClassSymbol> extracted(TypeSymbol clazz) {
        return switch (clazz) {
            case ParameterizeTypeSymbol symbol -> extracted(symbol.getType());
            case ClassSymbol symbol ->  Stream.of(symbol);
            default -> Stream.empty();
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

    public static String collection(Annotation[] args) {
        return String.join(", ", Arrays.stream(args).map(arg -> "@" + arg.annotationType().getSimpleName() + " ").toList());
    }


    public static String collection(Object[] args, String delimiter) {
        return String.join(delimiter, Arrays.stream(args).map(Object::toString).toList());
    }


}
