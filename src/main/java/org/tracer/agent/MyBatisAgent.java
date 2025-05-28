package org.tracer.agent;

import org.tracer.handler.ParamHandler;

import java.lang.instrument.Instrumentation;

public class MyBatisAgent {


    public static void premain(String args, Instrumentation inst) {
        System.out.println("MyBatis SQL Capture Agent 已加载！");

        // 解析参数
        ParamHandler.parseArgs(args);
        boolean canRetransform = inst.isRetransformClassesSupported() && inst.isRedefineClassesSupported();
        if (canRetransform) {
            System.out.println("当前环境支持 retransform，将启用");
        } else {
            System.out.println("当前环境不支持 retransform，仅监听新加载类");
        }
        inst.addTransformer(new SqlInterceptorUsingJavassist(), canRetransform);
    }

}
