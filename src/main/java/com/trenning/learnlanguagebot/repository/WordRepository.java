package com.trenning.learnlanguagebot.repository;

import com.trenning.learnlanguagebot.entity.User;
import com.trenning.learnlanguagebot.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    @Query("SELECT w FROM Word w JOIN w.users u WHERE w.word = :word AND w.language = :language AND u.id = :userId")
    Optional<Word> findByWordAndUserAndLanguage(@Param("word") String word, @Param("language") String language, @Param("userId") Long userId);

    @Query("select w from Word w where w.word = :word")
    Optional<Word> findByWord(String word);

    @Query("SELECT w FROM Word w JOIN w.users u WHERE u.id = :userId AND w.language = :language")
    List<Word> findByUserAndLanguage(@Param("userId") Long userId, @Param("language") String language);

}
