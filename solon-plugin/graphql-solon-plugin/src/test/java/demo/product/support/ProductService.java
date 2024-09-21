package demo.product.support;

import demo.product.dto.ProductPriceHistoryDTO;
import graphql.solon.annotation.QueryMapping;
import graphql.solon.annotation.SubscriptionMapping;
import graphql.solon.constant.OperationType;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Param;
import reactor.core.publisher.Flux;

/**
 * @author fuzi1996
 * @since 2.3
 */
@Component
public class ProductService {

    private final Random rn = new Random();
    private final Map<Long, ProductPriceHistoryDTO> database = new ConcurrentHashMap<>();

    @QueryMapping(typeName = OperationType.MUTATION)
    public ProductPriceHistoryDTO addProduct(@Param("product") ProductPriceHistoryDTO product) {
        database.put(product.getId(), product);
        System.out.println("==== 添加");
        return product;
    }

    @QueryMapping(typeName = OperationType.MUTATION)
    public ProductPriceHistoryDTO removeProduct(@Param Long productId) {
        System.out.println("==== 删除");
        return database.remove(productId);
    }

    @SubscriptionMapping("notifyProductPriceChange")
    public Flux<ProductPriceHistoryDTO> notifyProductPriceChange(@Param Long productId) {

        // A flux is the publisher of data
        return Flux.fromStream(
            Stream.generate(() -> new ProductPriceHistoryDTO(productId, new Date(),
                rn.nextInt(10) + 1))).delayElements(Duration.ofSeconds(1));
    }
}
