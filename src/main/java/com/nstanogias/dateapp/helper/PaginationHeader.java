package com.nstanogias.dateapp.helper;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaginationHeader {
    private int currentPage;
    private int itemsPerPage;
    private long totalItems;
    private int totalPages;
}
