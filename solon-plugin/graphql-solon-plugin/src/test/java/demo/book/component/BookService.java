package demo.book.component;

import demo.book.dto.BookInputDTO;
import graphql.solon.annotation.QueryMapping;
import graphql.solon.constant.OperationType;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Param;

/**
 * @author fuzi1996
 * @since 2.3
 */
@Component
public class BookService {

    private BookInputDTO generateNewOne(String id) {
        BookInputDTO book = new BookInputDTO();
        book.setId(id);
        book.setName("book-1");
        book.setPageCount(1);
        book.setAuthorId("1");
        return book;
    }

    @QueryMapping
    public BookInputDTO bookById(@Param String id) {
        return this.generateNewOne(id);
    }

    @QueryMapping(typeName = OperationType.MUTATION)
    public BookInputDTO createBook(@Param BookInputDTO book) {
        return book;
    }
}
