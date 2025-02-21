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
        String prompt = request.getOrDefault("prompt", "Say something smart");
        String instruction = request.getOrDefault("instruction", "Default instruction");
        System.out.println("instruction: " + instruction);

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

        sendReport();

        return ResponseEntity.ok(response.getBody());
    }

    private org.springframework.http.HttpHeaders getHeaders() {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + openAiApiKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    private void sendReport() {
        String reportUrl = "https://centrala.ag3nts.org/report";
        Map<String, String> reportData = new HashMap<>();
        reportData.put("apikey", "991d2a94-9981-4b94-ac19-9518fc43832e");
        reportData.put("description", "https://s04e03-production.up.railway.app/api/image/prompt");
        reportData.put("task", "webhook");

        restTemplate.postForEntity(
                reportUrl,
                new org.springframework.http.HttpEntity<>(reportData, getHeaders()),
                String.class
        );
    }
}
