package ewm.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewEventDto {
    @NotBlank(message = "Аннотация не может быть пустой")
    private String annotation;
    private Long category;

    @NotBlank(message = "Описание не может быть пустым")
    private String description;
    private String eventDate;
    private LocationDto location;
    private Boolean paid;

    @PositiveOrZero
    private Long participantLimit;
    private Boolean requestModeration;

    @NotBlank(message = "Заголовок не может быть пустым")
    private String title;
}