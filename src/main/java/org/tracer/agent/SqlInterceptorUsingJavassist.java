package org.tracer.agent;

import org.tracer.statement.AbstractJavassist;
import org.tracer.statement.ClientPreparedStatementJavassist;
import org.tracer.statement.TSDBStatementJavassist;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

public class SqlInterceptorUsingJavassist implements ClassFileTransformer {

    // 代码增强
    private List<AbstractJavassist> javassistList = new ArrayList<AbstractJavassist>() {{
        add(new ClientPreparedStatementJavassist());
        add(new TSDBStatementJavassist());
    }};


    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {

        if (className == null) return null;

        // 我们要拦截的类名
        for (AbstractJavassist abstractJavassist : javassistList) {
            if (abstractJavassist.match(className)) {
                return abstractJavassist.instrument(loader, classfileBuffer);
            }
        }
        return null;
    }

}
