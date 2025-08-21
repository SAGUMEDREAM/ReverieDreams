package cc.thonly.reverie_dreams.danmaku.script;

import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import javax.script.*;

@Slf4j
@Getter
public class DanmakuScriptNodeCondition {
    public static final Codec<DanmakuScriptNodeCondition> CODEC = Codec.STRING.xmap(
            DanmakuScriptNodeCondition::new,
            DanmakuScriptNodeCondition::getSource
    );
    private final String source;
    @Nullable
    private final CompiledScript expression;

    private DanmakuScriptNodeCondition(String source) {
        this.source = source;
        this.expression = this.tryCompile(source);
    }

    public boolean test(int tick) {
        if (this.expression == null) {
            log.warn("Expression is null for source: {}", source);
            return false;
        }
        try {
            Bindings bindings = this.expression.getEngine().createBindings();
            bindings.put("tick", tick);
            Object result = this.expression.eval(bindings);
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
            if (result instanceof Number) {
                return ((Number) result).doubleValue() != 0;
            }
            return false;
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    public CompiledScript tryCompile(String str) {
        try {
            return compileOrThrow(str);
        } catch (Exception err) {
            log.error("Can't compile str {}", str, err);
            return null;
        }
    }

    public CompiledScript compileOrThrow(String str) {
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

            String expr = str.replace("${{tick}}", "tick");

            if (!(engine instanceof Compilable compilable)) {
                throw new UnsupportedOperationException("Engine does not support compile");
            }

            return compilable.compile(expr);
        } catch (ScriptException e) {
            throw new RuntimeException("Compile failed: " + str, e);
        }
    }
}
