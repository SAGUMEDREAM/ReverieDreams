package cc.thonly.reverie_dreams.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ResolverUtil {
    private static final MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();

    public static boolean tryInvokeMethod(Class<?> targetClass, Object object, String methodName, Class<?> paramType, Object paramValue) {
        try {
            String mappedClassName = resolver.mapClassName("intermediary", targetClass.getName());
            Class<?> mappedClass = Class.forName(mappedClassName);

            String mappedMethodName = resolver.mapMethodName("intermediary", mappedClassName, methodName, getMethodDescriptor(paramType));

            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            MethodHandle methodHandle = lookup.findVirtual(mappedClass, mappedMethodName, MethodType.methodType(void.class, paramType));

            methodHandle.invoke(object, paramValue);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getMethodDescriptor(Class<?> paramType) {
        return "(" + getDescriptor(paramType) + ")V";
    }

    private static String getDescriptor(Class<?> paramType) {
        if (paramType == int.class) return "I";
        if (paramType == byte.class) return "B";
        if (paramType == short.class) return "S";
        if (paramType == long.class) return "J";
        if (paramType == float.class) return "F";
        if (paramType == double.class) return "D";
        if (paramType == char.class) return "C";
        if (paramType == boolean.class) return "Z";
        if (paramType == void.class) return "V";
        return "L" + paramType.getName().replace('.', '/') + ";";
    }
}
