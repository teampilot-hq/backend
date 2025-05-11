package app.teamwize.api.auth.service;


import app.teamwize.api.auth.domain.event.OrganizationCreatedEvent;
import app.teamwize.api.auth.domain.event.OrganizationEventPayload;
import app.teamwize.api.auth.domain.event.UserEventPayload;
import app.teamwize.api.auth.domain.request.LoginRequest;
import app.teamwize.api.auth.domain.request.RegistrationRequest;
import app.teamwize.api.auth.domain.response.AuthenticationResponse;
import app.teamwize.api.auth.exception.InvalidCredentialException;
import app.teamwize.api.base.exception.BaseException;
import app.teamwize.api.event.service.EventService;
import app.teamwize.api.leave.exception.LeavePolicyNotFoundException;
import app.teamwize.api.leave.exception.LeaveTypeNotFoundException;
import app.teamwize.api.leave.service.LeavePolicyService;
import app.teamwize.api.organization.domain.event.OrganizationCreateRequest;
import app.teamwize.api.organization.exception.OrganizationNotFoundException;
import app.teamwize.api.organization.service.OrganizationService;
import app.teamwize.api.team.domain.exception.TeamNotFoundException;
import app.teamwize.api.team.domain.request.TeamCreateRequest;
import app.teamwize.api.team.service.TeamService;
import app.teamwize.api.user.domain.request.AdminUserCreateRequest;
import app.teamwize.api.user.exception.UserAlreadyExistsException;
import app.teamwize.api.user.exception.UserNotFoundException;
import app.teamwize.api.user.mapper.UserMapper;
import app.teamwize.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService implements UserDetailsService {
    private final OrganizationService organizationService;
    private final TokenService tokenService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final TeamService teamService;
    private final LeavePolicyService leavePolicyService;
    private final EventService eventService;


    @Transactional(rollbackFor = BaseException.class)
    public AuthenticationResponse register(RegistrationRequest request) throws UserAlreadyExistsException, OrganizationNotFoundException, TeamNotFoundException, LeaveTypeNotFoundException, LeavePolicyNotFoundException {
        var organization = organizationService.registerOrganization(new OrganizationCreateRequest(request.organizationName(), request.country(), request.timezone()));
        var leavePolicy = leavePolicyService.createDefaultLeavePolicy(organization.getId(), 1L);
        var team = teamService.createTeam(organization.getId(), new TeamCreateRequest("Default", null));
        var registerRequest = new AdminUserCreateRequest(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.timezone(),
                request.country(),
                leavePolicy.getId()
        );

        var user = userService.createOrganizationAdmin(organization.getId(), team.getId(), registerRequest);

        eventService.emmit(organization.getId(), new OrganizationCreatedEvent(
                new UserEventPayload(user),
                new OrganizationEventPayload(organization)
        ));


        var accessToken = tokenService.generateAccessToken(
                user.getId().toString(),
                organization.getId(),
                user.getId(),
                user.getRole(),
                LocalDateTime.now().plusDays(1)
        );
        var refreshToken = tokenService.generateRefreshToken(user.getId().toString(), LocalDateTime.now().plusDays(7));
        return new AuthenticationResponse(accessToken, refreshToken, userMapper.toUserResponse(user));
    }

    public AuthenticationResponse login(LoginRequest request) throws InvalidCredentialException {
        try {
            var user = userService.getUserByEmail(request.getEmail());
            var accessToken = tokenService.generateAccessToken(
                    user.getId().toString(),
                    user.getOrganization().getId(),
                    user.getId(),
                    user.getRole(),
                    LocalDateTime.now().plusDays(1)
            );
            var refreshToken = tokenService.generateRefreshToken(user.getId().toString(), LocalDateTime.now().plusDays(7));
            return new AuthenticationResponse(accessToken, refreshToken, userMapper.toUserResponse(user));

        } catch (UserNotFoundException ex) {
            throw new InvalidCredentialException("Invalid email or password , email: " + request.getEmail());
        }
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        try {
            return userService.getUserDetails(Long.valueOf(userId));
        } catch (UserNotFoundException ex) {
            throw new UsernameNotFoundException(userId);
        }
    }

}
