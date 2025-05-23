package org.apache.seata.saga.engine.invoker;

import org.apache.seata.common.exception.FrameworkErrorCode;
import org.apache.seata.common.util.CollectionUtils;
import org.apache.seata.saga.engine.exception.EngineExecutionException;
import org.apache.seata.saga.engine.pcext.handlers.ServiceTaskStateHandler;
import org.apache.seata.saga.engine.utils.ExceptionUtils;
import org.apache.seata.saga.statelang.domain.ServiceTaskState;
import org.apache.seata.saga.statelang.domain.TaskState.Retry;
import org.apache.seata.saga.statelang.domain.impl.ServiceTaskStateImpl;
import org.apache.seata.saga.statelang.parser.JsonParser;
import org.apache.seata.saga.statelang.parser.JsonParserFactory;
import org.apache.seata.saga.util.BeanUtils;
import org.noear.solon.Solon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class SolonServiceInvoker implements ServiceInvoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolonServiceInvoker.class);

    private ThreadPoolExecutor threadPoolExecutor;
    private String sagaJsonParser;

    @Override
    public Object invoke(ServiceTaskState serviceTaskState, Object... input) throws Throwable {
        ServiceTaskStateImpl state = (ServiceTaskStateImpl) serviceTaskState;
        if (state.isAsync()) {
            if (threadPoolExecutor == null) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(
                            "threadPoolExecutor is null, Service[{}.{}] cannot execute asynchronously, executing "
                                    + "synchronously now. stateName: {}",
                            state.getServiceName(), state.getServiceMethod(), state.getName());
                }
                return doInvoke(state, input);
            }

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Submit Service[{}.{}] to asynchronously executing. stateName: {}", state.getServiceName(),
                        state.getServiceMethod(), state.getName());
            }
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        doInvoke(state, input);
                    } catch (Throwable e) {
                        LOGGER.error("Invoke Service[" + state.getServiceName() + "." + state.getServiceMethod() + "] failed.", e);
                    }
                }
            });
            return null;
        } else {
            return doInvoke(state, input);
        }
    }

    protected Object doInvoke(ServiceTaskStateImpl state, Object[] input) throws Throwable {

        Object bean = Solon.context().getBean(state.getServiceName());

        Method method = state.getMethod();
        if (method == null) {
            synchronized (state) {
                method = state.getMethod();
                if (method == null) {
                    method = findMethod(bean.getClass(), state.getServiceMethod(), state.getParameterTypes());
                    if (method != null) {
                        state.setMethod(method);
                    }
                }
            }
        }

        if (method == null) {
            throw new EngineExecutionException(
                    "No such method[" + state.getServiceMethod() + "] on BeanClass[" + bean.getClass() + "]",
                    FrameworkErrorCode.NoSuchMethod);

        }

        Object[] args = new Object[method.getParameterCount()];
        try {
            Class[] paramTypes = method.getParameterTypes();
            if (input != null && input.length > 0) {
                int len = input.length < paramTypes.length ? input.length : paramTypes.length;
                for (int i = 0; i < len; i++) {
                    args[i] = toJavaObject(input[i], paramTypes[i]);
                }
            }
        } catch (Exception e) {
            throw new EngineExecutionException(e,
                    "Input to java object error, Method[" + state.getServiceMethod() + "] on BeanClass[" + bean.getClass()
                            + "]", FrameworkErrorCode.InvalidParameter);
        }

        if (!Modifier.isPublic(method.getModifiers())) {
            throw new EngineExecutionException("Method[" + method.getName() + "] must be public",
                    FrameworkErrorCode.MethodNotPublic);
        }

        Map<Retry, AtomicInteger> retryCountMap = new HashMap<>();
        while (true) {
            try {
                return invokeMethod(bean, method, args);
            } catch (Throwable e) {
                Retry matchedRetryConfig = matchRetryConfig(state.getRetry(), e);
                if (matchedRetryConfig == null) {
                    throw e;
                }

                AtomicInteger retryCount = CollectionUtils.computeIfAbsent(retryCountMap, matchedRetryConfig,
                        key -> new AtomicInteger(0));
                if (retryCount.intValue() >= matchedRetryConfig.getMaxAttempts()) {
                    throw e;
                }

                double intervalSeconds = matchedRetryConfig.getIntervalSeconds();
                double backoffRate = matchedRetryConfig.getBackoffRate();
                long currentInterval = (long) (retryCount.intValue() > 0 ?
                        (intervalSeconds * backoffRate * retryCount.intValue() * 1000) : (intervalSeconds * 1000));

                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Invoke Service[" + state.getServiceName() + "." + state.getServiceMethod() + "] failed, will retry after "
                            + currentInterval + " millis, current retry count: " + retryCount.intValue(), e);
                }
                try {
                    Thread.sleep(currentInterval);
                } catch (InterruptedException e1) {
                    LOGGER.warn("Retry interval sleep error", e1);
                }
                retryCount.incrementAndGet();
            }
        }
    }

    private Retry matchRetryConfig(List<Retry> retryList, Throwable e) {
        if (CollectionUtils.isNotEmpty(retryList)) {
            for (Retry retryConfig : retryList) {
                List<String> exceptions = retryConfig.getExceptions();
                if (CollectionUtils.isEmpty(exceptions)) {
                    // Exceptions not configured, Match current exception if it is NetException.
                    if (ExceptionUtils.isNetException(e)) {
                        return retryConfig;
                    }
                } else {
                    List<Class<? extends Exception>> exceptionClasses = retryConfig.getExceptionClasses();
                    if (exceptionClasses == null) {
                        synchronized (retryConfig) {
                            exceptionClasses = retryConfig.getExceptionClasses();
                            if (exceptionClasses == null) {

                                exceptionClasses = new ArrayList<>(exceptions.size());
                                for (String expStr : exceptions) {

                                    Class<? extends Exception> expClass = null;
                                    try {
                                        expClass = (Class<? extends Exception>) ServiceTaskStateHandler.class
                                                .getClassLoader().loadClass(expStr);
                                    } catch (Exception e1) {

                                        LOGGER.warn("Cannot Load Exception Class by getClass().getClassLoader()", e1);

                                        try {
                                            expClass = (Class<? extends Exception>) Thread.currentThread()
                                                    .getContextClassLoader().loadClass(expStr);
                                        } catch (Exception e2) {
                                            LOGGER.warn(
                                                    "Cannot Load Exception Class by Thread.currentThread()"
                                                            + ".getContextClassLoader()",
                                                    e2);
                                        }
                                    }

                                    if (expClass != null) {
                                        exceptionClasses.add(expClass);
                                    }
                                }
                                retryConfig.setExceptionClasses(exceptionClasses);
                            }
                        }
                    }

                    for (Class<? extends Exception> expClass : exceptionClasses) {
                        if (expClass.isAssignableFrom(e.getClass())) {
                            return retryConfig;
                        }
                    }

                }
            }
        }
        return null;
    }

    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    protected Method findMethod(Class<?> clazz, String methodName, List<String> parameterTypes) {
        if (CollectionUtils.isEmpty(parameterTypes)) {
            return BeanUtils.findDeclaredMethodWithMinimalParameters(clazz, methodName);
        } else {
            Class[] paramClassTypes = new Class[parameterTypes.size()];
            for (int i = 0; i < parameterTypes.size(); i++) {
                paramClassTypes[i] = classForName(parameterTypes.get(i));
            }
            return BeanUtils.findDeclaredMethod(clazz, methodName, paramClassTypes);
        }
    }

    public static Method findMethodWithMinimalParameters(Method[] methods, String methodName)
            throws IllegalArgumentException {

        Method targetMethod = null;
        int numMethodsFoundWithCurrentMinimumArgs = 0;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                int numParams = method.getParameterCount();
                if (targetMethod == null || numParams < targetMethod.getParameterCount()) {
                    targetMethod = method;
                    numMethodsFoundWithCurrentMinimumArgs = 1;
                } else if (!method.isBridge() && targetMethod.getParameterCount() == numParams) {
                    if (targetMethod.isBridge()) {
                        // Prefer regular method over bridge...
                        targetMethod = method;
                    } else {
                        // Additional candidate with same length
                        numMethodsFoundWithCurrentMinimumArgs++;
                    }
                }
            }
        }
        if (numMethodsFoundWithCurrentMinimumArgs > 1) {
            throw new IllegalArgumentException("Cannot resolve method '" + methodName +
                    "' to a unique method. Attempted to resolve to overloaded method with " +
                    "the least number of parameters but there were " +
                    numMethodsFoundWithCurrentMinimumArgs + " candidates.");
        }
        return targetMethod;
    }

    protected Class classForName(String className) {
        Class clazz = getPrimitiveClass(className);
        if (clazz == null) {
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        if (clazz == null) {
            try {
                clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        if (clazz == null) {
            throw new EngineExecutionException("Parameter class not found [" + className + "]",
                    FrameworkErrorCode.ObjectNotExists);
        }
        return clazz;
    }

    protected Object invokeMethod(Object serviceBean, Method method, Object... input) throws Throwable {
        try {
            return method.invoke(serviceBean, input);
        } catch (InvocationTargetException e) {
            Throwable targetExp = e.getTargetException();
            if (targetExp == null) {
                throw new EngineExecutionException(e, e.getMessage(), FrameworkErrorCode.MethodInvokeError);
            }

            throw targetExp;
        }
    }

    protected Object toJavaObject(Object value, Class paramType) {
        if (value == null) {
            return value;
        }

        if (paramType.isAssignableFrom(value.getClass())) {
            return value;
        } else if (isPrimitive(paramType)) {
            return value;
        } else {
            JsonParser jsonParser = JsonParserFactory.getJsonParser(getSagaJsonParser());
            if (jsonParser == null) {
                throw new RuntimeException("Cannot get JsonParser by name : " + getSagaJsonParser());
            }
            String jsonValue = jsonParser.toJsonString(value, true, false);

            //compatible history autoType serialize json
            boolean useAutoType = jsonParser.useAutoType(jsonValue);

            return jsonParser.parse(jsonValue, paramType, !useAutoType);
        }
    }

    protected boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() //
                || clazz == Boolean.class //
                || clazz == Character.class //
                || clazz == Byte.class //
                || clazz == Short.class //
                || clazz == Integer.class //
                || clazz == Long.class //
                || clazz == Float.class //
                || clazz == Double.class //
                || clazz == BigInteger.class //
                || clazz == BigDecimal.class //
                || clazz == String.class //
                || clazz == java.util.Date.class //
                || clazz == java.sql.Date.class //
                || clazz == java.sql.Time.class //
                || clazz == java.sql.Timestamp.class //
                || clazz.isEnum() //
                ;
    }

    protected Class getPrimitiveClass(String className) {

        if (boolean.class.getName().equals(className)) {
            return boolean.class;
        } else if (char.class.getName().equals(className)) {
            return char.class;
        } else if (byte.class.getName().equals(className)) {
            return byte.class;
        } else if (short.class.getName().equals(className)) {
            return short.class;
        } else if (int.class.getName().equals(className)) {
            return int.class;
        } else if (long.class.getName().equals(className)) {
            return long.class;
        } else if (float.class.getName().equals(className)) {
            return float.class;
        } else if (double.class.getName().equals(className)) {
            return double.class;
        } else {
            return null;
        }
    }

    public String getSagaJsonParser() {
        return sagaJsonParser;
    }

    public void setSagaJsonParser(String sagaJsonParser) {
        this.sagaJsonParser = sagaJsonParser;
    }
}
