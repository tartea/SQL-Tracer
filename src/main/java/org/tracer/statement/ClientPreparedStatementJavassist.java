package org.tracer.statement;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.Objects;

/**
 * mysql处理
 */
public class ClientPreparedStatementJavassist extends AbstractJavassist {

    private static final String CLIENT_NAME = "com/mysql/cj/jdbc/ClientPreparedStatement";

    @Override
    public boolean match(String className) {
        return Objects.equals(className, CLIENT_NAME);
    }

    @Override
    protected void instrumentCtClass(CtClass cc) throws NotFoundException, CannotCompileException {
        queryAgent(cc);
        updateAgent(cc);
        batchAgent(cc);
    }


    private void queryAgent(CtClass cc) throws NotFoundException, CannotCompileException {
        CtMethod method = cc.getDeclaredMethod("query");
        insertCode(method);
    }

    private void insertCode(CtMethod method) throws CannotCompileException {
        method.instrument(
                new ExprEditor() {
                    public void edit(MethodCall m) throws CannotCompileException {
                        m.replace(
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
                                        "}");
                    }
                }
        );
    }

    private void updateAgent(CtClass cc) throws NotFoundException, CannotCompileException {
        CtMethod method = cc.getDeclaredMethod("update");
        insertCode(method);
    }

    private void batchAgent(CtClass cc) throws NotFoundException, CannotCompileException {
        CtMethod method = cc.getDeclaredMethod("batch");
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
