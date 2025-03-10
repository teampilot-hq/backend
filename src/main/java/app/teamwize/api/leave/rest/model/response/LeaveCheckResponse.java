package app.teamwize.api.leave.rest.model.response;

import app.teamwize.api.holiday.domain.response.HolidayResponse;

import java.util.List;

public record LeaveCheckResponse(
        Boolean isAllowed,
        String message,
        Float duration,
        Float totalDays,
        List<LeaveResponse> yourConflicts,
        List<LeaveResponse> teamConflicts,
        List<HolidayResponse> holidays) {
}
