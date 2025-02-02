package ewm.comment.dto;

import lombok.*;
import ewm.event.dto.EventCommentDto;
import ewm.user.dto.UserDto;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

    private long id;

    private String text;

    private UserDto author;

    private EventCommentDto event;

    private String createTime;
}