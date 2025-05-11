package app.teamwize.api.leave.rest.model.request;

import app.teamwize.api.leave.model.LeaveVoteStatus;


public record LeaveVoteRequest(LeaveVoteStatus status, String comment, Long userId) {
    public LeaveVoteRequest {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }
}