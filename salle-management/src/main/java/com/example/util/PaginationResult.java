package com.example.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaginationResult<T> {
    private final List<T> items;
    private final int currentPage;
    private final int pageSize;
    private final int totalPages;
    private final long totalItems;

    public PaginationResult(List<T> items, int currentPage, int pageSize, long totalItems) {
        if (items == null || currentPage < 1 || pageSize < 1 || totalItems < 0) {
            throw new IllegalArgumentException("Les résultats et les métadonnées de pagination sont invalides.");
        }
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = (int) ((totalItems + pageSize - 1) / pageSize);
    }

    public List<T> getItems() {
        return items;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalItems() {
        return totalItems;
    }
}
