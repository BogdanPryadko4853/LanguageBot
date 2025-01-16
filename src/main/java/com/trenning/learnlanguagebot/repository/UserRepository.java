package com.trenning.learnlanguagebot.repository;

import com.trenning.learnlanguagebot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    User findByChatId(Long chatId);
    User findByName(String name);
}
