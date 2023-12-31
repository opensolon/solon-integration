package demo.product.support;

import demo.product.dto.ProductPriceHistoryDTO;
import graphql.solon.annotation.SubscriptionMapping;
import java.util.Date;
import java.util.Random;
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

    @SubscriptionMapping("notifyProductPriceChange")
    public Flux<ProductPriceHistoryDTO> notifyProductPriceChange(@Param Long productId) {

        // A flux is the publisher of data
        return Flux.fromStream(
            Stream.generate(() -> {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                return new ProductPriceHistoryDTO(productId, new Date(),
                    (int) (rn.nextInt(10) + 1 + productId));
            }));

    }
}
