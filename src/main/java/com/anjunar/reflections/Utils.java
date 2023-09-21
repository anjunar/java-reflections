package com.anjunar.reflections;

import com.anjunar.reflections.types.ClassSymbol;
import com.anjunar.reflections.types.ParameterizeTypeSymbol;
import com.anjunar.reflections.types.TypeResolver;
import com.anjunar.reflections.types.TypeSymbol;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

public class Utils {

    public static void renderToConsole(Collection<ClassSymbol> symbols) {

        symbols.forEach(symbol -> {
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
            System.out.println(symbol);
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

    public static String collection(Object[] args) {
        return String.join(", ", Arrays.stream(args).map(Object::toString).toList());
    }

}
