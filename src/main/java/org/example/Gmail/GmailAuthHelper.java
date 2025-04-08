package org.example.Gmail;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.GmailScopes;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Scanner;

public class GmailAuthHelper {
    private static final String TOKENS_DIRECTORY = "tokens";
    private static final String CREDENTIALS_FILE = "credentialsSG.json";
    private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    public static void main(String[] args) {
        try {
            // 1. Загрузка client_secrets.json
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                    GsonFactory.getDefaultInstance(),
                    new InputStreamReader(new FileInputStream(CREDENTIALS_FILE))
            );

            // 2. Настройка AuthorizationCodeFlow
            AuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    clientSecrets,
                    Collections.singletonList(GmailScopes.GMAIL_READONLY)
            )
                    .setDataStoreFactory(new FileDataStoreFactory(Paths.get(TOKENS_DIRECTORY).toFile()))
                    .setAccessType("offline")
                    .build();

            // 3. Генерация URL для авторизации
            String url = flow.newAuthorizationUrl()
                    .setRedirectUri(REDIRECT_URI)
                    .build();

            System.out.println("1. Перейдите по ссылке в браузере:");
            System.out.println(url);
            System.out.println("2. Авторизуйтесь и скопируйте код");
            System.out.println("3. Вставьте код ниже и нажмите Enter");
            System.out.print("Код авторизации: ");

            // 4. Получение кода от пользователя
            String code = new Scanner(System.in).nextLine().trim();

            // 5. Обмен кода на токены
            TokenResponse response = flow.newTokenRequest(code)
                    .setRedirectUri(REDIRECT_URI)
                    .execute();

            // 6. Сохранение учетных данных
            Credential credential = flow.createAndStoreCredential(response, "user");

            System.out.println("\nАвторизация успешно завершена!");
            System.out.println("Access Token: " + credential.getAccessToken());
            System.out.println("Refresh Token: " + credential.getRefreshToken());
            System.out.println("Токены сохранены в папку: " + TOKENS_DIRECTORY);

        } catch (Exception e) {
            System.err.println("Ошибка авторизации:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}