package app.teamwize.api.event.service;

import app.teamwize.api.base.domain.model.Paged;
import app.teamwize.api.base.domain.model.Pagination;
import app.teamwize.api.event.entity.EventEntity;
import app.teamwize.api.event.entity.EventExecutionEntity;
import app.teamwize.api.event.exception.EventNotFoundException;
import app.teamwize.api.event.mapper.EventMapper;
import app.teamwize.api.event.model.*;
import app.teamwize.api.event.repository.EventExecutionRepository;
import app.teamwize.api.event.repository.EventRepository;
import app.teamwize.api.event.service.handler.EventHandler;
import app.teamwize.api.organization.domain.entity.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventExecutionRepository executionRepository;
    @Lazy
    private final List<EventHandler> eventHandlers;
    private final EventMapper eventMapper;


    @Transactional
    public Event emmit(Long organizationId, EventType eventType, Map<String, Object> params, byte maxAttempts, Instant scheduledAt) {
        var executions = eventHandlers.stream().filter(eventHandler -> eventHandler.accepts(eventType)).map(eventHandler -> new EventExecutionEntity()
                .setStatus(EventExecutionStatus.PENDING)
                .setAttempts(0)
                .setHandler(eventHandler.name())).toList();
        var event = new EventEntity()
                .setOrganization(new Organization(organizationId))
                .setType(eventType)
                .setParams(params)
                .setStatus(EventStatus.PENDING)
                .setMaxAttempts(maxAttempts)
                .setScheduledAt(scheduledAt)
                .setExecutions(executions);

        executions.forEach(eventExecutionEntity -> eventExecutionEntity.setEvent(event));

        return eventMapper.toEvent(eventRepository.persist(event));
    }

    @Transactional
    public Event emmit(Long organizationId, EventPayload eventPayload) {
        return emmit(organizationId, eventPayload.name(), eventPayload.payload(), (byte) 3, Instant.now());
    }


    // Maybe it's better to try forever to execute an event
    // There is no need to have exitCode for events they are not jobs

    @Transactional
    @Scheduled(fixedDelay = 10_000)
    public void processEvents() {
        var pendingEvents = eventRepository.findByStatus(EventStatus.PENDING);
        for (var pendingEvent : pendingEvents) {
            for (var execution : pendingEvent.getExecutions()) {
                var handlerOptional = eventHandlers.stream().filter(eventHandler -> eventHandler.name().equals(execution.getHandler())).findFirst();
                if (handlerOptional.isEmpty()) continue;
                var handler = handlerOptional.get();
                var executionResult = handler.process(pendingEvent);
                if (executionResult.exitCode() == EventExitCode.SUCCESS) {
                    execution.setStatus(EventExecutionStatus.FINISHED);
                } else {
                    execution.setStatus(EventExecutionStatus.RETRYING);
                }
                execution.setExitCode(executionResult.exitCode())
                        .setAttempts(execution.getAttempts() + 1)
                        .setMetadata(executionResult.metadata());

                if (execution.getAttempts() > pendingEvent.getMaxAttempts()) {
                    execution.setStatus(EventExecutionStatus.FINISHED);
                    execution.setExitCode(EventExitCode.RETRY_EXCEEDED);
                }
                executionRepository.update(execution);
            }
            pendingEvent.setStatus(EventStatus.FINISHED);
        }
        eventRepository.updateAll(pendingEvents);
    }

    public Paged<Event> getEvents(Pagination pagination) {
        var sort = Sort.by("id").descending();
        var pageRequest = PageRequest.of(pagination.pageNumber(), pagination.pageSize(), sort);
        var pagedEvents = eventRepository.findAll(pageRequest);
        return new Paged<>(
                pagedEvents.getContent().stream().map(eventMapper::toEvent).toList(),
                pagination.pageNumber(),
                pagination.pageSize(),
                pagedEvents.getTotalElements()
        );

    }

    public Event getEvent(Long organizationId, Long id) throws EventNotFoundException {
        var event = eventRepository.findByOrganizationIdAndId(organizationId, id).orElseThrow(() -> new EventNotFoundException("Event not found with Id: " + id));
        return eventMapper.toEvent(event);
    }

    public Paged<EventExecution> getEventExecutions(Long eventId, Pagination pagination) {
        var sort = Sort.by("id").descending();
        var pageRequest = PageRequest.of(pagination.pageNumber(), pagination.pageSize(), sort);
        var pagedEvents = executionRepository.findByEventId(eventId, pageRequest);
        return new Paged<>(
                pagedEvents.getContent().stream().map(eventMapper::toEventExecution).toList(),
                pagination.pageNumber(),
                pagination.pageSize(),
                pagedEvents.getTotalElements()
        );
    }
}
