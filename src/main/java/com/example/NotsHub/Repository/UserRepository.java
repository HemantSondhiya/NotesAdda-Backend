package com.example.NotsHub.Repository;
import com.example.NotsHub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUserName(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUserName(String username);

    Boolean existsByEmail(String email);


}
