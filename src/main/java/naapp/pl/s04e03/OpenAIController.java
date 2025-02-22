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
                "pierwszy rząd to kolejno od: \n" +
                "pierwsza kolumna: Start nawigacji\n" +
                "druga kolumna: Trawa\n" +
                "trzecia kolumna: Jedno drzewo\n" +
                "czwarta kolumna: Dom wiejski\n" +
                "\n" +
                "drugi rząd od góry to kolejno od lewej:\n" +
                "pierwsza kolumna: Trawa\n" +
                "druga kolumna: Wiatrak drewniany\n" +
                "trzecia kolumna: Trawa\n" +
                "czwarta kolumna: Trawa \n" +
                "\n" +
                "trzeci rzad to kolejno od lewej:\n" +
                "pierwsza kolumna: Trawa\n" +
                "druga kolumna: Trawa\n" +
                "trzecia kolumna: Male skaly\n" +
                "czwarta kolumna: Dwa drzewa\n" +
                "\n" +
                "czwarty rząd to kolejno od lewej:\n" +
                "pierwsza kolumna: Duze skaly\n" +
                "druga kolumna: Duze skaly\n" +
                "trzecia kolumna: Auto\n" +
                "czwarta kolumna: Wejscie jaskini\n" +
                instruction +
                "\n" +
                "W odpowiedzi zwróć tylko zawartość kwadrata nic poza tym. Dokładnie taką jaką Ci podaję w opisie. jak będzie instrukcja w prawo/lewo to przechodź po kolumnach, jak góra/dół to po wierszach. Wiesze numerowałem Ci od góry do dołu. A kolumny od lewej do prawej." +
                "Jeśli instrukcja będzie taka: 'Dobra. To co? zaczynamy? Odpalam silniki. Czas na kolejny lot. Jeste? moimi oczami. Lecimy w d??, albo nie! nie! czekaaaaj. Polecimy wiem jak. W prawo i dopiero teraz w d??. Tak b?dzie OK. Co widzisz?' to zwróć Wejscie jaskini" +
                "");

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
