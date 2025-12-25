package ru.practicum.server.user.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.server.user.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)", nativeQuery = true)
    boolean existsByEmail(@Param("email") String email);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM users WHERE email = :email AND id != :id)", nativeQuery = true)
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
}
