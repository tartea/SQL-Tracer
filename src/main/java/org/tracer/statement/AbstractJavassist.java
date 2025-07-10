package org.tracer.statement;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.tracer.handler.ParamHandler;

public abstract class AbstractJavassist {

    /**
     * 匹配对应关系
     *
     * @param className
     * @return
     */
    public abstract boolean match(String className);

    /**
     * 代码增强
     */
    public byte[] instrument(ClassLoader loader, byte[] classfileBuffer) {
        try {
            ClassPool pool = ClassPool.getDefault();
            // 添加当前类加载器，确保能加载相关类
            pool.appendClassPath(new LoaderClassPath(loader));

            CtClass cc = pool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
            if (cc.isFrozen()) {
                cc.defrost();
            }

            // 代码增强
            instrumentCtClass(cc);

            byte[] byteCode = cc.toBytecode();
            cc.detach(); // 释放资源

            // 加载组件
            ParamHandler.loadAgent();

            return byteCode;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 代码增强实现
     *
     * @param cc
     */
    protected abstract void instrumentCtClass(CtClass cc) throws NotFoundException, CannotCompileException;


}
