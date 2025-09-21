package org.tracer.statement;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.tracer.handler.ParamHandler;

import java.io.FileOutputStream;
import java.io.IOException;

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

            // 1. 获取原始的ThreadLocal类（不带泛型）
            CtClass threadLocalClass = ClassPool.getDefault().get("java.lang.ThreadLocal");
            // 2. 创建字段
            CtField startTimeField = new CtField(threadLocalClass, "startTime", cc);
            startTimeField.setModifiers(Modifier.PRIVATE);

            // 3. 设置初始化表达式（使用原始类型）
            CtField.Initializer initializer = CtField.Initializer.byExpr(
                    "new java.lang.ThreadLocal() {\n" +
                            "    protected Object initialValue() {\n" +
                            "        return Long.valueOf(0L);\n" +
                            "    }\n" +
                            "}"
            );
            // 4. 添加字段
            cc.addField(startTimeField, initializer);

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
     * 保存class文件到磁盘
     */
    private void saveClassToFile(CtClass ctClass, byte[] bytecode) {
        try {
            String className = ctClass.getName().replace('.', '/');
            String outputPath = "./modified_classes/" + className + ".class";

            // 确保目录存在
            new java.io.File(outputPath).getParentFile().mkdirs();

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                fos.write(bytecode);
            }

            System.out.println("修改后的class已保存到: " + outputPath);

        } catch (IOException e) {
            System.err.println("保存class文件失败: " + e.getMessage());
        }
    }

    /**
     * 代码增强实现
     *
     * @param cc
     */
    protected abstract void instrumentCtClass(CtClass cc) throws NotFoundException, CannotCompileException;


}
