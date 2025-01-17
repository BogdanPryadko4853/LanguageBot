package com.trenning.learnlanguagebot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "t_user")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long chatId;
    @Enumerated(value = EnumType.STRING)
    private UserState userState;
    private int currentWordIndex;
    private String currentLanguage;
    private Integer rating = 0;

    @ManyToMany(mappedBy = "users")
    private List<Word> words;



}