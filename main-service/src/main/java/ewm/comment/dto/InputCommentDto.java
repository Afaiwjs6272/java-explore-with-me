package ewm.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

//DTO Входящее дто с текстом
@Getter
public class InputCommentDto {
    @NotBlank(message = "Текст комментария не может быть пустым")
    private String text;
}