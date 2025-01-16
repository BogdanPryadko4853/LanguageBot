package com.trenning.learnlanguagebot.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryResponse {
    private String word;
    private String phonetic;
    private List<Phonetic> phonetics;
    private List<Meaning> meanings;


    @Getter
    @Setter
    public static class Phonetic {
        private String text;
        private String audio;

    }

    @Getter
    @Setter
    public static class Meaning {
        private String partOfSpeech;
        private List<Definition> definitions;
    }

    @Getter
    @Setter
    public static class Definition {
        private String definition;
        private String example;
        private List<String> synonyms;
        private List<String> antonyms;

    }
}