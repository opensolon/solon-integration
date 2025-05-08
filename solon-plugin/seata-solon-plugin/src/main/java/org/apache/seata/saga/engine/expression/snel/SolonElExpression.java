package org.apache.seata.saga.engine.expression.snel;

import org.apache.seata.saga.engine.expression.ELExpression;
import org.noear.solon.expression.context.StandardContext;
import org.noear.solon.expression.snel.SnEL;

public class SolonElExpression implements ELExpression {

    private String expression;

    public SolonElExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public Object getValue(Object elContext) {
        String realExpression = expression.replaceAll("#root", "root");
        return SnEL.eval(realExpression, new StandardContext(elContext));
    }

    @Override
    public void setValue(Object value, Object elContext) {

    }

    @Override
    public String getExpressionString() {
        return this.expression;
    }
}