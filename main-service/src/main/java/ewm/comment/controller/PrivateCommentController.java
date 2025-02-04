package ewm.comment.controller;

import ewm.comment.dto.CommentDto;
import ewm.comment.dto.InputCommentDto;
import ewm.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Validated
public class PrivateCommentController {
    private final CommentService commentService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{eventId}/{userId}")
    public CommentDto addComment(@PathVariable(name = "eventId") Long eventId,
                                 @PathVariable(name = "userId") Long userId,
                                 @Valid @RequestBody InputCommentDto inputCommentDto) {
        return commentService.add(userId, eventId, inputCommentDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{commentId}/{userId}")
    public void deleteComment(@PathVariable(name = "commentId") Long commentId,
                              @PathVariable(name = "userId") Long userId) {
        commentService.delete(userId, commentId);
    }

    @PatchMapping("/{commentId}/{userId}")
    public CommentDto updateComment(@PathVariable(name = "commentId") Long commentId,
                                    @PathVariable(name = "userId") Long userId,
                                    @Valid @RequestBody InputCommentDto inputCommentDto) {
        return commentService.update(userId, commentId, inputCommentDto);
    }
}