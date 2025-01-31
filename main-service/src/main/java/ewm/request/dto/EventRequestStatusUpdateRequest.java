package ewm.request.dto;

import ewm.request.model.RequestStatus;
import lombok.Getter;

import java.util.List;

@Getter
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private RequestStatus status;
}