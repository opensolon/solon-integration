package demo.product.dto;

import java.util.Date;

public class ProductPriceHistoryDTO {

    private Long id;
    private Date startDate;
    private int price;

    public ProductPriceHistoryDTO() {
    }

    public ProductPriceHistoryDTO(Long id, Date startDate, int price) {
        this.id = id;
        this.startDate = startDate;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
