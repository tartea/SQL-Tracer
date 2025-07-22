package org.tracer.handler;

import org.tracer.dto.AgentParam;
import org.tracer.logger.LoggerUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 参数处理
 */
public class ParamHandler {


    // 协议参数
    private static AgentParam agentParam = new AgentParam();

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
                    agentParam.setOutputToConsole(isValid(binaryStr.charAt(binaryStr.length() - 1)));
                    // 第二位
                    agentParam.setFileOverlay(isValid(binaryStr.charAt(binaryStr.length() - 2)));
                }
            }
        }
    }

    /**
     * 验证有效无效
     *
     * @return
     */
    private static boolean isValid(char var) {
        return Objects.equals(var, '1');
    }

    /**
     * 加载各种组件
     */
    public static void loadAgent() {
        // 处理日志
        LoggerUtil.builder(agentParam);
    }

    /**
     * 获取打印参数
     *
     * @return
     */
    public static Boolean getOutputToConsole() {
        return agentParam.isOutputToConsole();
    }


}
