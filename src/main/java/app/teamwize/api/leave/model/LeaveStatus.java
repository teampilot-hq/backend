package app.teamwize.api.leave.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum LeaveStatus {
    ENABLED,
    ACCEPTED,
    REJECTED,
    @JsonEnumDefaultValue
    PENDING;

    @Override
    public String toString() {
        return super.toString();
    }
}
