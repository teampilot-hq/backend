package app.teamwize.api.leave.model;

import app.teamwize.api.holiday.domain.entity.Holiday;
import app.teamwize.api.leave.model.entity.Leave;

import java.util.List;

public record LeaveCheckResult(
        Boolean isAllowed,
        String message,
        Float duration,
        Float totalDays,
        List<Leave> yourConflicts,
        List<Leave> teamConflicts,
        List<Holiday> holidays) {
}
