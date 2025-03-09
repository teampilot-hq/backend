package app.teamwize.api.user.repository;

import app.teamwize.api.user.domain.UserRole;
import app.teamwize.api.user.domain.entity.User;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends BaseJpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @EntityGraph(attributePaths = {"organization", "team", "avatar"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<User> findByOrganizationIdAndId(long organizationId, long userId);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"organization", "team", "avatar"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"organization", "team", "avatar"}, type = EntityGraph.EntityGraphType.FETCH)
    List<User> findByOrganizationId(Long organization);

    @EntityGraph(attributePaths = {"organization", "team", "avatar"}, type = EntityGraph.EntityGraphType.FETCH)
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"organization", "team", "avatar"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<User> findByOrganizationIdAndEmail(Long organizationId, String email);

    @EntityGraph(attributePaths = {"organization", "team", "avatar"}, type = EntityGraph.EntityGraphType.FETCH)
    List<User> findByOrganizationIdAndTeamIdAndRoleIsIn(Long organizationId, Long teamId, List<UserRole> roles);

    @EntityGraph(attributePaths = {"organization", "team", "avatar"}, type = EntityGraph.EntityGraphType.FETCH)
    List<User> findByOrganizationIdAndRole(Long organizationId, UserRole role);
}
