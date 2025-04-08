package com.ecom.productcatalog.repository;

import com.ecom.productcatalog.model.Role;
import com.ecom.productcatalog.model.Role.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
