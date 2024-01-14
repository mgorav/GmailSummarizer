package com.gonnect.gmail.email;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class EmailService {
    private static final String APPLICATION_NAME = "GmailSummary";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @Value("${google.credentials-file-path}")
    Resource credentialsFilePath;

    private final NetHttpTransport httpTransport;
    private final JsonFactory jsonFactory;
    private GoogleAuthorizationCodeFlow flow;

    public EmailService() {
        this.httpTransport = new NetHttpTransport();
        this.jsonFactory = new GsonFactory();
    }

    @PostConstruct
    private void init() throws IOException {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                jsonFactory,
                new FileReader(credentialsFilePath.getFile())
        );

        flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, Collections.singletonList(GmailScopes.GMAIL_READONLY))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
    }

    public String getAuthorizationUrl() {
        return flow.newAuthorizationUrl().setRedirectUri(redirectUri).build();
    }

    public Credential exchangeCodeForTokens(String code) throws IOException {
        GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
        return flow.createAndStoreCredential(response, "gonnect.uk@gmail.com");
    }

    public List<Message> fetchEmails(String userId) throws IOException {
        Credential credential = flow.loadCredential(userId);
        if (credential == null) {
            throw new IllegalStateException("User credentials not found for " + userId);
        }

        Gmail service = new Gmail.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        ListMessagesResponse response = service.users().messages().list(userId).execute();
        return response.getMessages();
    }
}
