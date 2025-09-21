package org.tracer.statement;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.Objects;

public class TSDBStatementJavassist extends AbstractJavassist {

    private static final String TSDB_NAME = "com/taosdata/jdbc/TSDBStatement";

    @Override
    public boolean match(String className) {
        return Objects.equals(className, TSDB_NAME);
    }

    @Override
    protected void instrumentCtClass(CtClass cc) throws NotFoundException, CannotCompileException {

        instrumentExecute(cc);
    }


    private void instrumentExecute(CtClass cc) throws NotFoundException, CannotCompileException {
        insertCode(cc, "execute");
        insertCode(cc, "executeQuery");
        insertCode(cc, "executeUpdate");
    }


    private void insertCode(CtClass cc, String methodName) throws CannotCompileException, NotFoundException {
        CtMethod method = cc.getDeclaredMethod(methodName);

        // 插入前置逻辑
        StringBuffer beforeCodeSql = new StringBuffer();
        beforeCodeSql.append("this.startTime.set(java.lang.Long.valueOf(System.currentTimeMillis()));");
        method.insertBefore(beforeCodeSql.toString());


        // 插入后置逻辑
        StringBuffer afterCodeSql = new StringBuffer();
        afterCodeSql.append("long tempTime = ((Long) this.startTime.get()).longValue();\n");
        afterCodeSql.append("org.tracer.logger.LoggerUtil.info(\"[TD][耗时] \"+ (System.currentTimeMillis() - tempTime) +\"毫秒 [sql]\"+ org.tracer.logger.SqlUtil.formatSql(sql))); ");
        method.insertAfter(afterCodeSql.toString());

    }


}
