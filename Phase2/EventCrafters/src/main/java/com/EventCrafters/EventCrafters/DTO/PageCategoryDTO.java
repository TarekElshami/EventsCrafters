package com.EventCrafters.EventCrafters.DTO;

import com.EventCrafters.EventCrafters.model.Category;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PageCategoryDTO {
    private List<CategoryDTO> categories;
    private int page;

    private int totalPages;

    public PageCategoryDTO(List<CategoryDTO> categories, int page, int totalPages) {
        this.categories = categories;
        this.page = page;
        this.totalPages = totalPages;
    }
}
