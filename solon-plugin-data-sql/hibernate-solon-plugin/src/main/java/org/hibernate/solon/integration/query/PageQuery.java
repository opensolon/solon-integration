package org.hibernate.solon.integration.query;

import java.util.List;

/**
 * 分页查询结果
 * 
 * @param <T> 实体类型
 * @author noear
 * @since 3.4
 */
public class PageQuery<T> {
    /**
     * 数据列表
     */
    private List<T> content;
    
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 当前页码（从1开始）
     */
    private int page;
    
    /**
     * 每页大小
     */
    private int size;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    public PageQuery() {
    }
    
    public PageQuery(List<T> content, long total, int page, int size) {
        this.content = content;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) total / size);
    }
    
    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return page > 1;
    }
    
    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return page < totalPages;
    }
    
    /**
     * 是否为第一页
     */
    public boolean isFirst() {
        return page == 1;
    }
    
    /**
     * 是否为最后一页
     */
    public boolean isLast() {
        return page == totalPages || totalPages == 0;
    }
    
    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
    
    // Getters and Setters
    public List<T> getContent() {
        return content;
    }
    
    public void setContent(List<T> content) {
        this.content = content;
    }
    
    public long getTotal() {
        return total;
    }
    
    public void setTotal(long total) {
        this.total = total;
        if (size > 0) {
            this.totalPages = (int) Math.ceil((double) total / size);
        }
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
        if (total > 0 && size > 0) {
            this.totalPages = (int) Math.ceil((double) total / size);
        }
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}

