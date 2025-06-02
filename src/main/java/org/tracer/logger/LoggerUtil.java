package org.tracer.logger;

import org.tracer.handler.ParamHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class LoggerUtil {

    // 单例实例
    public static LoggerUtil instance = null;
    private final BufferedWriter writer;
    // 定义 10MB 的字节数
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    // 日志级别枚举
    public enum Level {
        INFO, WARN
    }


    /**
     * 私有构造器（单例模式）
     */
    private LoggerUtil(String logFilePath) {

        try {
            // 文件存在，进行重命名
            File logFile = new File(logFilePath);

            // 如果文件已存在，进行重命名
            if (logFile.exists()) {
                long fileSize = logFile.length();
                if (fileSize > MAX_FILE_SIZE) {
                    String backupName = generateBackupFileName(logFile);
                    File backupFile = new File(backupName);
                    if (logFile.renameTo(backupFile)) {
                        System.out.println("Renamed existing log file to: " + backupName);
                    } else {
                        System.err.println("Failed to rename log file.");
                    }
                }
            }

            this.writer = new BufferedWriter(new FileWriter(logFilePath, true));

            // 注册关闭钩子（JVM 关闭前自动执行）
            Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize logger.", e);
        }
    }


    /**
     * 私有构造器（单例模式）
     */
    public static void builder(Map<String, String> agentArgs) {

        String logFilePath = agentArgs.get("logFilePath") != null ? agentArgs.get("logFilePath") : "sql-tracer.log";
        LoggerUtil.instance = new LoggerUtil(logFilePath);
    }

    /**
     * 获取日志实例
     */
    public static LoggerUtil getInstance() {
        return LoggerUtil.instance;
    }

    /**
     * 写入日志
     */
    public void log(Level level, String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logMessage = String.format("[%s] [%s] %s%n", timestamp, level, message);

        if (ParamHandler.getOutputToConsole()) {
            System.out.print(logMessage);
        }

        try {
            writer.write(logMessage);
            this.writer.flush();
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
    public static void warn(String message) {
        getInstance().log(Level.WARN, message);
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

    /**
     * 生成备份文件名（带时间戳）
     */
    private String generateBackupFileName(File logFile) {
        String prefix = logFile.getName();
        String parent = logFile.getParent();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int dotIndex = prefix.lastIndexOf('.');
        if (dotIndex > 0) {
            String baseName = prefix.substring(0, dotIndex);
            String extension = prefix.substring(dotIndex);
            return (parent == null ? "" : parent + File.separator) + baseName + "-" + timestamp + extension;
        } else {
            return (parent == null ? "" : parent + File.separator) + prefix + "-" + timestamp;
        }
    }
}
