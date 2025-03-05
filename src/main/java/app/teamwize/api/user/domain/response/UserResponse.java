package app.teamwize.api.user.domain.response;


import app.teamwize.api.assets.domain.model.response.AssetCompactResponse;
import app.teamwize.api.leave.rest.model.response.LeavePolicyCompactResponse;
import app.teamwize.api.organization.domain.response.OrganizationCompactResponse;
import app.teamwize.api.team.domain.response.TeamCompactResponse;
import app.teamwize.api.user.domain.UserRole;
import app.teamwize.api.user.domain.UserStatus;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.time.Instant;


public record UserResponse(
        @Nonnull Long id,
        @Nonnull UserStatus status,
        @Nonnull UserRole role,
        @Nonnull String email,
        @Nonnull String firstName,
        @Nullable String lastName,
        @Nullable String phone,
        @Nullable String timezone,
        @Nullable String country,
        @Nonnull OrganizationCompactResponse organization,
        @Nonnull TeamCompactResponse team,
        @Nonnull AssetCompactResponse avatar,
        @Nonnull LeavePolicyCompactResponse leavePolicy,
        @Nonnull Instant joinedAt,
        @Nonnull Instant createdAt,
        @Nonnull Instant updatedAt
) {
}