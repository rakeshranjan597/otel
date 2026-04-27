package com.rak.java.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(value = "v1")
public class TestController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/test/otel")
    public ResponseEntity<String> testOtel(
            @RequestHeader(value = "Authorization", required = false) String auth
    ) {
        System.out.println("Received Auth: " + auth);
        String url = "http://abcraj.free.beeceptor.com";
        String body = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(createHeaders()), String.class).getBody();
        System.out.println("Completed");
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    public HttpHeaders createHeaders() {
        return new HttpHeaders() {{
            set("Authorization", "QWRtaW46UGFzc3dvcmQ=");
            set("Content-Type", "application/json");
        }};
    }
}
