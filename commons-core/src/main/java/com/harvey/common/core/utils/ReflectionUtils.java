package com.harvey.common.core.utils;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import com.harvey.common.core.context.Context;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

public class ReflectionUtils {

    private static final Paranamer paranamer = new CachingParanamer(new BytecodeReadingParanamer());

    public static String[] getMethodParameterNames(Method method) {
        return paranamer.lookupParameterNames(method);
    }

    public static Map<Method, String[]> doGetMethodsParameterNames(Method[] methods) {
        Map<Method, String[]> result = new LinkedHashMap<Method, String[]>();
        for (Method method : methods) {
            String[] names = getMethodParameterNames(method);
            result.put(method, names);
        }
        return result;
    }

    /**
     * Get methods and their parameter names declared by a class.
     * 
     * @param clazz
     * @return
     */
    public static Map<Method, String[]> getDeclaredMethodsParameterNames(Class<?> clazz) {
        return doGetMethodsParameterNames(clazz.getDeclaredMethods());
    }

    /**
     * Get methods and their parameter names declared by a class and its super
     * classes.
     * 
     * @param clazz
     * @return
     */
    public static Map<Method, String[]> getMethodsParameterNames(Class<?> clazz) {
        return doGetMethodsParameterNames(clazz.getMethods());
    }

    public static Type getActualTypeArgument(Class<?> clazz, TypeVariable<?> typeVariable) {
        GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
        if (!(genericDeclaration instanceof Class)) {
            return null;
        }
        TypeVariable<?>[] typeVariables = genericDeclaration.getTypeParameters();
        Type[] actualTypeArguments = null;
        Type[] interfaceTypes = clazz.getGenericInterfaces();
        for (Type interfaceType : interfaceTypes) {
            if (interfaceType instanceof ParameterizedType) {
                actualTypeArguments = ((ParameterizedType) interfaceType).getActualTypeArguments();
                break;
            }
        }
        if (actualTypeArguments == null) {
            throw new RuntimeException("Actual type arguments not found");
        }
        if (typeVariables.length != actualTypeArguments.length) {
            throw new RuntimeException("Wrong number of actual type arguments");
        }
        for (int i = 0; i < typeVariables.length; i++) {
            if (typeVariables[i] == typeVariable) {
                return actualTypeArguments[i];
            }
        }
        throw new RuntimeException("Actual type argument for " + typeVariable + " not found");
    }

    public static MethodInfo getMethodInfo(Method method) {
        Map<String, Class<?>> parameters = new LinkedHashMap<String, Class<?>>();
        Class<?>[] parameterTypes = method.getParameterTypes();
        try {
            String[] methodNames = paranamer.lookupParameterNames(method);
            for (int i = 0; i < methodNames.length; i++) {
                parameters.put(methodNames[i], parameterTypes[i]);
            }
        } catch (Exception ex) {
        }
        if (parameterTypes.length > 0 && parameters.size() == 0) {
            for (int i = 0; i < parameterTypes.length; i++) {
                parameters.put(i + "", parameterTypes[i]);
            }
        }
        return new MethodInfo(method, parameters);
    }

    public static List<MethodInfo> getMethodInfos(Class<?> type) {
        List<MethodInfo> methodInfos = new ArrayList<MethodInfo>();
        if (type.isInterface()) {
            recursiveAddInterfaceMethodsToList(type, methodInfos);
        } else {
            addDeclaredMethodsToList(type, methodInfos);
        }
        return methodInfos;
    }

    private static void addDeclaredMethodsToList(Class<?> type, List<MethodInfo> methodInfos) {
        Class nextClass = type;
        while (nextClass != null && nextClass != Object.class) {
            Method[] declaredMethods = nextClass.getDeclaredMethods();
            for (int i = 0, n = declaredMethods.length; i < n; i++) {
                Method method = declaredMethods[i];
                int modifiers = method.getModifiers();
                if (Modifier.isPrivate(modifiers))
                    continue;
                MethodInfo methodInfo = getMethodInfo(method);
                methodInfo.setIndex(methodInfos.size());
                methodInfos.add(methodInfo);
            }
            nextClass = nextClass.getSuperclass();
        }
    }

    private static void recursiveAddInterfaceMethodsToList(Class interfaceType, List<MethodInfo> methodInfos) {
        addDeclaredMethodsToList(interfaceType, methodInfos);
        for (Class nextInterface : interfaceType.getInterfaces()) {
            recursiveAddInterfaceMethodsToList(nextInterface, methodInfos);
        }
    }

    public static List<MethodInfo> getClazzMethodInfos(Class<?> type, Method... methods) {
        List<MethodInfo> methodInfos = new LinkedList<MethodInfo>();
        for (Method method : methods) {
            try {
                Method typeMethod = type.getMethod(method.getName(), method.getParameterTypes());
                MethodInfo mi = getMethodInfo(typeMethod);
                mi.setIndex(methodInfos.size());
                methodInfos.add(mi);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return methodInfos;
    }

    public static boolean hasMethod(Class<?> type, Method method) {
        try {
            Method typeMethod = type.getMethod(method.getName(), method.getParameterTypes());
            return typeMethod != null;
        } catch (Exception e) {
        }
        return false;
    }

    public static List<MethodInfo> getClazzMethodInfos(Class<?> type) {
        return getClazzMethodInfos(type, type.getDeclaredMethods());
    }

    public static Method[] getServiceMethods(Class<?> beanType, Class<?> serviceType) {
        Class<?> managerInterface = getServiceInterface(beanType, serviceType);
        return managerInterface == null ? null : managerInterface.getDeclaredMethods();
    }

    public static Class<?> getServiceInterface(Class<?> beanType, Class<?> serviceType) {
        Class<?> managerInterface = null;
        for (; serviceType.isAssignableFrom(beanType);) {
            Class<?>[] interfaces = beanType.getInterfaces();
            for (Class<?> interfaceClass : interfaces) {
                if (serviceType.isAssignableFrom(interfaceClass) && serviceType != interfaceClass) {
                    managerInterface = interfaceClass;
                    break;
                }
            }
            if (managerInterface != null) {
                break;
            }
            beanType = beanType.getSuperclass();
        }
        return managerInterface;
    }

    public static <T> List<T> getInstancesOfPackage(Class<T> baseType, String basePackage) {
        try {
            List<Resource> resources = ResourceUtils.getResources("classpath:/" + basePackage.replace('.', '/') + "/*.class");
            List<T> result = new ArrayList<T>();
            for (Resource resource : resources) {
                String className = resource.getFilename();
                if (StringUtils.isEmpty(className)) {
                    continue;
                }
                if (className.endsWith(".class")) {
                    className = className.substring(0, className.length() - ".class".length());
                }
                className = basePackage + "." + className;
                try {
                    Class<?> clazz = Class.forName(className);
                    if (!clazz.isInterface() && baseType.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                        T instance = (T) clazz.newInstance();
                        if (instance instanceof ApplicationContextAware) {
                            ((ApplicationContextAware) instance).setApplicationContext(Context.getContext());
                        }
                        if (instance instanceof InitializingBean) {
                            ((InitializingBean) instance).afterPropertiesSet();
                        }
                        result.add(instance);
                    }
                } catch (Exception ex) {
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
