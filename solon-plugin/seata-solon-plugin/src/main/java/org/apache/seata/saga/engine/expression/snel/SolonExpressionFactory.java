package org.apache.seata.saga.engine.expression.snel;

import org.apache.seata.saga.engine.expression.Expression;
import org.apache.seata.saga.engine.expression.ExpressionFactory;

public class SolonExpressionFactory implements ExpressionFactory {
    @Override
    public Expression createExpression(String expression) {
        SolonElExpression solonElExpression = new SolonElExpression(expression);
        return solonElExpression;
    }
}
