package com.trenning.learnlanguagebot.service;

import com.trenning.learnlanguagebot.entity.User;
import com.trenning.learnlanguagebot.entity.UserState;
import com.trenning.learnlanguagebot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findByChatId(Long chatId) {
        return userRepository.findByChatId(chatId);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public void updateUserState(Long chatId, UserState state) {
        User user = findByChatId(chatId);
        if (user != null) {
            user.setUserState(state);
            save(user);
        }
    }

    public void resetUserState(Long chatId) {
        updateUserState(chatId, UserState.DEFAULT);
    }
    public Optional<User> findById(Long id){
        return userRepository.findById(id);
    }
}