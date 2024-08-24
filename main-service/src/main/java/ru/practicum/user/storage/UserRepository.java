package ru.practicum.user.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.user.model.User;

import java.util.Set;

public interface UserRepository extends JpaRepository<User, Integer> {
    Page<User> findAllByIdIn(Set<Integer> userId, Pageable pageable);
    User findByEmail(String email);
}
