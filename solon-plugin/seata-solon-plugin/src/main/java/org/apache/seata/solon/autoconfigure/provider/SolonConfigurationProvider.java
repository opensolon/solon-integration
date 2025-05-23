/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.seata.solon.autoconfigure.provider;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.seata.common.exception.ShouldNeverHappenException;
import org.apache.seata.common.util.CollectionUtils;
import org.apache.seata.common.util.ReflectionUtil;
import org.apache.seata.config.Configuration;
import org.apache.seata.config.ExtConfigurationProvider;
import org.apache.commons.lang.StringUtils;
import org.noear.solon.Solon;
import org.noear.solon.core.AppClassLoader;
import org.noear.solon.core.util.ConvertUtil;
import org.noear.solon.lang.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.seata.common.util.StringFormatUtils.DOT;
import static org.apache.seata.solon.autoconfigure.StarterConstants.PROPERTY_BEAN_MAP;
import static org.apache.seata.solon.autoconfigure.StarterConstants.SEATA_PREFIX;
import static org.apache.seata.solon.autoconfigure.StarterConstants.SERVICE_PREFIX;
import static org.apache.seata.solon.autoconfigure.StarterConstants.SPECIAL_KEY_GROUPLIST;
import static org.apache.seata.solon.autoconfigure.StarterConstants.SPECIAL_KEY_SERVICE;
import static org.apache.seata.solon.autoconfigure.StarterConstants.SPECIAL_KEY_VGROUP_MAPPING;


public class SolonConfigurationProvider implements ExtConfigurationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolonConfigurationProvider.class);

    private static final String INTERCEPT_METHOD_PREFIX = "get";

    private static final Map<String, Object> PROPERTY_BEAN_INSTANCE_MAP = new ConcurrentHashMap<>(64);

    @Override
    public Configuration provide(Configuration originalConfiguration) {
        return (Configuration)Proxy.newProxyInstance(AppClassLoader.global(), new Class[]{Configuration.class}, (InvocationHandler) (proxy, method, args) -> {
            if (method.getName().startsWith(INTERCEPT_METHOD_PREFIX) && args.length > 0) {
                Object result;
                String rawDataId = (String)args[0];
                Class<?> dataType = ReflectionUtil.getWrappedClass(method.getReturnType());

                // 1. Get config value from the system property
                result = originalConfiguration.getConfigFromSys(rawDataId);

                if (result == null) {
                    String dataId = convertDataId(rawDataId);

                    // 2. Get config value from the springboot environment
                    result = getConfigFromEnvironment(dataId, dataType);
                    if (result != null) {
                        return result;
                    }

                    // 3. Get config defaultValue from the arguments
                    if (args.length > 1) {
                        result = args[1];

                        if (result != null) {
                            // See Configuration#getConfig(String dataId, long timeoutMills)
                            if (dataType.isAssignableFrom(result.getClass())) {
                                return result;
                            } else {
                                result = null;
                            }
                        }
                    }

                    // 4. Get config defaultValue from the property object
                    try {
                        result = getDefaultValueFromPropertyObject(dataId);
                    } catch (Throwable t) {
                        LOGGER.error("Get config '{}' default value from the property object failed:", dataId, t);
                    }
                }

                if (result != null) {
                    if (dataType.isAssignableFrom(result.getClass())) {
                        return result;
                    }

                    // Convert type
                    return this.convertType(result, dataType);
                }
            }

            return method.invoke(originalConfiguration, args);
        });
    }

    private Object getDefaultValueFromPropertyObject(String dataId) throws IllegalAccessException {
        String propertyPrefix = getPropertyPrefix(dataId);
        String propertySuffix = getPropertySuffix(dataId);

        // Get the property class
        final Class<?> propertyClass = PROPERTY_BEAN_MAP.get(propertyPrefix);
        if (propertyClass == null) {
            throw new ShouldNeverHappenException("PropertyClass for prefix: [" + propertyPrefix + "] should not be null.");
        }

        // Instantiate the property object
        Object propertyObj = CollectionUtils.computeIfAbsent(PROPERTY_BEAN_INSTANCE_MAP, propertyPrefix, k -> {
            try {
                return propertyClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.warn("PropertyClass for prefix: [" + propertyPrefix + "] should not be null. error :" + e.getMessage(), e);
            }
            return null;
        });
        Objects.requireNonNull(propertyObj, () -> "Instantiate the property object fail: " + propertyClass.getName());

        // Get defaultValue from the property object
        return getDefaultValueFromPropertyObject(propertyObj, propertySuffix);
    }

    /**
     * Get defaultValue from the property object
     *
     * @param propertyObj the property object
     * @param fieldName   the field name
     * @return defaultValue
     */
    @Nullable
    private Object getDefaultValueFromPropertyObject(Object propertyObj, String fieldName) throws IllegalAccessException {
        Optional<Field> fieldOptional = Stream.of(propertyObj.getClass().getDeclaredFields())
                .filter(f -> f.getName().equalsIgnoreCase(fieldName)).findAny();

        // Get defaultValue from the field
        if (fieldOptional.isPresent()) {
            Field field = fieldOptional.get();
            if (!Map.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                return field.get(propertyObj);
            }
        }

        return null;
    }

    /**
     * convert data id
     *
     * @param rawDataId
     * @return dataId
     */
    private String convertDataId(String rawDataId) {
        if (rawDataId.endsWith(SPECIAL_KEY_GROUPLIST)) {
            String suffix = StringUtils.removeStart(StringUtils.removeEnd(rawDataId, DOT + SPECIAL_KEY_GROUPLIST),
                    SPECIAL_KEY_SERVICE + DOT);
            // change the format of default.grouplist to grouplist.default
            return SERVICE_PREFIX + DOT + SPECIAL_KEY_GROUPLIST + DOT + suffix;
        }
        return SEATA_PREFIX + DOT + rawDataId;
    }

    /**
     * Get property prefix
     *
     * @param dataId
     * @return propertyPrefix
     */
    private String getPropertyPrefix(String dataId) {
        if (dataId.contains(SPECIAL_KEY_VGROUP_MAPPING)) {
            return SERVICE_PREFIX;
        }
        if (dataId.contains(SPECIAL_KEY_GROUPLIST)) {
            return SERVICE_PREFIX;
        }
        return StringUtils.substringBeforeLast(dataId, String.valueOf(DOT));
    }

    /**
     * Get property suffix
     *
     * @param dataId
     * @return propertySuffix
     */
    private String getPropertySuffix(String dataId) {
        if (dataId.contains(SPECIAL_KEY_VGROUP_MAPPING)) {
            return SPECIAL_KEY_VGROUP_MAPPING;
        }
        if (dataId.contains(SPECIAL_KEY_GROUPLIST)) {
            return SPECIAL_KEY_GROUPLIST;
        }
        return StringUtils.substringAfterLast(dataId, String.valueOf(DOT));
    }

    /**
     * get spring config
     *
     * @param dataId   data id
     * @param dataType data type
     * @return object
     */
    @Nullable
    private Object getConfigFromEnvironment(String dataId, Class<?> dataType) {
        String val = Solon.cfg().get(dataId);

        if (val == null) {
            return null;
        } else {
            return ConvertUtil.tryTo(dataType, val);
        }
    }

    private Object convertType(Object configValue, Class<?> dataType) {
        if (String.class.equals(dataType)) {
            return String.valueOf(configValue);
        }
        if (Long.class.equals(dataType)) {
            return Long.parseLong(String.valueOf(configValue));
        }
        if (Integer.class.equals(dataType)) {
            return Integer.parseInt(String.valueOf(configValue));
        }
        if (Short.class.equals(dataType)) {
            return Short.parseShort(String.valueOf(configValue));
        }
        if (Boolean.class.equals(dataType)) {
            return Boolean.parseBoolean(String.valueOf(configValue));
        }
        if (Duration.class.equals(dataType)) {
            return Duration.parse(String.valueOf(configValue));
        }
        return configValue;
    }

}
