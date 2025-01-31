package ewm.event.repository;

import ewm.event.model.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e " +
            "WHERE LOWER(e.title) LIKE LOWER(?1) " +
            "OR LOWER(e.description) LIKE LOWER(?1) " +
            "AND e.category IN ?2 " +
            "AND e.paid = ?3 " +
            "AND e.eventDate BETWEEN ?4 AND ?5 " +
            "AND e.participantLimit = ?6")
    List<Event> findEvents(
            String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") Boolean onlyAvailable,
            Pageable pageable
    );

    @Query("SELECT e FROM Event e " +
            "WHERE e.initiator.id IN :users " +
            "AND e.state IN :states " +
            "AND e.category.id IN :categories " +
            "AND e.eventDate BETWEEN :rangeStart AND :rangeEnd")
    List<Event> findAdminEvents(List<Long> users,
                                List<String> states,
                                List<Long> categories,
                                LocalDateTime rangeStart,
                                LocalDateTime rangeEnd,
                                Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    List<Event> findAllByIdIsIn(Collection<Long> ids);

    List<Event> findAllByInitiatorId(Long initiatorId);

    List<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);
}