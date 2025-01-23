package com.example.stajirovka.repository;

import com.example.stajirovka.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query(value = "select t from User t where t.username=?1")
    User findByUsername(String username);

    @Query(value = "select t from User t where t.chatId =?1")
    User findByChatId(Long chatId);

    @Query("select t from User t where t.chatId=?1 and t.verificationCode=?2")
    User findByCodeAndChatId(Long chatId, String code);
}
