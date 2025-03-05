package app.teamwize.api.user.domain.request;

import app.teamwize.api.user.domain.UserRole;

import java.time.Instant;

public record UserCreateRequest(
        String email,
        String firstName,
        String lastName,
        String password,
        String phone,
        UserRole role,
        String timezone,
        String country,
        Long teamId,
        Long leavePolicyId,
        Instant joinedAt) {
}