package graphql.solon.annotation;

import graphql.schema.DataFetcher;
import graphql.solon.fetcher.DataFetcherWrap;
import java.lang.reflect.Method;
import org.apache.commons.lang3.StringUtils;
import org.noear.solon.Utils;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.BeanWrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author fuzi1996
 * @since 2.3
 */
public class SubscriptionMappingAnnoHandler extends
    BaseSchemaMappingAnnoHandler<SubscriptionMapping> {

    private static Logger log = LoggerFactory.getLogger(SubscriptionMappingAnnoHandler.class);

    public SubscriptionMappingAnnoHandler(AppContext context) {
        super(context);
    }

    @Override
    public void doExtract(BeanWrap wrap, Method method, SubscriptionMapping schemaMapping)
        throws Throwable {
        String typeName = this.getTypeName(wrap, method, schemaMapping);
        String fieldName = this.getFieldName(wrap, method, schemaMapping);

        DataFetcher<Object> dataFetcher = this.getDataFetcher(context, wrap, method);
        DataFetcherWrap fetcherWrap = new DataFetcherWrap(typeName, fieldName, dataFetcher);
        log.debug("扫描到 typeName: [{}],fieldName: [{}] 的 SchemaMappingDataFetcher", typeName,
            fieldName);
        this.wrapList.add(fetcherWrap);
    }

    @Override
    String getTypeName(BeanWrap wrap, Method method,
        SubscriptionMapping schemaMapping) {
        return schemaMapping.typeName();
    }

    @Override
    String getFieldName(BeanWrap wrap, Method method,
        SubscriptionMapping schemaMapping) {
        String fieldName = Utils.annoAlias(schemaMapping.name(), schemaMapping.value());

        if (StringUtils.isBlank(fieldName)) {
            // 注解没标就使用方法名
            fieldName = method.getName();
        }
        return fieldName;
    }

    @Override
    protected boolean isSubscription() {
        return true;
    }
}
