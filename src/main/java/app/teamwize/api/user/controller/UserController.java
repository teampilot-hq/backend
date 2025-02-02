package app.teamwize.api.user.controller;


import app.teamwize.api.assets.domain.exception.AssetNotFoundException;
import app.teamwize.api.auth.service.SecurityService;
import app.teamwize.api.base.domain.model.request.PaginationRequest;
import app.teamwize.api.base.domain.model.response.PagedResponse;
import app.teamwize.api.base.mapper.PagedResponseMapper;
import app.teamwize.api.leave.exception.LeavePolicyNotFoundException;
import app.teamwize.api.organization.exception.OrganizationNotFoundException;
import app.teamwize.api.team.domain.exception.TeamNotFoundException;
import app.teamwize.api.user.domain.request.*;
import app.teamwize.api.user.domain.response.UserResponse;
import app.teamwize.api.user.exception.*;
import app.teamwize.api.user.mapper.UserMapper;
import app.teamwize.api.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityService securityService;
    private final UserMapper userMapper;
    private final PagedResponseMapper pagedResponseMapper;

    @GetMapping
    public PagedResponse<UserResponse> getUsers(@ParameterObject @Valid UserFilterRequest filter,
                                                @ParameterObject @Valid PaginationRequest page) {
        var pagedUsers = userService.getUsers(securityService.getUserOrganizationId(), filter, page);
        return pagedResponseMapper.toPagedResponse(
                userMapper.toUserResponses(pagedUsers.getContent()),
                pagedUsers.getNumber(),
                pagedUsers.getSize(),
                pagedUsers.getTotalPages(),
                pagedUsers.getTotalElements()
        );
    }

    @PostMapping
    public UserResponse createUser(@RequestBody UserCreateRequest request) throws UserAlreadyExistsException, OrganizationNotFoundException, TeamNotFoundException, UserNotFoundException, PermissionDeniedException, LeavePolicyNotFoundException {
        var user = userService.createUser(securityService.getUserOrganizationId(), securityService.getUserId(), request);
        return userMapper.toUserResponse(user);
    }

    @GetMapping("mine")
    public UserResponse getMyProfile() throws UserNotFoundException {
        return userMapper.toUserResponse(userService.getUser(securityService.getUserOrganizationId(), securityService.getUserId()));
    }

    @PatchMapping("mine")
    public UserResponse updateMyProfile(@RequestBody @Valid UserUpdateRequest request)
            throws UserNotFoundException, UserAlreadyExistsException, AssetNotFoundException, TeamNotFoundException, LeavePolicyNotFoundException {
        var organizationId = securityService.getUserOrganizationId();
        return userMapper.toUserResponse(userService.partiallyUpdateUser(organizationId, securityService.getUserId(), request));
    }

    @GetMapping("{userId}")
    public UserResponse getUser(@PathVariable Long userId) throws UserNotFoundException {
        return userMapper.toUserResponse(userService.getUser(securityService.getUserOrganizationId(), userId));
    }

    @PatchMapping("{userId}")
    public UserResponse updateProfile(@PathVariable Long userId, @RequestBody @Valid UserUpdateRequest request)
            throws UserNotFoundException, UserAlreadyExistsException, AssetNotFoundException, TeamNotFoundException, LeavePolicyNotFoundException {
        var organizationId = securityService.getUserOrganizationId();
        return userMapper.toUserResponse(userService.partiallyUpdateUser(organizationId, userId, request));
    }


    @PatchMapping("{userId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeUserStatus(@PathVariable long userId, @RequestBody @Valid UserChangeStatusRequest request)
            throws UnableToDisableCurrentUserException, UserNotFoundException {
        userService.changeUserStatus(securityService.getUserOrganizationId(), securityService.getUserId(),
                userId, request.getStatus());
    }

    @PatchMapping("{userId}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody @Valid ChangePasswordRequest request)
            throws UserNotFoundException, IncorrectPasswordException {
        userService.changePassword(securityService.getUserOrganizationId(), securityService.getUserId(),
                request.getCurrentPassword(), request.getNewPassword());
    }


}
