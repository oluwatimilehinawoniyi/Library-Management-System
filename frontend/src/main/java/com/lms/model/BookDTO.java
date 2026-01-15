package com.lms.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("author")
    private String author;

    @JsonProperty("isbn")
    private String isbn;

    @JsonProperty("publishedDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishedDate;

    // validate that all required fields are filled
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
                author != null && !author.trim().isEmpty() &&
                isbn != null && !isbn.trim().isEmpty() &&
                publishedDate != null;
    }

    public String getFormattedPublishedDate() {
        return publishedDate != null ? publishedDate.toString() : "";
    }
}
