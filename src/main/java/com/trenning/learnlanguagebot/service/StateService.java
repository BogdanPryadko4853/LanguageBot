package com.trenning.learnlanguagebot.service;

import com.trenning.learnlanguagebot.entity.User;
import com.trenning.learnlanguagebot.entity.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StateService {
    private final UserService userService;

    public void setUserState(Long chatId, UserState state) {
        userService.updateUserState(chatId, state);
    }

    public void resetUserState(Long chatId) {
        userService.resetUserState(chatId);
    }

    public UserState getUserState(Long chatId) {
        User user = userService.findByChatId(chatId);
        return user != null ? user.getUserState() : UserState.DEFAULT;
    }

}