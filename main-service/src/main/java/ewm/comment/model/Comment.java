package ewm.comment.model;

import lombok.*;
import ewm.event.model.Event;
import ewm.user.model.User;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "textual_content")
    private String text;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "patch_time")
    private LocalDateTime patchTime;
}