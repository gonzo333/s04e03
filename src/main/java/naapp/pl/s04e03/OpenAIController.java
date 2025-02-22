package naapp.pl.s04e03;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OpenAIController {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/image/prompt")
    public ResponseEntity<?> handlePrompt(@RequestBody Map<String, String> request) {

        String instruction = request.getOrDefault("instruction", "Default instruction");


        System.out.println("instruction: " + instruction);

        String prompt = request.getOrDefault("prompt", "ok teraz jest tak, mam obraz składający się z 16 kwadratów w układzie 4 na 4\n" +
                "\n" +
                "pierwszy rząd od lewej to kolejno: \n" +
                "1. Start nawigacji\n" +
                "2. Trawa łąkowa\n" +
                "3. Pojedyncze drzewo\n" +
                "4. Dom wiejski\n" +
                "\n" +
                "drugi rząd to kolejno od lewej:\n" +
                "1. Trawa łąkowa\n" +
                "2. Wiatrak drewniany\n" +
                "3. Trawa łąkowa\n" +
                "4. Trawa łąkowa\n" +
                "\n" +
                "trzeci rząd to kolejno od lewej:\n" +
                "1. Trawa łąkowa\n" +
                "2. Trawa łąkowa\n" +
                "3. Małe skały\n" +
                "4. Dwa drzewa\n" +
                "\n" +
                "czwarty rząd to kolejno od lewej:\n" +
                "pierwsza kolumna duże skały, druga duże skały, trzecia samochód, czwarta jaskinia\n" +
                "\n" +
                "startujemy z kwadratu 1 wiersz 1 kolumna\n" +
                "1. Duże skały\n" +
                "2. Duże skały\n" +
                "3. Samochód osobowy\n" +
                "4. Wejście jaskini\n" +
                instruction +
                "\n" +
                "W odpowiedzi zwróć tylko zawartość kwadrata nic poza tym");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", prompt)
        });
        requestBody.put("max_tokens", 100);

        System.out.println("Sending request");
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                new org.springframework.http.HttpEntity<>(requestBody, getHeaders()),
                Map.class
        );
        String content = "";
        if (response.getBody() != null && response.getBody().containsKey("choices")) {
            var choices = (java.util.List<Map<String, Object>>) response.getBody().get("choices");
            if (!choices.isEmpty() && choices.get(0).containsKey("message")) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                content = (String) message.get("content");
            }
        }
        System.out.println("Content: " + content);
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("description", content);

        return ResponseEntity.ok(responseBody);
    }

    private org.springframework.http.HttpHeaders getHeaders() {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + openAiApiKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
