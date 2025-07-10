package org.tracer.handler;

import org.tracer.logger.LoggerUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 参数处理
 */
public class ParamHandler {

    private static Map<String, String> agentArgMap = new HashMap<>();

    static {
        // 0代表关闭，1代表开启
        // 是否控制台打印 第一位
        agentArgMap.put("outputToConsole", "0");
        // 文件覆盖  第二位
        agentArgMap.put("fileOverlay", "0");
    }

    /**
     * 解析参数（
     * 为了解决参数过长的问题，采用二进制的方式，目前采用8位，后续累加
     */
    public static void parseArgs(String agentArgs) {
        if (agentArgs != null && !agentArgs.isEmpty()) {
            String[] pairs = agentArgs.split(",");
            for (String pair : pairs) {
                String[] entry = pair.split("=", 2); // 最多分割成两部分
                if (entry.length == 2 && Objects.equals(entry[0], "tracer")) {
                    String value = entry[1].trim();
                    String binaryStr = String.format("%8s", Integer.toBinaryString(Integer.parseInt(value))).replace(' ', '0');
                    // 第一位
                    agentArgMap.put("outputToConsole", String.valueOf(binaryStr.charAt(binaryStr.length() - 1)));
                    // 第二位
                    agentArgMap.put("fileOverlay", String.valueOf(binaryStr.charAt(binaryStr.length() - 2)));
                }
            }
        }
    }

    /**
     * 加载各种组件
     */
    public static void loadAgent() {
        // 处理日志
        LoggerUtil.builder(agentArgMap);
    }

    /**
     * 获取打印参数
     *
     * @return
     */
    public static Boolean getOutputToConsole() {
        return Objects.equals(agentArgMap.get("outputToConsole"), "1");
    }


}
