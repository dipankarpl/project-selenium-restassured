package com.yourorg.models.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginatedResponse<T> {
    
    @JsonProperty("data")
    private List<T> data;
    
    @JsonProperty("page")
    private Integer page;
    
    @JsonProperty("pageSize")
    private Integer pageSize;
    
    @JsonProperty("totalPages")
    private Integer totalPages;
    
    @JsonProperty("totalCount")
    private Long totalCount;
    
    @JsonProperty("hasNext")
    private Boolean hasNext;
    
    @JsonProperty("hasPrevious")
    private Boolean hasPrevious;
    
    // Default constructor
    public PaginatedResponse() {}
    
    // Getters and Setters
    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }
    
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    
    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
    
    public Long getTotalCount() { return totalCount; }
    public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
    
    public Boolean getHasNext() { return hasNext; }
    public void setHasNext(Boolean hasNext) { this.hasNext = hasNext; }
    
    public Boolean getHasPrevious() { return hasPrevious; }
    public void setHasPrevious(Boolean hasPrevious) { this.hasPrevious = hasPrevious; }
    
    @Override
    public String toString() {
        return "PaginatedResponse{" +
                "page=" + page +
                ", pageSize=" + pageSize +
                ", totalPages=" + totalPages +
                ", totalCount=" + totalCount +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                ", dataSize=" + (data != null ? data.size() : 0) +
                '}';
    }
}