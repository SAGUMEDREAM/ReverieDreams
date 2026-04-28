package cc.thonly.reverie_dreams.util.math;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

@SuppressWarnings("unchecked")
@ToString
@EqualsAndHashCode
@Slf4j
public class VariableObject<T> {
    private T value;

    public VariableObject(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public T increment() {
        return add(1);
    }

    public T decrement() {
        return subtract(1);
    }

    public T add(Number other) {
        return value = operate(other, Operation.ADD);
    }

    public T subtract(Number other) {
        return value = operate(other, Operation.SUBTRACT);
    }

    public T multiply(Number other) {
        return value = operate(other, Operation.MULTIPLY);
    }

    public T divide(Number other) {
        return value = operate(other, Operation.DIVIDE);
    }

    private T operate(Number other, Operation op) {
        switch (value) {
            case Integer integer -> {
                int result = apply(((Number) (value)).intValue(), other.intValue(), op);
                return (T) Integer.valueOf(result);
            }
            case Long l -> {
                long result = apply(((Number) (value)).longValue(), other.longValue(), op);
                return (T) Long.valueOf(result);
            }
            case Float v -> {
                float result = apply(((Number) (value)).floatValue(), other.floatValue(), op);
                return (T) Float.valueOf(result);
            }
            case Double v -> {
                double result = apply(((Number) (value)).doubleValue(), other.doubleValue(), op);
                return (T) Double.valueOf(result);
            }
            case null, default ->
                    throw new UnsupportedOperationException("Unsupported type: " + (value != null ? value.getClass() : Object.class));
        }
    }

    private int apply(int a, int b, Operation op) {
        return switch (op) {
            case ADD -> a + b;
            case SUBTRACT -> a - b;
            case MULTIPLY -> a * b;
            case DIVIDE -> a / b;
        };
    }

    private long apply(long a, long b, Operation op) {
        return switch (op) {
            case ADD -> a + b;
            case SUBTRACT -> a - b;
            case MULTIPLY -> a * b;
            case DIVIDE -> a / b;
        };
    }

    private float apply(float a, float b, Operation op) {
        return switch (op) {
            case ADD -> a + b;
            case SUBTRACT -> a - b;
            case MULTIPLY -> a * b;
            case DIVIDE -> a / b;
        };
    }

    private double apply(double a, double b, Operation op) {
        return switch (op) {
            case ADD -> a + b;
            case SUBTRACT -> a - b;
            case MULTIPLY -> a * b;
            case DIVIDE -> a / b;
        };
    }

    public T mod(Number other) {
        return switch (value) {
            case Integer i -> (T) Integer.valueOf(i % other.intValue());
            case Long l -> (T) Long.valueOf(l % other.longValue());
            case Float f -> (T) Float.valueOf(f % other.floatValue());
            case Double d -> (T) Double.valueOf(d % other.doubleValue());
            default -> throw new UnsupportedOperationException("Unsupported type for mod: " + value.getClass());
        };
    }

    public T abs() {
        return switch (value) {
            case Integer i -> (T) Integer.valueOf(Math.abs(i));
            case Long l -> (T) Long.valueOf(Math.abs(l));
            case Float f -> (T) Float.valueOf(Math.abs(f));
            case Double d -> (T) Double.valueOf(Math.abs(d));
            default -> throw new UnsupportedOperationException("Unsupported type for abs: " + value.getClass());
        };
    }

    public T negate() {
        return switch (value) {
            case Integer i -> (T) Integer.valueOf(-i);
            case Long l -> (T) Long.valueOf(-l);
            case Float f -> (T) Float.valueOf(-f);
            case Double d -> (T) Double.valueOf(-d);
            default -> throw new UnsupportedOperationException("Unsupported type for negate: " + value.getClass());
        };
    }


    public int intValue() {
        return ((Number) value).intValue();
    }

    public long longValue() {
        return ((Number) value).longValue();
    }

    public float floatValue() {
        return ((Number) value).floatValue();
    }

    public double doubleValue() {
        return ((Number) value).doubleValue();
    }

    public boolean greaterThan(Number other) {
        return ((Number) value).doubleValue() > other.doubleValue();
    }

    public boolean lessThan(Number other) {
        return ((Number) value).doubleValue() < other.doubleValue();
    }

    public boolean equalsTo(Number other) {
        return Double.compare(((Number) value).doubleValue(), other.doubleValue()) == 0;
    }

    public boolean test(Predicate<T> predicate) {
        return predicate.test(value);
    }

    public <R> R map(java.util.function.Function<T, R> mapper) {
        return mapper.apply(value);
    }

    @Nullable
    public <R> R getTryCast(Class<R> rClass) {
        try {
            return (R) ((Object) value);
        } catch (Exception exception) {
            log.error("{0}", exception);
            return null;
        }
    }

    public void apply(java.util.function.Consumer<T> consumer) {
        consumer.accept(value);
    }

    public VariableObject<T> with(T newValue) {
        this.value = newValue;
        return this;
    }

    public static <T> VariableObject<T> of(T value) {
        return new VariableObject<>(value);
    }

    private enum Operation {
        ADD, SUBTRACT, MULTIPLY, DIVIDE
    }
}
