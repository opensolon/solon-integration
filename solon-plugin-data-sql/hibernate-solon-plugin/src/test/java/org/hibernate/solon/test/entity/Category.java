package org.hibernate.solon.test.entity;

import org.hibernate.solon.annotation.CreatedDate;
import org.hibernate.solon.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 分类实体类 - 展示更多Hibernate注解特性
 * 
 * @author noear
 * @since 3.4
 */
@Entity
@Table(
    name = "category",
    indexes = {
        @Index(name = "idx_category_parent", columnList = "parent_id"),
        @Index(name = "idx_category_slug", columnList = "slug")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_slug", columnNames = {"slug"})
    }
)
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    /**
     * 分类名称 - 不为空
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    /**
     * 分类别名 - 唯一、不为空
     */
    @Column(name = "slug", nullable = false, length = 100, unique = true)
    private String slug;
    
    /**
     * 父分类ID - 可为空（顶级分类）
     */
    @Column(name = "parent_id")
    private Long parentId;
    
    /**
     * 排序 - 不为空、默认值
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
    
    /**
     * 描述 - 可为空
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
}

