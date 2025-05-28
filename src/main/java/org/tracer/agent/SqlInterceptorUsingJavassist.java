package org.tracer.agent;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class SqlInterceptorUsingJavassist implements ClassFileTransformer {


    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {

        if (className == null) return null;

        // 我们要拦截的类名
        String targetClassName = "org/apache/ibatis/executor/statement/PreparedStatementHandler";

        if (!className.equals(targetClassName)) {
            return null;
        }

        try {
            ClassPool pool = ClassPool.getDefault();
            // 添加当前类加载器，确保能加载相关类
            pool.appendClassPath(new LoaderClassPath(loader));

            CtClass cc = pool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
            if (cc.isFrozen()) {
                cc.defrost();
            }

            queryAgent(cc);
            updateAgent(cc);

            byte[] byteCode = cc.toBytecode();
            cc.detach(); // 释放资源
            return byteCode;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void parameterizeAgent(CtClass cc) throws NotFoundException, CannotCompileException {
        CtMethod method = cc.getDeclaredMethod("parameterize");
        // 在方法入口插入日志
        String insertCode =
                "{ "
                        + "   try { "
                        + getCodeBlock()
                        + "       org.tracer.logger.LoggerUtil.info( \"[SQL 打印]: \" + statementSql); "
                        + "   } catch (Exception e) { "
                        + "       org.tracer.logger.LoggerUtil.error(\"⚠️ 获取 SQL 失败: \" + e.getMessage()); "
                        + "       e.printStackTrace(); "
                        + "   } "
                        + "} ";
        method.insertAfter(insertCode);
    }

    private void queryAgent(CtClass cc) throws NotFoundException, CannotCompileException {
        CtMethod method = cc.getDeclaredMethod("query");
        insertCode(method);
    }

    private void insertCode(CtMethod method) throws CannotCompileException {
        method.instrument(
                new ExprEditor() {
                    public void edit(MethodCall m) throws CannotCompileException {
                        m.replace(String.format(
                                "{\n" +
                                        getCodeBlock() +
                                        "    long start = System.currentTimeMillis();\n" +
                                        "    try {\n" +
                                        "        $_ = $proceed($$);\n" +  // 调用原始方法并自动处理返回值
                                        "    } finally {\n" +
                                        "       org.tracer.logger.LoggerUtil.info(statementSql); \n" +
                                        " long l = System.currentTimeMillis() - start; \n" +
                                        " if(l < 2000){ \n" +
                                        "       org.tracer.logger.LoggerUtil.info(\"[SQL 耗时] executed in \" + (System.currentTimeMillis() - start) + \" ms\");\n" +
                                        "}else { \n" +
                                        "org.tracer.logger.LoggerUtil.warn(\"[SQL 耗时] executed in \" + (System.currentTimeMillis() - start) + \" ms\"); \n" +
                                        " } \n" +
                                        "    }\n" +
                                        "}", "query"));
                    }
                }
        );
    }

    private void updateAgent(CtClass cc) throws NotFoundException, CannotCompileException {
        CtMethod method = cc.getDeclaredMethod("update");
        insertCode(method);
    }

    /**
     * 获取处理sql的代码块
     *
     * @return
     */
    private String getCodeBlock() {
        return
                "       java.lang.String statementSql = statement.toString();"
                        + "       int index = statement.toString().indexOf(\"Statement:\"); "
                        + "       if(index != -1){ "
                        + "       statementSql = statementSql.substring(index + 10).replaceAll(\"[\\\\r\\\\n\\\\s]+\", \" \"); } ";
    }
}
