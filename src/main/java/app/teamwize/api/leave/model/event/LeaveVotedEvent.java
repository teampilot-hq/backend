package app.teamwize.api.leave.model.event;

import app.teamwize.api.auth.domain.event.UserEventPayload;
import app.teamwize.api.event.model.EventPayload;
import app.teamwize.api.event.model.EventType;
import app.teamwize.api.leave.model.LeaveVoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;


@Schema(name = "LEAVE_VOTED", description = "Leave voted event")
public record LeaveVotedEvent(LeaveEventPayload leave, UserEventPayload user,
                              LeaveVoteStatus vote) implements EventPayload {

    @Override
    public EventType name() {
        return EventType.LEAVE_VOTED;
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "leave", leave,
                "vote", vote,
                "user", user
        );
    }


}
