package com.matchi.integration.events

import com.google.common.collect.Lists
import com.matchi.User
import com.matchi.UserBookingController
import com.matchi.api.BackhandSmashController
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

import java.util.stream.Collectors

abstract class Initiator {
    final String type;
    final String id;

    Initiator(String type, String id) {
        this.type = type;
        this.id = id;
    }
}

class UserInitiator extends Initiator {
    UserInitiator(String id) {
        super("user", id);
    }
}

class SystemInitiator extends Initiator {
    SystemInitiator(String name) {
        super("system", name);
    }
}

class InitiatorProvider {
    static log = LoggerFactory.getLogger(InitiatorProvider.class)

    static final List<String> systemPrefixes = Lists.asList(
            "com.matchi.jobs",
            BackhandSmashController.class.getName(),
            UserBookingController.class.getName()
    )

    static Initiator from(User user) {
        if (user != null) {
            return new UserInitiator(user.id.toString())
        } else return fromStack(filteredReversedStacktrace())
    }

    protected static Initiator fromStack(List<StackTraceElement> stack) {
        for (s in stack) {
            if (startsWithAny(systemPrefixes, s)) {
                return new SystemInitiator(getClassName(s))
            }
        }
        def trace = new Throwable().getStackTrace()
        log.error("trace ${trace}")
        log.info("Failed to find initator from filtered stack ${stack}")
        return new UserInitiator("unknown")
    }

    protected static String getClassName(StackTraceElement s) {
        return StringUtils.substringBefore(s.getFileName(), ".")
    }

    protected static boolean startsWithAny(List<String> prefixes, StackTraceElement s) {
        for (String prefix : prefixes) {
            if (StringUtils.startsWith(s.getClassName(), prefix)) {
                return true
            }
        }
        return false
    }

    protected static List<StackTraceElement> filteredReversedStacktrace(List<StackTraceElement> stack) {
        return stack
                .stream()
                .filter({ s -> s.getClassName().startsWith("com.matchi") })
                .collect(Collectors.toList())
                .reverse()
    }

    protected static List<StackTraceElement> filteredReversedStacktrace() {
        return filteredReversedStacktrace(Arrays.asList(new Throwable().getStackTrace()))
    }
}