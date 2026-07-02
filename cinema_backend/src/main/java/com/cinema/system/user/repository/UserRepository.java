package com.cinema.system.user.repository;

import com.cinema.system.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT * FROM user WHERE BINARY user_id = :userId", nativeQuery = true)
    Optional<User> findByUserId(@Param("userId") String userId);
}
