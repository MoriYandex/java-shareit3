package ru.practicum.shareit.comment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Long id;
    private String text;
    private Long itemId;
    private Long authorId;
    private String authorName;
    private LocalDateTime created;

    public CommentDto(@JsonProperty("id") Long id,
                      @JsonProperty("text") String text,
                      @JsonProperty("itemId") Long itemId,
                      @JsonProperty("authorId") Long authorId,
                      @JsonProperty("authorName") String authorName,
                      @JsonProperty("created") LocalDateTime created) {
        this.id = id;
        this.text = text;
        this.itemId = itemId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.created = created;
    }
}
