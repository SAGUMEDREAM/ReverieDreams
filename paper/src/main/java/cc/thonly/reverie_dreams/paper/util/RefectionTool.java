package cc.thonly.reverie_dreams.paper.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RefectionTool {

    public static final Map<String, Class> CLASSES = new Object2ObjectOpenHashMap<>();
    public static final Map<Class, Map<String, Map<String, Method>>> METHODS = new Object2ObjectOpenHashMap<>();
    public static final Map<Class, Map<String, Field>> FIELDS = new Object2ObjectOpenHashMap<>();

    public static <T> Class<T> getClass(String name) {
        return CLASSES.computeIfAbsent(name, _ -> {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <T> Method getMethod(Class<T> clazz, String method) {
        return getMethod(clazz, method, new Object[0]);
    }

    public static <T> Method getMethod(Class<T> clazz, String method, Object... args) {
        Map<String, Map<String, Method>> classCache =
                METHODS.computeIfAbsent(clazz, c -> new Object2ObjectOpenHashMap<>());

        String key = buildKey(args);

        Map<String, Method> overloads =
                classCache.computeIfAbsent(method, m -> new Object2ObjectOpenHashMap<>());

        return overloads.computeIfAbsent(key, k -> {
            try {
                Method[] methods = clazz.getDeclaredMethods();
                for (Method m : methods) {
                    if (!m.getName().equals(method)) continue;

                    Class<?>[] params = m.getParameterTypes();
                    if (params.length != args.length) continue;

                    boolean match = true;
                    for (int i = 0; i < params.length; i++) {
                        if (!isAssignable(params[i], args[i])) {
                            match = false;
                            break;
                        }
                    }

                    if (match) {
                        m.setAccessible(true);
                        return m;
                    }
                }
                throw new NoSuchMethodException("No method: " + method);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <T> Field getField(Class<T> clazz, String name) {
        return getField(clazz, name, null);
    }

    public static <T> Field getField(Class<T> clazz, String name, Object type) {
        Map<String, Field> classCache =
                FIELDS.computeIfAbsent(clazz, c -> new Object2ObjectOpenHashMap<>());

        String key = name + ":" + (type == null ? "any" : type.toString());

        return classCache.computeIfAbsent(key, k -> {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);

                if (type != null && !field.getType().equals(type)) {
                    throw new NoSuchFieldException("Field type mismatch: " + name);
                }

                return field;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <T> Field getFieldValue(Class<T> clazz, T object, String name) {
        return getField(clazz, name);
    }

    public static <T> Field getFieldValue(Class<T> clazz, T object, String name, Object type) {
        return getField(clazz, name, type);
    }

    public static <T> Field getFieldStaticValue(Class<T> clazz, T object, String name) {
        return getField(clazz, name);
    }

    public static <T> Field getFieldStaticValue(Class<T> clazz, T object, String name, Object type) {
        return getField(clazz, name, type);
    }

    private static String buildKey(Object... args) {
        if (args == null || args.length == 0) return "void";
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            if (arg == null) {
                sb.append("null;");
            } else {
                sb.append(arg.getClass().getName()).append(";");
            }
        }
        return sb.toString();
    }

    private static boolean isAssignable(Class<?> paramType, Object arg) {
        if (arg == null) return !paramType.isPrimitive();

        Class<?> argType = arg.getClass();

        if (paramType.isPrimitive()) {
            if (paramType == int.class) return argType == Integer.class;
            if (paramType == long.class) return argType == Long.class;
            if (paramType == double.class) return argType == Double.class;
            if (paramType == float.class) return argType == Float.class;
            if (paramType == boolean.class) return argType == Boolean.class;
            if (paramType == char.class) return argType == Character.class;
            if (paramType == byte.class) return argType == Byte.class;
            if (paramType == short.class) return argType == Short.class;
        }

        return paramType.isAssignableFrom(argType);
    }
}