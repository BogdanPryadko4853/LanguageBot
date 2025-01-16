package com.trenning.learnlanguagebot.client;

import com.trenning.learnlanguagebot.entity.Word;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class DictionaryApiClient {

    private static final String YANDEX_DICTIONARY_API_URL = "https://dictionary.yandex.net/api/v1/dicservice.json/lookup";
    private static final String API_KEY = "dict.1.1.20250115T184656Z.f679fcb6c3e80a4b.80c2dcd5ed6dafd3a1fd69835813dad18b9bf2cb";


    public Word getWordInfo(String word, String language) {
        RestTemplate restTemplate = new RestTemplate();
        String url = YANDEX_DICTIONARY_API_URL + "?key=" + API_KEY + "&lang=" + language + "-ru&text=" + word;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                List<Map<String, Object>> definitions = (List<Map<String, Object>>) responseBody.get("def");
                if (definitions != null && !definitions.isEmpty()) {

                    Map<String, Object> firstDefinition = definitions.get(0);

                    String transcription = (String) firstDefinition.get("ts");

                    List<Map<String, Object>> translations = (List<Map<String, Object>>) firstDefinition.get("tr");
                    if (translations != null && !translations.isEmpty()) {

                        Map<String, Object> firstTranslation = translations.get(0);
                        String translation = (String) firstTranslation.get("text");

                        return new Word(word, translation, transcription);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}


