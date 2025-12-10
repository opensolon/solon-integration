package org.hibernate.solon.test.entity;

import org.hibernate.solon.annotation.CreatedDate;
import org.hibernate.solon.annotation.LastModifiedDate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品实体类 - 展示各种Hibernate注解的使用
 * 
 * <p>包含：索引、唯一约束、字符长度、精度、不为空等注解</p>
 * 
 * @author noear
 * @since 3.4
 */
@Entity
@Table(
    name = "product",
    // 表级索引
    indexes = {
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_status", columnList = "status"),
        // 复合索引
        @Index(name = "idx_product_category_status", columnList = "category_id,status")
    },
    // 唯一约束
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_code", columnNames = {"code"})
    }
)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    /**
     * 产品编码 - 唯一约束、不为空、固定长度
     */
    @Column(name = "code", nullable = false, length = 32, unique = true)
    private String code;
    
    /**
     * 产品名称 - 不为空、指定长度、带索引
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    /**
     * 产品描述 - 可为空、长文本
     */
    @Column(name = "description", length = 2000)
    @Lob
    private String description;
    
    /**
     * 价格 - 精度和标度、不为空
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    /**
     * 库存 - 不为空、默认值
     */
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;
    
    /**
     * 状态 - 不为空、枚举类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status = ProductStatus.DRAFT;
    
    /**
     * 分类ID - 外键、带索引
     */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;
    
    /**
     * 创建时间 - 自动填充、不为空、不可更新
     */
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    /**
     * 更新时间 - 自动更新、不为空
     */
    @LastModifiedDate
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
    
    /**
     * 是否删除 - 软删除标记
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public ProductStatus getStatus() {
        return status;
    }
    
    public void setStatus(ProductStatus status) {
        this.status = status;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public Boolean getDeleted() {
        return deleted;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
    
    /**
     * 产品状态枚举
     */
    public enum ProductStatus {
        DRAFT,      // 草稿
        PUBLISHED,  // 已发布
        ARCHIVED    // 已归档
    }
}

