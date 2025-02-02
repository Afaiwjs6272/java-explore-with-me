package ewm.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ewm.category.model.Category;
import ewm.comment.model.Comment;
import ewm.event.model.Location;
import ewm.event.model.EventState;
import ewm.user.model.User;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventFullWithComment {
    private Long id;

    private String annotation;

    private Category category;

    private String createdOn;

    private String description;

    private String eventDate;

    private User initiator;

    private Location location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    private EventState state;

    private String title;

    private Long views;

    private Long confirmedRequests;

    private String publishedOn;

    private List<Comment> comments;
}