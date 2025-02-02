package ewm.comment.mapper;

import lombok.experimental.UtilityClass;
import ewm.comment.dto.CommentDto;
import ewm.comment.dto.CommentShortDto;
import ewm.comment.dto.CommentCreateDto;
import ewm.comment.model.Comment;
import ewm.event.mapper.EventMapper;
import ewm.user.mapper.UserMapper;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CommentMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Comment toComment(CommentCreateDto commentDto) {
        return Comment.builder()
                .text(commentDto.getText())
                .build();
    }

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .author(UserMapper.toUserDto(comment.getAuthor()))
                .event(EventMapper.toEventComment(comment.getEvent()))
                .createTime(comment.getCreateTime().format(FORMATTER))
                .text(comment.getText())
                .build();
    }

    public List<CommentDto> toListCommentDto(List<Comment> list) {
        return list.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());
    }

    public CommentShortDto toCommentShortDto(Comment comment) {
        return CommentShortDto.builder()
                .author(UserMapper.toUserDto(comment.getAuthor()))
                .createTime(comment.getCreateTime().format(FORMATTER))
                .id(comment.getId())
                .text(comment.getText())
                .build();
    }

    public List<CommentShortDto> toListCommentShortDto(List<Comment> list) {
        return list.stream().map(CommentMapper::toCommentShortDto).collect(Collectors.toList());
    }
}