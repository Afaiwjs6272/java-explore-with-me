package ewm.event.service;

import ewm.ParamDto;
import ewm.ViewStats;
import ewm.category.model.Category;
import ewm.category.repository.CategoryRepository;
import ewm.client.RestStatClient;
import ewm.event.dto.*;
import ewm.event.mapper.EventMapper;
import ewm.event.mapper.LocationMapper;
import ewm.event.model.*;
import ewm.event.repository.EventRepository;
import ewm.event.repository.LocationRepository;
import ewm.exception.ConditionNotMetException;
import ewm.exception.EntityNotFoundException;
import ewm.exception.InitiatorRequestException;
import ewm.exception.ValidationException;
import ewm.request.model.Request;
import ewm.request.model.RequestStatus;
import ewm.request.repository.RequestRepository;
import ewm.user.model.User;
import ewm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ewm.utility.Constants.FORMAT_DATETIME;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final RestStatClient statClient;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final LocationMapper locationMapper;

    @Override
    public List<EventShortDto> getAllEvents(ReqParam reqParam) {
        Pageable pageable = PageRequest.of(reqParam.getFrom(), reqParam.getSize());
        List<EventShortDto> eventShortDtos = eventMapper.toEventShortDto(eventRepository.findEvents(
                reqParam.getText(),
                reqParam.getCategories(),
                reqParam.getPaid(),
                reqParam.getRangeStart(),
                reqParam.getRangeEnd(),
                reqParam.getOnlyAvailable(),
                pageable
        ));
        List<EventShortDto> addedViews = addViews(eventShortDtos);
        List<EventShortDto> addedRequests = addRequests(addedViews);
        return switch (reqParam.getSort()) {
            case EVENT_DATE ->
                    addedRequests.stream().sorted(Comparator.comparing(EventShortDto::getEventDate)).toList();
            case VIEWS -> addedRequests.stream().sorted(Comparator.comparing(EventShortDto::getViews)).toList();
        };
    }

    @Override
    public List<EventFullDto> getAllEvents(AdminEventParams params) {
        Pageable pageable = PageRequest.of(params.getFrom(), params.getSize());

        return eventMapper.toEventFullDtos(eventRepository.findAdminEvents(
                params.getUsers(),
                params.getStates(),
                params.getCategories(),
                params.getRangeStart(),
                params.getRangeEnd(),
                pageable));
    }

    @Override
    public EventFullDto publicGetEvent(long id) {
        return addViews(eventMapper.toEventFullDto(eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие c ID - " + id + ", не найдено."))));
    }

    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        LocalDateTime eventDate = LocalDateTime.parse(newEventDto.getEventDate(),
                DateTimeFormatter.ofPattern(FORMAT_DATETIME));
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException(NewEventDto.class, "До начала события осталось меньше двух часов");
        }
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, "Пользователь не найден"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new EntityNotFoundException(Category.class, "Категория не найден"));

        Event event = eventMapper.toEvent(newEventDto);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event.setLocation(locationRepository.save(event.getLocation()));
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto update(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, " c ID = " + eventId + ", не найдено."));

        if ((event.getPublishedOn() != null) && updateEventAdminRequest.getEventDate().isAfter(event.getPublishedOn().minusHours(1))) {
            throw new ConditionNotMetException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
        }
        if (updateEventAdminRequest.getStateAction() == AdminStateAction.PUBLISH_EVENT && event.getState() != EventState.PENDING) {
            throw new ConditionNotMetException("Cобытие можно публиковать, только если оно в состоянии ожидания публикации");
        }
        if (updateEventAdminRequest.getStateAction() == AdminStateAction.REJECT_EVENT && event.getState() == EventState.PUBLISHED) {
            throw new ConditionNotMetException("Cобытие можно отклонить, только если оно еще не опубликовано");
        }

        checkEvent(event, updateEventAdminRequest);
        return eventMapper.toEventFullDto(eventRepository.save(event));

    }

    @Override
    public List<EventShortDto> findUserEvents(Long userId, Integer from, Integer size) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, "Пользователь не найден"));
        Pageable pageable = PageRequest.of(from, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        List<EventShortDto> eventShortDtos = eventMapper.toEventShortDto(events);
        List<EventShortDto> addedViews = addViews(eventShortDtos);
        return addRequests(addedViews);
    }

    @Override
    public EventFullDto findUserEventById(Long userId, Long eventId) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, "Пользователь не найден"));
        Event event = eventRepository.findByIdAndInitiatorId(userId, eventId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие не найдено"));
        EventFullDto result = eventMapper.toEventFullDto(event);
        EventFullDto addedViews = addViews(result);
        return addRequests(addedViews);
    }

    @Override
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, "Пользователь не найден"));
        Event event = eventRepository.findByIdAndInitiatorId(userId, eventId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие не найдено"));
        if (Objects.equals(initiator.getId(), event.getInitiator().getId())) {
            throw new ConditionNotMetException("Изменение опубликованного события от имени пользователя");
        }
        if (event.getState() == EventState.PUBLISHED) {
            throw new InitiatorRequestException("Нельзя отредактировать опубликованное событие");
        }
        LocalDateTime eventDate;
        if (updateRequest.getEventDate() != null) {
            eventDate = LocalDateTime.parse(updateRequest.getEventDate(),
                    DateTimeFormatter.ofPattern(FORMAT_DATETIME));
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException(NewEventDto.class, "До начала события осталось меньше двух часов");
            }
            event.setEventDate(eventDate);
        }
        Category category;
        if (updateRequest.getCategory() != null) {
            category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new EntityNotFoundException(Category.class, "Категория не найден"));
            event.setCategory(category);
        }
        if (updateRequest.getLocation() != null) {
            Optional<Location> locationOpt = locationRepository.findByLatAndLon(
                    updateRequest.getLocation().getLat(),
                    updateRequest.getLocation().getLon());
            Location location = locationOpt.orElse(locationRepository.save(
                    new Location(null, updateRequest.getLocation().getLat(), updateRequest.getLocation().getLon())));
            event.setLocation(location);
        }
        if (updateRequest.getAnnotation() != null && !updateRequest.getAnnotation().isBlank()) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getDescription() != null && !updateRequest.getDescription().isBlank()) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null && updateRequest.getParticipantLimit() >= 0) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null && !updateRequest.getTitle().isBlank()) {
            event.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == PrivateStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
            if (updateRequest.getStateAction() == PrivateStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            }
        }
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    private List<EventShortDto> addViews(List<EventShortDto> eventDtos) {
        HashMap<String, EventShortDto> eventDtoMap = new HashMap<>();
        List<String> gettingUris = new ArrayList<>();
        for (EventShortDto dto : eventDtos) {
            String uri = "/events/" + dto.getId();
            eventDtoMap.put(uri, dto);
            gettingUris.add(uri);
        }
        ParamDto paramDto = new ParamDto(LocalDateTime.now().minusYears(1), LocalDateTime.now(), gettingUris, true);
        statClient.getStat(paramDto)
                .stream()
                .peek(viewStats -> eventDtoMap.get(viewStats.getUri()).setViews(viewStats.getHits()));
        return eventDtoMap.values().stream().toList();
    }

    private List<EventFullDto> addViewsFullDto(List<EventFullDto> eventDtos) {
        HashMap<String, EventFullDto> eventDtoMap = new HashMap<>();
        List<String> gettingUris = new ArrayList<>();
        for (EventFullDto dto : eventDtos) {
            String uri = "/events/" + dto.getId();
            eventDtoMap.put(uri, dto);
            gettingUris.add(uri);
        }
        ParamDto paramDto = new ParamDto(LocalDateTime.now().minusYears(1), LocalDateTime.now(), gettingUris, true);
        statClient.getStat(paramDto)
                .stream()
                .peek(viewStats -> eventDtoMap.get(viewStats.getUri()).setViews(viewStats.getHits()));
        return eventDtoMap.values().stream().toList();
    }

    private EventFullDto addViews(EventFullDto eventShortDto) {
        List<String> gettingUris = new ArrayList<>();
        gettingUris.add("/events/" + eventShortDto.getId());
        ParamDto paramDto = new ParamDto(LocalDateTime.now().minusYears(1), LocalDateTime.now(), gettingUris, true);
        Long views = statClient.getStat(paramDto)
                .stream().map(ViewStats::getHits).reduce(0L, Long::sum);
        eventShortDto.setViews(views);
        return eventShortDto;
    }

    private void checkEvent(Event event, UpdateEventAdminRequest updateEventAdminRequest) {
        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventAdminRequest.getCategory().longValue())
                    .orElseThrow(() -> new EntityNotFoundException(Category.class, "Категория не найдена"));
            event.setCategory(category);
        }
        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getEventDate() != null) {
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }
        if (updateEventAdminRequest.getLocation() != null) {
            Location location = locationRepository.save(locationMapper.toLocation(updateEventAdminRequest.getLocation()));
            event.setLocation(location);
        }
        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit().longValue());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }
        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }
        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
                event.setState(EventState.PUBLISHED);
            }
            if (updateEventAdminRequest.getStateAction() == AdminStateAction.REJECT_EVENT) {
                event.setState(EventState.CANCELED);
            }
        }
    }

    private List<EventShortDto> addRequests(List<EventShortDto> eventDtos) {
        List<Long> eventIds = eventDtos.stream().map(EventShortDto::getId).toList();
        List<Request> requests = requestRepository.findAllByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED);
        Map<Long, Long> requestsMap = requests.stream()
                .collect(Collectors.groupingBy(request -> request.getEvent().getId(), Collectors.counting()));
        eventDtos.forEach(eventDto -> eventDto.setConfirmedRequests(requestsMap.getOrDefault(eventDto.getId(), 0L)));
        return eventDtos;
    }

    private List<EventFullDto> addRequestsFullDto(List<EventFullDto> eventDtos) {
        List<Long> eventIds = eventDtos.stream().map(EventFullDto::getId).toList();
        List<Request> requests = requestRepository.findAllByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED);
        Map<Long, Long> requestsMap = requests.stream()
                .collect(Collectors.groupingBy(request -> request.getEvent().getId(), Collectors.counting()));
        eventDtos.forEach(eventDto -> eventDto.setConfirmedRequests(requestsMap.getOrDefault(eventDto.getId(), 0L)));
        return eventDtos;
    }

    private EventFullDto addRequests(EventFullDto eventDto) {
        eventDto.setConfirmedRequests(
                requestRepository.countByEventIdAndStatus(eventDto.getId(), RequestStatus.CONFIRMED)
        );
        return eventDto;
    }
}
