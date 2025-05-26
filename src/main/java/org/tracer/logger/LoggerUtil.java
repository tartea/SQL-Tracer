package org.tracer.logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerUtil {

    // 单例实例
    private static final LoggerUtil instance = new LoggerUtil("sql-tracer.log", false);
    private final BufferedWriter writer;
    private final boolean outputToConsole;

    // 日志级别枚举
    public enum Level {
        INFO, ERROR
    }

    /**
     * 私有构造器（单例模式）
     */
    private LoggerUtil(String logFilePath, boolean outputToConsole) {
        this.outputToConsole = outputToConsole;
        try {
            this.writer = new BufferedWriter(new FileWriter(logFilePath, true));

            // 注册关闭钩子（JVM 关闭前自动执行）
            Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize logger.", e);
        }
    }

    /**
     * 获取日志实例
     */
    public static LoggerUtil getInstance() {
        return instance;
    }

    /**
     * 写入日志
     */
    public void log(Level level, String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logMessage = String.format("[%s] [%s] %s%n", timestamp, level, message);

        if (outputToConsole) {
            System.out.print(logMessage);
        }

        try {
            writer.write(logMessage);
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }

    /**
     * 快捷方法：INFO 级别日志
     */
    public static void info(String message) {
        getInstance().log(Level.INFO, message);
    }

    /**
     * 快捷方法：ERROR 级别日志
     */
    public static void error(String message) {
        getInstance().log(Level.ERROR, message);
    }

    /**
     * 关闭日志资源（必须在程序退出前调用）
     */
    public void close() {
        try {
            writer.flush();  // 强制刷新缓冲区
            writer.close();
            System.out.println("ibatis Logger closed gracefully.");
        } catch (IOException e) {
            System.err.println("ibatis Error closing logger: " + e.getMessage());
        }
    }
}
