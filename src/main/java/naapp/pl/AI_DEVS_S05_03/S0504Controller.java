package naapp.pl.AI_DEVS_S05_03;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/s0504")
public class S0504Controller {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @PostMapping("/task")
    public ResponseEntity<?> s0504(@RequestBody Map<String, String> request) {
        return null;
    }

}