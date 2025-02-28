package naapp.pl.AI_DEVS_S05_03;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class OpenAIController {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @PostMapping("/s5e3/prompt")
    public ResponseEntity<?> s5e3(@RequestBody Map<String, String> request) {
        String URL = "https://rafal.ag3nts.org/b46c3/";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();

        // 1. Pobranie hasha do podpisania
        String requestBodyPass = "{\"password\":\"NONOMNISMORIAR\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyPass, headers);
        ResponseEntity<String> responsePass = restTemplate.exchange(URL, HttpMethod.POST, requestEntity, String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(responsePass.getBody());
            String hashToSign = jsonNode.get("message").asText();

            // 2. Podpisanie i pobranie informacji
            String requestBodySign = "{\"sign\":\"" + hashToSign + "\"}";
            requestEntity = new HttpEntity<>(requestBodySign, headers);
            ResponseEntity<String> responseSign = restTemplate.exchange(URL, HttpMethod.POST, requestEntity, String.class);
            JsonNode jsonNodeSign = objectMapper.readTree(responseSign.getBody());
            System.out.println(jsonNodeSign);

            // 3. Pobranie danych z challenges
            ArrayNode challenges = (ArrayNode) jsonNodeSign.get("message").get("challenges");
            String timestamp = jsonNodeSign.get("message").get("timestamp").asText();
            String signature = jsonNodeSign.get("message").get("signature").asText();
            List<Future<String>> futures = new ArrayList<>();

            for (JsonNode challengeNode : challenges) {
                futures.add(executorService.submit(() -> processChallenge(challengeNode.asText())));
            }

            List<String> results = new ArrayList<>();
            for (Future<String> future : futures) {
                results.add(future.get());
            }

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("apikey", "7d179ddfc99711c531b6b2d9653368da");
            requestBody.put("timestamp", timestamp);
            requestBody.put("signature", signature);
            requestBody.put("answer", results.toString());

            System.out.println("===========================================");
            System.out.println(requestBody);
            ResponseEntity<String> finalResponse = restTemplate.postForEntity(URL, new HttpEntity<>(requestBody, getHeaders()), String.class);
            System.out.println(finalResponse.getBody());
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd przetwarzania");
        }
    }

    private String processChallenge(String challengeUrl) {
        try {
            ResponseEntity<String> challengeResponse = restTemplate.getForEntity(challengeUrl, String.class);
            JsonNode challengeData = new ObjectMapper().readTree(challengeResponse.getBody());
            String task = challengeData.get("task").asText();
            ArrayNode dataArray = (ArrayNode) challengeData.get("data");
            List<String> data = new ArrayList<>();
            for (JsonNode dataNode : dataArray) {
                data.add(dataNode.asText());
            }
            if (task.endsWith(".html")) {
                Matcher matcher = Pattern.compile("(https?://[^\s]+.html)").matcher(task);
                task = matcher.find() ? matcher.group(1) : null;
                ResponseEntity<String> htmlResponse = restTemplate.getForEntity(task, String.class);
                task = extractTextFromHtml(htmlResponse.getBody());
            }
            return executeTask(task, data);
        } catch (Exception e) {
            e.printStackTrace();
            return "Błąd przetwarzania zadania";
        }
    }

    private String executeTask(String task, List<String> data) {
        String prompt = "Odpowiadaj krótko na pytania na podstawie podanych danych. Tylko same odpowiedzi, nie zdania. Odpowiedzi zwróć w formie tabeli stringów, żebym mógł je od razu wrzucić do tablicy. Wytnij wszystkie znaki nowych linii i cudzysłowia, odpowiedzi odseparuj przecinkiem. Jak będzie pytanie o rozwinięcie skrótu to znajdź w kontekście pełną nazwę." +
                "\nKontekst: " + task +
                "\nPytania: " + String.join(", ", data);
        return promptOpenAI(prompt);
    }

    private String promptOpenAI(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        requestBody.put("max_tokens", 50);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                new HttpEntity<>(requestBody, getHeaders()),
                Map.class
        );

        if (response.getBody() != null && response.getBody().containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (!choices.isEmpty() && choices.get(0).containsKey("message")) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        }
        return "";
    }

    private String extractTextFromHtml(String htmlContent) {
        return Jsoup.parse(htmlContent).text();
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openAiApiKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
