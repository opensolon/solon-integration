package org.hibernate.solon.integration;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.noear.solon.Solon;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.Props;
import org.noear.solon.core.VarHolder;
import org.noear.solon.core.util.ResourceUtil;

import javax.persistence.EntityManagerFactory;
import javax.security.auth.login.Configuration;
import javax.sql.DataSource;

/**
 * @author lingkang
 * @since 2.5
 */
public class HibernateAdapter {
    protected BeanWrap dsWrap;
    protected Props dsProps;

    protected HibernateConfiguration configuration;

    public HibernateAdapter(BeanWrap dsWrap) {
        this(dsWrap, Solon.cfg().getProp("jpa"));
    }

    public HibernateAdapter(BeanWrap dsWrap, Props dsProps) {
        this.dsWrap = dsWrap;
        this.dsProps = dsProps;

        DataSource dataSource = getDataSource();

        configuration = new HibernateConfiguration();
        configuration.setDataSource(dataSource);

        initConfiguration();

        initDo();
    }

    protected DataSource getDataSource() {
        return dsWrap.raw();
    }

    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = getConfiguration().buildSessionFactory();
        }

        return sessionFactory;
    }

    public HibernateConfiguration getConfiguration() {
        return configuration;
    }


    /**
     * @author bai
     * */
    protected void initConfiguration() {
        // 默认兼容 hibernate.cfg.xml
        if (ResourceUtil.hasResource(null, StandardServiceRegistryBuilder.DEFAULT_CFG_RESOURCE_NAME)){
            configuration.configure(StandardServiceRegistryBuilder.DEFAULT_CFG_RESOURCE_NAME );
        }
        // 加载hibernate常规设置
        getConfiguration().setProperties(this.dsProps.getProp("properties"));
    }

    protected void initDo() {
        //for mappers section
        dsProps.forEach((k, v) -> {
            if (k instanceof String && v instanceof String) {
                String key = (String) k;
                String valStr = (String) v;

                if (key.startsWith("mappings[") || key.equals("mappings")) {
                    for (String val : valStr.split(",")) {
                        val = val.trim();
                        if (val.length() == 0) {
                            continue;
                        }
                        getConfiguration().addMapping(val);
                    }
                }
            }
        });
        
        // 根据hbm2ddl.auto配置自动执行DDL
        executeAutoDdl();
    }
    
    /**
     * 根据配置自动执行DDL
     */
    private void executeAutoDdl() {
        String ddlAuto = dsProps.get("properties.hibernate.hbm2ddl.auto", "");
        
        if (ddlAuto == null || ddlAuto.isEmpty()) {
            return;
        }
        
        // 延迟执行，等待所有实体类注册完成
        org.noear.solon.Solon.app().onEvent(org.noear.solon.core.event.AppLoadEndEvent.class, e -> {
            SchemaManager schemaManager = getSchemaManager();
            
            switch (ddlAuto.toLowerCase()) {
                case "create":
                    schemaManager.createSchema(false);
                    break;
                case "create-drop":
                    schemaManager.createSchema(false);
                    // 注意：create-drop需要在应用关闭时执行drop，这里只执行create
                    break;
                case "update":
                    schemaManager.updateSchema();
                    break;
                case "validate":
                    SchemaManager.SchemaValidationResult result = schemaManager.validateSchema();
                    if (!result.isValid()) {
                        throw new RuntimeException("Schema验证失败: " + result.getMessage());
                    }
                    break;
                case "none":
                default:
                    // 不执行任何操作
                    break;
            }
        });
    }
    
    /**
     * 获取Schema管理器
     * 
     * @return Schema管理器
     */
    public org.hibernate.solon.integration.schema.SchemaManager getSchemaManager() {
        // 使用Configuration方式，因为需要实体类信息
        return new org.hibernate.solon.integration.schema.SchemaManager(
            getConfiguration(),
            getDataSource(),
            getConfiguration().getProperties()
        );
    }
    
    /**
     * 获取DDL生成器
     * 
     * @return DDL生成器
     */
    public org.hibernate.solon.integration.schema.DdlGenerator getDdlGenerator() {
        // 使用Configuration方式，因为需要实体类信息
        return new org.hibernate.solon.integration.schema.DdlGenerator(
            getConfiguration(),
            getConfiguration().getProperties()
        );
    }

    protected void injectTo(VarHolder vh) {
        if (SessionFactory.class.isAssignableFrom(vh.getType())) {
            vh.setValue(getSessionFactory());
        }

        if (Configuration.class.isAssignableFrom(vh.getType())) {
            vh.setValue(getConfiguration());
        }

        if (EntityManagerFactory.class.isAssignableFrom(vh.getType())) {
            vh.setValue(getSessionFactory());
        }
    }
}