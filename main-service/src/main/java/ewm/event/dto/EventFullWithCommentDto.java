package ewm.event.dto;

import ewm.category.dto.CategoryDto;
import ewm.comment.dto.CommentShortDto;
import ewm.event.model.EventState;
import ewm.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventFullWithCommentDto {
    private Long id;

    private String annotation;

    private CategoryDto category;

    private String createdOn;

    private String description;

    private String eventDate;

    private UserDto initiator;

    private LocationDto location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    private EventState state;

    private String title;

    private Long views;

    private Long confirmedRequests;

    private String publishedOn;

    private List<CommentShortDto> comments;
}