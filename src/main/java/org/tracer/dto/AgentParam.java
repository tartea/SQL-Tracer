package org.tracer.dto;

/**
 * java agent参数
 */
public class AgentParam {

    /**
     * 日志路径
     */
    private String logFilePath = "sql-tracer.log";
    /**
     * 是否控制台打印
     */
    private boolean outputToConsole ;
    /**
     * 文件覆盖
     */
    private boolean fileOverlay;

    public boolean isOutputToConsole() {
        return outputToConsole;
    }

    public void setOutputToConsole(boolean outputToConsole) {
        this.outputToConsole = outputToConsole;
    }

    public boolean isFileOverlay() {
        return fileOverlay;
    }

    public void setFileOverlay(boolean fileOverlay) {
        this.fileOverlay = fileOverlay;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }
}
