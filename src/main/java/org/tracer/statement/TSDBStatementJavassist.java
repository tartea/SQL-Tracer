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
        CtMethod method = cc.getDeclaredMethod("execute");
        method.instrument(
                new ExprEditor() {
                    public void edit(MethodCall m) throws CannotCompileException {
                        m.replace(
                                "{\n" +
                                        "    try {\n" +
                                        "        $_ = $proceed($$);\n" +  // 调用原始方法并自动处理返回值
                                        "    } finally {\n" +
                                        "       org.tracer.logger.LoggerUtil.info(sql); \n" +
                                        "    }\n" +
                                        "}");
                    }
                }
        );
    }

}
