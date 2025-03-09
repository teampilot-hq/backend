package app.teamwize.api.user.service;

import app.teamwize.api.assets.domain.exception.AssetNotFoundException;
import app.teamwize.api.assets.service.AssetService;
import app.teamwize.api.auth.domain.AuthUserDetails;
import app.teamwize.api.auth.domain.event.OrganizationEventPayload;
import app.teamwize.api.auth.domain.event.UserEventPayload;
import app.teamwize.api.base.domain.model.request.PaginationRequest;
import app.teamwize.api.event.service.EventService;
import app.teamwize.api.leave.exception.LeavePolicyNotFoundException;
import app.teamwize.api.leave.service.LeavePolicyService;
import app.teamwize.api.organization.exception.OrganizationNotFoundException;
import app.teamwize.api.organization.service.OrganizationService;
import app.teamwize.api.team.domain.exception.TeamNotFoundException;
import app.teamwize.api.team.service.TeamService;
import app.teamwize.api.user.domain.UserRole;
import app.teamwize.api.user.domain.UserStatus;
import app.teamwize.api.user.domain.entity.User;
import app.teamwize.api.user.domain.event.UserInvitedEvent;
import app.teamwize.api.user.domain.request.AdminUserCreateRequest;
import app.teamwize.api.user.domain.request.UserCreateRequest;
import app.teamwize.api.user.domain.request.UserFilterRequest;
import app.teamwize.api.user.domain.request.UserUpdateRequest;
import app.teamwize.api.user.exception.*;
import app.teamwize.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import java.util.List;

import static app.teamwize.api.user.repository.UserSpecifications.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final OrganizationService organizationService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TeamService teamService;
    private final AssetService assetService;
    private final LeavePolicyService leavePolicyService;
    private final EventService eventService;

    public void checkIfUserExists(String email) throws UserAlreadyExistsException {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User already exists with email: " + email);
        }
    }

    public User getUser(long organizationId, long userId) throws UserNotFoundException {
        return getById(organizationId, userId);
    }

    public User getUserByEmail(Long organizationId, String email) throws UserNotFoundException {
        return userRepository.findByOrganizationIdAndEmail(organizationId, email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    public User getUserByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    public UserDetails getUserDetails(Long id) throws UserNotFoundException {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(null));

        return AuthUserDetails.build(user);
    }

    @Transactional
    public User createOrganizationAdmin(Long organizationId, Long teamId, AdminUserCreateRequest request) throws UserAlreadyExistsException, TeamNotFoundException, OrganizationNotFoundException, LeavePolicyNotFoundException {
        var organization = organizationService.getOrganization(organizationId);
        checkIfUserExists(request.email());
        var team = teamService.getTeam(organizationId, teamId);
        var leavePolicy = leavePolicyService.getLeavePolicy(organizationId, request.leavePolicyId());
        var user = new User()
                .setStatus(UserStatus.ENABLED)
                .setEmail(request.email())
                .setRole(UserRole.ORGANIZATION_ADMIN)
                .setFirstName(request.firstName())
                .setLastName(request.lastName())
                .setPhone(request.phone())
                .setTimezone(request.timezone())
                .setCountry(request.country())
                .setTeam(team)
                .setOrganization(organization)
                .setLeavePolicy(leavePolicy)
                .setJoinedAt(Instant.now());
        user.setTeam(team);
        user.setPassword(passwordEncoder.encode(request.password()));
        return userRepository.merge(user);
    }

    @Transactional
    public User createUser(Long organizationId, Long inviterUserId, UserCreateRequest request)
            throws UserAlreadyExistsException, OrganizationNotFoundException, TeamNotFoundException, UserNotFoundException, PermissionDeniedException, LeavePolicyNotFoundException {
        var organization = organizationService.getOrganization(organizationId);
        var team = teamService.getTeam(organizationId, request.teamId());
        var leavePolicy = leavePolicyService.getLeavePolicy(organizationId, request.leavePolicyId());

        checkIfUserExists(request.email());
        var inviterUser = userRepository.findById(inviterUserId).orElseThrow(() -> new UserNotFoundException("Inviter user not found, id: " + inviterUserId));
        if (inviterUser.getRole() != UserRole.ORGANIZATION_ADMIN) {
            throw new PermissionDeniedException("Only organization admin can create users");
        }
        var user = new User()
                .setStatus(UserStatus.ENABLED)
                .setEmail(request.email())
                .setRole(request.role())
                .setFirstName(request.firstName())
                .setLastName(request.lastName())
                .setPhone(request.phone())
                .setTimezone(request.timezone())
                .setCountry(request.country())
                .setTeam(team)
                .setOrganization(organization)
                .setLeavePolicy(leavePolicy)
                .setJoinedAt(request.joinedAt() == null ? Instant.now() : request.joinedAt());

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        user = userRepository.persist(user);
        eventService.emmit(organizationId, new UserInvitedEvent(new UserEventPayload(user), new OrganizationEventPayload(organization)));
        return user;
    }

    @Transactional
    public User partiallyUpdateUser(Long organizationId, Long userId, UserUpdateRequest request)
            throws UserNotFoundException, UserAlreadyExistsException, AssetNotFoundException, LeavePolicyNotFoundException, TeamNotFoundException {
        var user = getById(organizationId, userId);

        if (request.getEmail() != null && request.getEmail().isPresent() && !request.getEmail().get().equals(user.getEmail())) {
            checkIfUserExists(request.getEmail().get());
            user.setEmail(request.getEmail().get());
        }
        if (request.getFirstName() != null) {
            request.getFirstName().ifPresent(user::setFirstName);
        }
        if (request.getLastName() != null) {
            request.getLastName().ifPresent(user::setLastName);
        }
        if (request.getPhone() != null) {
            request.getPhone().ifPresent(user::setPhone);
        }

        if (request.getJoinedAt() != null) {
            request.getJoinedAt().ifPresent(user::setJoinedAt);
        }

        if (request.getAvatarAssetId() != null && request.getAvatarAssetId().isPresent()) {
            var asset = assetService.getAsset(organizationId, request.getAvatarAssetId().get());
            user.setAvatar(asset);
        }

        if (request.getLeavePolicyId() != null && request.getLeavePolicyId().isPresent()) {
            var leavePolicy = leavePolicyService.getLeavePolicy(organizationId, request.getLeavePolicyId().get());
            user.setLeavePolicy(leavePolicy);
        }

        if (request.getTeamId() != null && request.getTeamId().isPresent()) {
            var team = teamService.getTeam(organizationId, request.getTeamId().get());
            user.setTeam(team);
        }

        return userRepository.update(user);
    }


    @Transactional
    public User changeUserStatus(long organizationId, long adminUserId, long targetUserId, UserStatus status)
            throws UnableToDisableCurrentUserException, UserNotFoundException {
        if (adminUserId == targetUserId) {
            throw new UnableToDisableCurrentUserException("Unable to disable current user");
        }

        var user = getById(organizationId, targetUserId);
        user.setStatus(status);
        return userRepository.update(user);
    }

    @Transactional
    public void changePassword(Long organizationId, Long userId, String currentPassword, String newPassword)
            throws UserNotFoundException, IncorrectPasswordException {
        var user = getById(organizationId, userId);

        checkPasswordMatch(currentPassword, user.getPassword());

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.update(user);
    }

    @Transactional
    public void resetPassword(Long organizationId, Long userId, String password) throws UserNotFoundException {
        var user = getById(organizationId, userId);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.update(user);
    }

    public List<User> getUsersByTeam(Long organizationId, Long teamId, List<UserRole> roles) {
        return userRepository.findByOrganizationIdAndTeamIdAndRoleIsIn(organizationId, teamId, roles);
    }

    public List<User> getUsersByRole(Long organizationId, UserRole role) {
        return userRepository.findByOrganizationIdAndRole(organizationId, role);
    }

    private User getById(Long organizationId, Long userId) throws UserNotFoundException {
        return userRepository.findByOrganizationIdAndId(organizationId, userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    private void checkPasswordMatch(String rawPassword, String encodedPassword) throws IncorrectPasswordException {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new IncorrectPasswordException("Password does not match");
        }
    }

    public Page<User> getUsers(Long organizationId, UserFilterRequest request, PaginationRequest page) {
        var sort = Sort.by("id").descending();
        var pageRequest = PageRequest.of(page.getPageNumber(), page.getPageSize(), sort);

        var specs = hasOrganizationId(organizationId);

        specs = specs.and(hasNotDeleted());

        if (request.teamId() != null) {
            specs = specs.and(hasTeamId(request.teamId()));
        }
        if (request.searchTerm() != null) {
            specs = specs.and(hasNameLike(request.searchTerm()));
        }
        return userRepository.findAll(specs, pageRequest);
    }
}
