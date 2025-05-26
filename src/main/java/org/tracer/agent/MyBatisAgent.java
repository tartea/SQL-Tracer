package org.tracer.agent;

import java.lang.instrument.Instrumentation;

public class MyBatisAgent {
    public static void premain(String args, Instrumentation inst) {
        System.out.println("MyBatis SQL Capture Agent 已加载！");

        if (args != null && !args.isEmpty()) {
            System.out.println("Received agent args: " + args);
            parseArgs(args);
        } else {
            System.out.println("No arguments provided.");
        }

        boolean canRetransform = inst.isRetransformClassesSupported() && inst.isRedefineClassesSupported();
        if (canRetransform) {
            System.out.println("当前环境支持 retransform，将启用");
        } else {
            System.out.println("当前环境不支持 retransform，仅监听新加载类");
        }

        inst.addTransformer(new SqlInterceptorUsingJavassist(), canRetransform);
    }


    /**
     * 解析参数（格式：key1=value1,key2=value2）
     */
    private static void parseArgs(String agentArgs) {
        String[] pairs = agentArgs.split(",");
        for (String pair : pairs) {
            String[] entry = pair.split("=", 2); // 最多分割成两部分
            if (entry.length == 2) {
                String key = entry[0].trim();
                String value = entry[1].trim();
                System.out.println("Key: " + key + ", Value: " + value);
            }
        }
    }
}
