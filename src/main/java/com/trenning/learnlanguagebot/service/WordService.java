
package com.trenning.learnlanguagebot.service;

import com.trenning.learnlanguagebot.client.DictionaryApiClient;
import com.trenning.learnlanguagebot.entity.User;
import com.trenning.learnlanguagebot.entity.Word;
import com.trenning.learnlanguagebot.repository.WordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WordService {
    private final WordRepository wordRepository;
    private final DictionaryApiClient dictionaryApiClient;
    private final UserService userService;

    public Optional<Word> findWord(String word) {
        return wordRepository.findByWord(word);
    }

    @Transactional
    public Optional<Word> searchAndSaveWord(String word, User user) {

        User managedUser = userService.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Word> existingWord = wordRepository.findByWordAndUserAndLanguage(word, managedUser.getCurrentLanguage(), managedUser.getId());
        if (existingWord.isPresent()) {
            return existingWord;
        }

        Word wordInfo = dictionaryApiClient.getWordInfo(word, managedUser.getCurrentLanguage());
        if (wordInfo == null) {
            return Optional.empty();
        }
        wordInfo.setLanguage(managedUser.getCurrentLanguage());
        wordInfo.getUsers().add(managedUser);

        wordRepository.save(wordInfo);

        return Optional.of(wordInfo);
    }

    public Optional<Word> findById(Long id){
        return wordRepository.findById(id);
    }

    public List<Word> getUserWords(User user) {
        return wordRepository.findByUserAndLanguage(user.getId(), user.getCurrentLanguage());
    }

    @Transactional
    public void deleteWordForUser(Word word, User user) {

        Word managedWord = wordRepository.findById(word.getId())
                .orElseThrow(() -> new RuntimeException("Word not found"));

        User managedUser = userService.findByChatId(user.getChatId());

        managedUser.getWords().remove(managedWord);

        managedWord.removeUser(managedUser);

        userService.save(managedUser);
        wordRepository.save(managedWord);
    }
}


