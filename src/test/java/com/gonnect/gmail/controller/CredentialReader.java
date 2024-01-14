package com.gonnect.gmail.controller;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.File;
import java.util.HashMap;
import com.google.api.client.auth.oauth2.StoredCredential;

public class CredentialReader {

    public static void readStoredCredential(String filePath) {
        try {
            File file = new File(filePath);
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            // Read the HashMap from the file
            HashMap<String, StoredCredential> credentials = (HashMap<String, StoredCredential>) in.readObject();

            // Iterate through the HashMap and print details
            for (String key : credentials.keySet()) {
                StoredCredential storedCredential = credentials.get(key);
                System.out.println("User: " + key);
                System.out.println("Access Token: " + storedCredential.getAccessToken());
                System.out.println("Refresh Token: " + storedCredential.getRefreshToken());
                System.out.println("Expiration Time: " + storedCredential.getExpirationTimeMilliseconds());
            }

            in.close();
            fileIn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String filePath = "/Users/gauravmalhotra/Documents/MyGithub/GmailSummary/tokens/StoredCredential";
        readStoredCredential(filePath);
    }
}

