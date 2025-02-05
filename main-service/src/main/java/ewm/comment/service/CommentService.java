package ewm.comment.service;

import ewm.comment.dto.CommentDto;
import ewm.comment.dto.InputCommentDto;
import ewm.comment.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto add(Long userId, Long eventId, InputCommentDto inputCommentDto);

    CommentDto add(Long adminId, Long eventId, CommentDto commentDto);

    CommentDto update(Long userId, Long commentId, InputCommentDto inputCommentDto);

    CommentDto update(Long id, UpdateCommentDto updateCommentDto);

    void delete(Long id);

    void delete(Long userId, Long commentId);

    List<CommentDto> findCommentsByEventId(Long eventId, Integer from, Integer size);
}