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
                "pierwsza kolumna start nawigacji, druga to trawa na łące, druga to jedno drzewo, trzecia dom\n" +
                "\n" +
                "drugi rząd to kolejno od lewej:\n" +
                "pierwsza kolumna trawa, druga młyn, trzecia trawa, czwarta trawa\n" +
                "\n" +
                "trzeci rząd to kolejno od lewej:\n" +
                "pierwsza kolumna trawa, druga trawa, trzecia małe skały, czwarta dwa drzewa\n" +
                "\n" +
                "czwarty rząd to kolejno od lewej:\n" +
                "pierwsza kolumna duże skały, druga duże skały, trzecia samochód, czwarta jaskinia\n" +
                "\n" +
                "startujemy z kwadratu 1 wiersz 1 kolumna\n" +
                "nawiguj teraz po tych kwadratach według instrukcji i powiedz co jest w kwadracie do którego trafisz według takiego pytania:\n" +
                instruction +
                "\n" +
                "W odpowiedzi zwróć tylko zawartość kwadrata nic poza tym");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", prompt)
        });
        requestBody.put("max_tokens", 100);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                new org.springframework.http.HttpEntity<>(requestBody, getHeaders()),
                Map.class
        );
        System.out.println("response: " + response.getBody());


        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("description", response.getBody().toString());

        return ResponseEntity.ok(responseBody);
    }

    private org.springframework.http.HttpHeaders getHeaders() {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + openAiApiKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
