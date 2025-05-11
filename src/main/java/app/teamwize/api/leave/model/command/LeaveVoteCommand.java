package app.teamwize.api.leave.model.command;

import app.teamwize.api.leave.model.LeaveVoteStatus;

public record LeaveVoteCommand(LeaveVoteStatus status, String comment) {
    public LeaveVoteCommand {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
    }
}