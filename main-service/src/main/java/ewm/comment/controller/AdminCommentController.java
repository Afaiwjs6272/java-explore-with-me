package ewm.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ewm.comment.dto.CommentDto;
import ewm.comment.service.CommentService;

import java.util.List;


@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminCommentController {

    private final CommentService commentService;


    @GetMapping("comment/search")
    public ResponseEntity<List<CommentDto>> search(@RequestParam String text) {
        log.info("Calling the GET request to /admin/comment/search endpoint");
        return ResponseEntity.ok(commentService.search(text));
    }

    @GetMapping("users/{userId}/comment")
    public ResponseEntity<List<CommentDto>> get(@PathVariable Long userId) {
        log.info("Calling the GET request to admin/users/{userId}/comment endpoint");
        return ResponseEntity.ok(commentService.findAllById(userId));
    }

    @DeleteMapping("comment/{comId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> delete(@PathVariable Long comId) {
        log.info("Calling the GET request to admin/comment/{comId} endpoint");
        commentService.delete(comId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("Comment deleted by admin: " + comId);
    }
}