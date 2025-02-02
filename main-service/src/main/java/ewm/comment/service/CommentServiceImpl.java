package ewm.comment.service;

import ewm.comment.dto.CommentCountDto;
import ewm.comment.dto.CommentCreateDto;
import ewm.comment.dto.CommentDto;
import ewm.comment.dto.CommentShortDto;
import ewm.comment.mapper.CommentMapper;
import ewm.comment.model.Comment;
import ewm.comment.repository.CommentRepository;
import ewm.event.model.Event;
import ewm.event.model.EventState;
import ewm.event.repository.EventRepository;
import ewm.exception.NotFoundException;
import ewm.exception.NotPublishEventException;
import ewm.exception.OperationUnnecessaryException;
import ewm.user.model.User;
import ewm.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ewm.utility.Constants.createPageRequestAsc;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository repository;
    private final UserRepository userMainServiceRepository;
    private final EventRepository eventRepository;

    @Override
    public void delete(Long comId) {
        log.info("admin delete - invoked");
        if (repository.existsById(comId)) {
            log.error("User with id = {} not exist", comId);
            throw new NotFoundException("Comment not found");
        }
        log.info("Result: comment with id = {} deleted", comId);
        repository.deleteById(comId);
    }

    @Override
    public List<CommentDto> search(String text) {
        log.info("admin search - invoked");
        List<Comment> list = repository.findAllByText(text);
        log.info("Result: list of comments size = {} ", list.size());
        return CommentMapper.toListCommentDto(list);
    }

    @Override
    public List<CommentDto> findAllById(Long userId) {
        log.info("admin findAllById - invoked");
        if (userMainServiceRepository.existsById(userId)) {
            log.error("User with id = {} not exist", userId);
            throw new NotFoundException("User not found");
        }
        List<Comment> list = repository.findAllByAuthorId(userId);
        log.info("Result: list of comments size = {} ", list.size());
        return CommentMapper.toListCommentDto(list);
    }

    @Override
    public CommentDto getComment(Long comId) {
        log.info("getComment - invoked");
        Comment comment = repository.findById(comId)
                .orElseThrow(() -> {
                    log.error("Comment with id = {} - not exist", comId);
                    return new NotFoundException("Comment not found");
                });
        log.info("Result: comment with id= {}", comId);
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentShortDto> getCommentsByEvent(Long eventId, int from, int size) {
        log.info("getCommentsByEvent - invoked");
        if (eventRepository.existsById(eventId)) {
            log.error("Event with id = {} - not exist", eventId);
            throw new NotFoundException("Event not found");
        }
        Pageable pageable = createPageRequestAsc("createTime", from, size);
        List<Comment> comments = repository.findAllByEventId(eventId, pageable);
        log.info("Result : list of comments size = {}", comments.size());
        return CommentMapper.toListCommentShortDto(comments);
    }

    @Transactional
    @Override
    public CommentDto createComment(Long userId, Long eventId, CommentCreateDto commentDto) {
        log.info("createComment - invoked");
        Comment comment = CommentMapper.toComment(commentDto);
        User author = userMainServiceRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User with id = {} - not registered", userId);
                    return new NotFoundException("Please register first then you can comment");
                });

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Event with id = {} - not exist", eventId);
                    return new NotFoundException("Event not found");
                });

        if (!event.getState().equals(EventState.PUBLISHED)) {
            log.error("Event state = {} - should be PUBLISHED", event.getState());
            throw new NotPublishEventException("Event not published you cant comment it");
        }

        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setCreateTime(LocalDateTime.now().withNano(0));
        log.info("Result: new comment created");
        return CommentMapper.toCommentDto(repository.save(comment));
    }

    @Transactional
    @Override
    public void deleteComment(Long userId, Long comId) {
        log.info("deleteComment - invoked");
        Comment comment = repository.findById(comId)
                .orElseThrow(() -> {
                    log.error("Comment with id = {} - not exist", comId);
                    return new NotFoundException("Comment not found");
                });
        if (!comment.getAuthor().getId().equals(userId)) {
            log.error("Unauthorized access by user");
            throw new NotPublishEventException("you didn't write this comment and can't delete it");
        }
        log.info("Result: comment with id = {} - deleted", comId);
        repository.deleteById(comId);
    }

    @Transactional
    @Override
    public CommentDto patchComment(Long userId, Long comId, CommentCreateDto commentCreateDto) {
        log.info("patchComment - invoked");

        Comment newComment = CommentMapper.toComment(commentCreateDto);

        Comment comment = repository.findById(comId)
                .orElseThrow(() -> {
                    log.error("Comment with id = {} - not exist", comId);
                    return new NotFoundException("Comment not found");
                });

        if (!comment.getAuthor().getId().equals(userId)) {
            log.error("Unauthorized access by user");
            throw new OperationUnnecessaryException("you didn't write this comment and can't patch it");
        }

        comment.setText(newComment.getText());
        comment.setCreateTime(LocalDateTime.now().withNano(0));
        log.info("Result: comment with id = {} - updated", comId);
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public Map<Long, Long> getCommentCount(Collection<Event> list) {
        List<Long> listEventId = list.stream().map(Event::getId).collect(Collectors.toList());
        List<CommentCountDto> countList = repository.findAllCommentCount(listEventId);
        return countList.stream().collect(Collectors.toMap(CommentCountDto::getEventId, CommentCountDto::getCommentCount));
    }
}