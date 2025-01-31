package ewm.event.dto;

import ewm.event.model.PrivateStateAction;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateEventUserRequest {
    @Size(max = 2000)
    private String annotation;
    private Long category;
    @Size(min = 20, max = 7000)
    private String description;
    private String eventDate;
    private LocationDto location;
    private Boolean paid;
    private Long participantLimit;
    private Boolean requestModeration;
    private PrivateStateAction stateAction;
    @Size(min = 3, max = 120)
    private String title;
}