package cc.thonly.polymer.logger;

import lombok.experimental.Delegate;
import org.slf4j.Logger;
import org.slf4j.Marker;

public class MyFilteredLogger implements Logger {

    @Delegate(types = Logger.class)
    private final Logger parent;

    public MyFilteredLogger(Logger parent) {
        this.parent = parent;
    }

    private boolean shouldMute(String msg, Object... args) {
        if (msg == null) return false;

        if (msg.contains("Missing textures in model") ||
                msg.contains("Missing texture references")) {

            if (args != null) {
                for (Object a : args) {
                    if (a != null && a.toString().contains("polymerify")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void warn(String msg, Object... args) {
        if (!shouldMute(msg, args)) {
            parent.warn(msg, args);
        }
    }

    // ---------------------------
    // 其它 warn 重载全部转发到上面
    // ---------------------------
    @Override
    public void warn(String msg) {
        warn(msg, (Object[]) null);
    }

    @Override
    public void warn(String msg, Object arg) {
        warn(msg, new Object[]{arg});
    }

    @Override
    public void warn(String msg, Object arg1, Object arg2) {
        warn(msg, new Object[]{arg1, arg2});
    }

    @Override
    public void warn(String msg, Throwable t) {
        warn(msg, new Object[]{t});
    }

    @Override
    public void warn(Marker marker, String msg) {
        warn(msg);
    }

    @Override
    public void warn(Marker marker, String msg, Object arg) {
        warn(msg, arg);
    }

    @Override
    public void warn(Marker marker, String msg, Object arg1, Object arg2) {
        warn(msg, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String msg, Object... args) {
        warn(msg, args);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        warn(msg, t);
    }

    // 其余 logger 方法全部默认委托
    @Override public String getName() { return parent.getName(); }
    @Override public boolean isTraceEnabled() { return parent.isTraceEnabled(); }
    @Override public void trace(String msg) { parent.trace(msg); }
    @Override public void trace(String msg, Object o) { parent.trace(msg, o); }
    @Override public void trace(String msg, Object o, Object o1) { parent.trace(msg, o, o1); }
    @Override public void trace(String msg, Object... args) { parent.trace(msg, args); }
    @Override public void trace(String msg, Throwable throwable) { parent.trace(msg, throwable); }
    @Override public boolean isDebugEnabled() { return parent.isDebugEnabled(); }
    @Override public void debug(String msg) { parent.debug(msg); }
    @Override public void debug(String msg, Object o) { parent.debug(msg, o); }
    @Override public void debug(String msg, Object o, Object o1) { parent.debug(msg, o, o1); }
    @Override public void debug(String msg, Object... args) { parent.debug(msg, args); }
    @Override public void debug(String msg, Throwable throwable) { parent.debug(msg, throwable); }
    @Override public boolean isInfoEnabled() { return parent.isInfoEnabled(); }
    @Override public void info(String s) { parent.info(s); }
    @Override public void info(String s, Object o) { parent.info(s, o); }
    @Override public void info(String s, Object o, Object o1) { parent.info(s, o, o1); }
    @Override public void info(String s, Object... objects) { parent.info(s, objects); }
    @Override public void info(String s, Throwable throwable) { parent.info(s, throwable); }
    @Override public boolean isWarnEnabled() { return parent.isWarnEnabled(); }
    @Override public boolean isErrorEnabled() { return parent.isErrorEnabled(); }
    @Override public void error(String msg) { parent.error(msg); }
    @Override public void error(String msg, Object o) { parent.error(msg, o); }
    @Override public void error(String msg, Object o, Object o1) { parent.error(msg, o, o1); }
    @Override public void error(String msg, Object... args) { parent.error(msg, args); }
    @Override public void error(String msg, Throwable throwable) { parent.error(msg, throwable); }
}
