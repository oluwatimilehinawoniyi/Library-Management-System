package com.lms.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResponse<T> {

    @JsonProperty("content")
    private List<T> content;

    @JsonProperty("totalElements")
    private long totalElements;

    @JsonProperty("totalPages")
    private int totalPages;

    @JsonProperty("size")
    private int size;

    @JsonProperty("number")
    private int number;

    @JsonProperty("numberOfElements")
    private int numberOfElements;

    @JsonProperty("first")
    private boolean first;

    @JsonProperty("last")
    private boolean last;

    @JsonProperty("empty")
    private boolean empty;

    // check if there's a previous page
    public boolean hasPrevious() {
        return number > 0;
    }

    // check if there's a next page
    public boolean hasNext() {
        return number < totalPages - 1;
    }

    public String getPaginationInfo() {
        if (empty) {
            return "No items";
        }
        int start = number * size + 1;
        int end = start + numberOfElements - 1;
        return String.format("Showing %d-%d of %d", start, end, totalElements);
    }
}
