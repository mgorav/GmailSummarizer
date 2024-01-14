package com.gonnect.gmail.controller;

import com.gonnect.gmail.email.EmailService;
import com.google.api.services.gmail.model.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate.HttpClientOption;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmailControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmailService emailService;

    @Test
    public void testGetEmails() throws IOException {
        // Assuming you have actual emails in the Gmail account for testing

        String userId = "gonnect.uk@gmail.com";
        Instant startTime = Instant.parse("2022-01-01T00:00:00Z");
        Instant endTime = Instant.parse("2022-12-31T23:59:59Z");
        String keywords = "vibe";

        // Prepare the URL for the GET request
        String url = "http://localhost:" + port + "/emails?userId=" + userId +
                "&startTime=" + startTime + "&endTime=" + endTime + "&keywords=" + keywords;

        // Send the GET request to the controller
        ResponseEntity<Message[]> responseEntity = restTemplate.getForEntity(url, Message[].class);

        // Check the response status code
        assertEquals(200, responseEntity.getStatusCodeValue());

        // Get the list of messages from the response
        Message[] responseMessages = responseEntity.getBody();

        // Ensure that the response is not null
        assertNotNull(responseMessages);

        // Fetch emails using the real EmailService
        List<Message> emailList = emailService.fetchEmails(userId);

        // Compare the size of the response with the size of the fetched emails
        assertEquals(emailList.size(), responseMessages.length);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestRestTemplate testRestTemplate() {
            return new TestRestTemplate(HttpClientOption.ENABLE_REDIRECTS);
        }
    }
}
