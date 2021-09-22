package org.example;


import org.apache.commons.logging.Log;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LogUtils {
    private static Log logger;
    private static boolean enable = false;

    public static void setLogger(Log logger) {
        LogUtils.logger = logger;
    }

    public static void setEnable(boolean enable) {
        LogUtils.enable = enable;
    }

    public static void log(CharSequence... charSequences) {
        if (enable) {
            logger.info(String.join(", ", charSequences));
        }
    }

    public static <T> Function<T, T> streamLog(String head) {
        return t -> {
            if (enable) {
                logger.info(head);
                logger.info(Objects.toString(t));
            }
            return t;
        };
    }
}
