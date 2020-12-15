package jgroups;

import org.jgroups.logging.CustomLogFactory;
import org.jgroups.logging.Log;

public class Log4jLoggerFactory implements CustomLogFactory {
    @Override
    public Log getLog(Class clazz) {
        return new Log4JLogger(org.apache.log4j.LogManager.getLogger(clazz));
    }

    @Override
    public Log getLog(String category) {
        return new Log4JLogger(org.apache.log4j.LogManager.getLogger(category));
    }
}
