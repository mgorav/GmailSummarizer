package com.gonnect.gmail.controller;

import com.gonnect.gmail.email.EmailService;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/login/google")
    public String redirectToGoogle() {
        String authorizationUrl = emailService.getAuthorizationUrl();
        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/oauth2callback")
    public String saveAuthorizationCode(@RequestParam("code") String code) throws IOException {
        emailService.exchangeCodeForTokens(code);
        return "Code received and tokens stored";
    }

    @GetMapping("/emails")
    public Flux<Message> getEmails(
            @RequestParam String userId,
            @RequestParam(required = false) Instant startTime,
            @RequestParam(required = false) Instant endTime,
            @RequestParam(required = false) String keywords
    ) throws IOException {
        List<Message> allEmails = emailService.fetchEmails(userId);

        List<Message> filteredEmails = allEmails.stream()
                .filter(email -> {
                    if (startTime != null && email.getInternalDate() != null) {
                        Instant emailDate = Instant.ofEpochMilli(email.getInternalDate());
                        if (emailDate.isBefore(startTime)) {
                            return false;
                        }
                    }

                    if (endTime != null && email.getInternalDate() != null) {
                        Instant emailDate = Instant.ofEpochMilli(email.getInternalDate());
                        if (emailDate.isAfter(endTime)) {
                            return false;
                        }
                    }

                    if (keywords != null) {
                        String emailSubject = getEmailSubject(email) != null ? getEmailSubject(email).toLowerCase() : "";
                        String emailBody = getEmailBody(email) != null ? getEmailBody(email).toLowerCase() : "";

                        for (String keyword : keywords.split(",")) {
                            keyword = keyword.trim().toLowerCase();
                            if (!emailSubject.contains(keyword) && !emailBody.contains(keyword)) {
                                return false;
                            }
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        return Flux.fromIterable(filteredEmails);
    }

    private String getEmailSubject(Message message) {
        if (message != null && message.getPayload() != null) {
            List<MessagePartHeader> headers = message.getPayload().getHeaders();
            for (MessagePartHeader header : headers) {
                if ("Subject".equals(header.getName())) {
                    return header.getValue();
                }
            }
        }
        return null;
    }

    private String getEmailBody(Message message) {
        if (message != null && message.getPayload() != null) {
            MessagePart payload = message.getPayload();
            if (payload.getBody() != null && payload.getBody().getData() != null) {
                byte[] bodyBytes = Base64.getUrlDecoder().decode(payload.getBody().getData());
                return new String(bodyBytes, StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
