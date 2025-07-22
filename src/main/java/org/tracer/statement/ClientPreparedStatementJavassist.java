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
        executeAgent(cc);
        addBatchAgent(cc);
    }


    private void executeAgent(CtClass cc) throws NotFoundException, CannotCompileException {
        CtMethod method = cc.getDeclaredMethod("execute");
        insertCode(method);
    }

    private void addBatchAgent(CtClass cc) throws NotFoundException, CannotCompileException {
        CtMethod method = cc.getDeclaredMethod("addBatch");
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
                                        " long l = System.currentTimeMillis() - start; \n" +
                                        "       org.tracer.logger.LoggerUtil.info(\"[mysql][耗时]\"+ l +\"毫秒 [sql ]\"+ statementSql); \n" +
                                        "    }\n" +
                                        "}");
                    }
                }
        );
    }

    /**
     * 获取处理sql的代码块
     *
     * @return
     */
    private String getCodeBlock() {
        return
                "       java.lang.String statementSql = this.toString();"
                        + "       int index = statementSql.indexOf(\"Statement:\"); "
                        + "       if(index != -1){ "
                        + "       statementSql = org.tracer.logger.SqlUtil.formatSql(statementSql.substring(index + 10)); } ";
    }

}
