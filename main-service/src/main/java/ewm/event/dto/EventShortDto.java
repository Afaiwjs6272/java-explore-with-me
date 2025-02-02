package ewm.event.dto;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@AllArgsConstructor
public class EventShortDto extends EventBaseDto {
    private String eventDate;

    private Long commentCount;

    public void setCommentsCount(Long orDefault) {
        commentCount = orDefault;
    }
}
