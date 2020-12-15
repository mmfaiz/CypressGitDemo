package jgroups;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jgroups.logging.Log;

import java.util.Arrays;
import java.util.IllegalFormatException;

public class Log4JLogger implements Log {
    private final Logger loj4jLogger;

    public Log4JLogger(Logger logger) {
        this.loj4jLogger = logger;
    }

    @Override
    public boolean isFatalEnabled() {
        return loj4jLogger.isEnabledFor(Level.FATAL);
    }

    @Override
    public boolean isErrorEnabled() {
        return loj4jLogger.isEnabledFor(Level.ERROR);
    }

    @Override
    public boolean isWarnEnabled() {
        return loj4jLogger.isEnabledFor(Level.WARN);
    }

    @Override
    public boolean isInfoEnabled() {
        return loj4jLogger.isEnabledFor(Level.INFO);
    }

    @Override
    public boolean isDebugEnabled() {
        return loj4jLogger.isEnabledFor(Level.DEBUG);
    }

    @Override
    public boolean isTraceEnabled() {
        return loj4jLogger.isEnabledFor(Level.TRACE);
    }

    @Override
    public void fatal(String msg) {
        loj4jLogger.fatal(msg);
    }

    @Override
    public void fatal(String msg, Object... args) {
        loj4jLogger.fatal(format(msg, args));
    }

    @Override
    public void fatal(String msg, Throwable throwable) {
        loj4jLogger.fatal(msg, throwable);
    }

    @Override
    public void error(String msg) {
        loj4jLogger.error(msg);
    }

    @Override
    public void error(String msg, Object... args) {
        loj4jLogger.error(format(msg, args));

    }

    @Override
    public void error(String msg, Throwable throwable) {
        loj4jLogger.error(msg, throwable);
    }

    @Override
    public void warn(String msg) {
        loj4jLogger.warn(msg);
    }

    @Override
    public void warn(String msg, Object... args) {
        loj4jLogger.warn(format(msg, args));

    }

    @Override
    public void warn(String msg, Throwable throwable) {
        loj4jLogger.warn(msg, throwable);
    }

    @Override
    public void info(String msg) {
        loj4jLogger.info(msg);
    }

    @Override
    public void info(String msg, Object... args) {
        loj4jLogger.info(format(msg, args));
    }

    @Override
    public void debug(String msg) {
        loj4jLogger.debug(msg);
    }

    @Override
    public void debug(String msg, Object... args) {
        loj4jLogger.debug(format(msg, args));

    }

    @Override
    public void debug(String msg, Throwable throwable) {
        loj4jLogger.debug(msg, throwable);
    }

    @Override
    public void trace(Object msg) {
        loj4jLogger.trace(msg);
    }

    @Override
    public void trace(String msg) {
        loj4jLogger.trace(msg);
    }

    @Override
    public void trace(String msg, Object... args) {
        loj4jLogger.trace(format(msg, args));

    }

    @Override
    public void trace(String msg, Throwable throwable) {
        loj4jLogger.trace(msg, throwable);
    }

    @Override
    public void setLevel(String level) {
        loj4jLogger.setLevel(strToLevel(level));
    }

    @Override
    public String getLevel() {
        Level level = loj4jLogger.getLevel();
        return level != null ? level.toString() : "off";
    }


    protected static Level strToLevel(String level) {
        if (level == null) return null;
        level = level.toLowerCase().trim();
        if (level.equals("fatal")) return Level.FATAL;
        if (level.equals("error")) return Level.ERROR;
        if (level.equals("warn")) return Level.WARN;
        if (level.equals("warning")) return Level.WARN;
        if (level.equals("info")) return Level.INFO;
        if (level.equals("debug")) return Level.DEBUG;
        if (level.equals("trace")) return Level.TRACE;
        return null;
    }

    protected String format(String format, Object... args) {
        try {
            return String.format(format, args);
        } catch (IllegalFormatException ex) {
            error("Illegal format string \"" + format + "\", args=" + Arrays.toString(args));
        } catch (Throwable t) {
            error("Failure formatting string: format string=" + format + ", args=" + Arrays.toString(args));
        }
        return format;
    }
}

