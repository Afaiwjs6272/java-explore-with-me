package ewm.comment.service;

import ewm.comment.dto.CommentCreateDto;
import ewm.comment.dto.CommentDto;
import ewm.comment.dto.CommentShortDto;
import ewm.event.model.Event;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CommentService {
    void delete(Long comId);

    List<CommentDto> search(String text);

    List<CommentDto> findAllById(Long userId);

    CommentDto getComment(Long comId);

    List<CommentShortDto> getCommentsByEvent(Long eventId, int from, int size);

    CommentDto createComment(Long userId, Long eventId, CommentCreateDto commentDto);

    void deleteComment(Long userId, Long comId);

    CommentDto patchComment(Long userId, Long comId, CommentCreateDto commentCreateDto);

    Map<Long, Long> getCommentCount(Collection<Event> list);
}
