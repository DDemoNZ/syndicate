package com.task10.utils;

import com.task10.ApiHandler;
import com.task10.apiHandlers.BaseAPIHandler;
import javassist.bytecode.annotation.NoSuchClassError;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionUtils<T> {

    public static <T> Map<String, T> instantiate(Class<T> baseClass) {
//        return new Reflections(BaseAPIHandler.class.getPackage().getName())
//                .getSubTypesOf(baseClass).stream()
//                .map(ReflectionUtils::newInstance)
//                .collect(Collectors.toMap(BaseAPIHandler::getPathMatcher, ob-> ob));
        return new Reflections(ApiHandler.class.getPackage().getName())
                .getSubTypesOf(baseClass).stream()
                .map(ReflectionUtils::newInstance)
                .collect(Collectors.toMap(instance -> ((BaseAPIHandler) instance).getPathMatcher(), instance -> (T) instance));
    }

    private static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException
                 | IllegalAccessException | InvocationTargetException ex) {
            throw new NoSuchClassError(clazz.getName(), null);
        }
    }


}
