package org.tracer.statement;

import javassist.*;
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
        // 插入前置逻辑
        StringBuffer beforeCodeSql = new StringBuffer();
        beforeCodeSql.append("this.startTime.set(java.lang.Long.valueOf(System.currentTimeMillis()));");
        method.insertBefore(beforeCodeSql.toString());


        // 插入后置逻辑
        StringBuffer afterCodeSql = new StringBuffer();
        afterCodeSql.append(getCodeBlock());
        afterCodeSql.append("long tempTime = ((Long) this.startTime.get()).longValue();\n");
        afterCodeSql.append("org.tracer.logger.LoggerUtil.info(\"[mysql][耗时] \"+ (System.currentTimeMillis() - tempTime) +\"毫秒 [sql]\"+ statementSql); ");
        method.insertAfter(afterCodeSql.toString());
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
