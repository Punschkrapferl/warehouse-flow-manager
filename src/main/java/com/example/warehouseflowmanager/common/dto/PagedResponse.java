package com.example.warehouseflowmanager.common.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "PagedResponse",
        description = "Generic wrapper for paginated API responses."
)
public class PagedResponse<T> {

    @Schema(
            description = "Zero-based page index of the current result page.",
            example = "0"
    )
    private int page;

    @Schema(
            description = "Requested page size.",
            example = "10"
    )
    private int size;

    @Schema(
            description = "Total number of available elements across all pages.",
            example = "57"
    )
    private long totalElements;

    @Schema(
            description = "Total number of available pages.",
            example = "6"
    )
    private int totalPages;

    @Schema(
            description = "Indicates whether this is the first page.",
            example = "true"
    )
    private boolean first;

    @Schema(
            description = "Indicates whether this is the last page.",
            example = "false"
    )
    private boolean last;

    @ArraySchema(
            schema = @Schema(description = "Elements contained in the current page.")
    )
    private List<T> content;

    public static <T> PagedResponse<T> from(Page<T> pageData) {
        return new PagedResponse<>(
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages(),
                pageData.isFirst(),
                pageData.isLast(),
                pageData.getContent()
        );
    }
}