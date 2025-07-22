package com.horseriding.ecommerce.categories.dtos.responses;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for hierarchical category tree display.
 * Optimized for displaying category hierarchies.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeResponse {

    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private List<CategoryTreeResponse> subcategories;
    private boolean active;
    private Integer displayOrder;
    private int level;
    private String fullPath;
    private int productCount;

    /**
     * Utility methods
     */
    public boolean isRootCategory() {
        return parentId == null;
    }

    public boolean hasSubcategories() {
        return subcategories != null && !subcategories.isEmpty();
    }

    public boolean hasProducts() {
        return productCount > 0;
    }
}
