package org.hibernate.solon.integration.schema;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * 自定义表命名策略
 * 
 * <p>提供多种表名生成策略：
 * - 下划线命名（user_info）
 * - 驼峰命名（userInfo）
 * - 前缀命名（t_user）
 * </p>
 * 
 * @author noear
 * @since 3.4
 */
public class TableNamingStrategy extends PhysicalNamingStrategyStandardImpl {
    
    private final NamingStrategyType strategyType;
    private final String tablePrefix;
    
    public TableNamingStrategy() {
        this(NamingStrategyType.UNDERSCORE, "");
    }
    
    public TableNamingStrategy(NamingStrategyType strategyType) {
        this(strategyType, "");
    }
    
    public TableNamingStrategy(NamingStrategyType strategyType, String tablePrefix) {
        this.strategyType = strategyType;
        this.tablePrefix = tablePrefix != null ? tablePrefix : "";
    }
    
    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        String tableName = name.getText();
        
        // 应用前缀
        if (!tablePrefix.isEmpty() && !tableName.startsWith(tablePrefix)) {
            tableName = tablePrefix + tableName;
        }
        
        // 应用命名策略
        switch (strategyType) {
            case UNDERSCORE:
                tableName = camelToUnderscore(tableName);
                break;
            case CAMEL:
                // 保持驼峰命名
                break;
            case UPPERCASE:
                tableName = tableName.toUpperCase();
                break;
            case LOWERCASE:
                tableName = tableName.toLowerCase();
                break;
        }
        
        return Identifier.toIdentifier(tableName);
    }
    
    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        String columnName = name.getText();
        
        // 应用命名策略
        switch (strategyType) {
            case UNDERSCORE:
                columnName = camelToUnderscore(columnName);
                break;
            case CAMEL:
                // 保持驼峰命名
                break;
            case UPPERCASE:
                columnName = columnName.toUpperCase();
                break;
            case LOWERCASE:
                columnName = columnName.toLowerCase();
                break;
        }
        
        return Identifier.toIdentifier(columnName);
    }
    
    /**
     * 驼峰转下划线
     */
    private String camelToUnderscore(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    /**
     * 命名策略类型
     */
    public enum NamingStrategyType {
        /**
         * 下划线命名（user_info）
         */
        UNDERSCORE,
        
        /**
         * 驼峰命名（userInfo）
         */
        CAMEL,
        
        /**
         * 大写命名（USER_INFO）
         */
        UPPERCASE,
        
        /**
         * 小写命名（user_info）
         */
        LOWERCASE
    }
}

