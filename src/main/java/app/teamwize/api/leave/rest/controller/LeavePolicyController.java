package app.teamwize.api.leave.rest.controller;

import app.teamwize.api.auth.service.SecurityService;
import app.teamwize.api.leave.exception.LeavePolicyNotFoundException;
import app.teamwize.api.leave.exception.LeaveTypeNotFoundException;
import app.teamwize.api.leave.rest.mapper.LeavePolicyRestMapper;
import app.teamwize.api.leave.rest.model.request.LeavePolicyCreateRequest;
import app.teamwize.api.leave.rest.model.request.LeavePolicyUpdateRequest;
import app.teamwize.api.leave.rest.model.response.LeavePolicyResponse;
import app.teamwize.api.leave.service.LeavePolicyService;
import app.teamwize.api.organization.exception.OrganizationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("leaves/policies")
@RequiredArgsConstructor
public class LeavePolicyController {

    private final LeavePolicyService leavePolicyService;
    private final LeavePolicyRestMapper leavePolicyRestMapper;
    private final SecurityService securityService;

    @PostMapping
    public LeavePolicyResponse createLeavePolicy(@RequestBody LeavePolicyCreateRequest request)
            throws OrganizationNotFoundException, LeaveTypeNotFoundException {
        var leaveType = leavePolicyService.createLeavePolicy(
                securityService.getUserOrganizationId(),
                leavePolicyRestMapper.toNewLeavePolicy(request)
        );
        return leavePolicyRestMapper.toResponse(leaveType);
    }

    @GetMapping
    public List<LeavePolicyResponse> getLeavePolicies() {
        var leaveTypes = leavePolicyService.getLeavePolicies(securityService.getUserOrganizationId());
        return leaveTypes.stream()
                .map(leavePolicyRestMapper::toResponse)
                .toList();
    }

    @GetMapping("{id}")
    public LeavePolicyResponse getLeavePolicy(@PathVariable Long id) throws LeavePolicyNotFoundException {
        var leaveType = leavePolicyService.getLeavePolicy(securityService.getUserOrganizationId(), id);
        return leavePolicyRestMapper.toResponse(leaveType);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    public void deleteLeavePolicy(@PathVariable Long id) {
        leavePolicyService.deleteLeavePolicy(securityService.getUserOrganizationId(), id);
    }


    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("{id}/default")
    public void updateDefaultPolicy(@PathVariable Long id) {
        leavePolicyService.updateDefaultPolicy(securityService.getUserOrganizationId(), id);
    }

    @PutMapping("{id}")
    public LeavePolicyResponse updateLeavePolicy(@PathVariable Long id, @RequestBody LeavePolicyUpdateRequest request)
            throws OrganizationNotFoundException, LeaveTypeNotFoundException, LeavePolicyNotFoundException {
        var leaveType = leavePolicyService.updateLeavePolicy(
                securityService.getUserOrganizationId(),
                id,
                leavePolicyRestMapper.toUpdateLeavePolicy(request)
        );
        return leavePolicyRestMapper.toResponse(leaveType);
    }


}
