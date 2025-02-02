package ewm.comment.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateDto {

    @NotBlank
    @Size(min = 1, max = 1000)
    private String text;

}