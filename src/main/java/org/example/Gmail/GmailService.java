package org.example.Gmail;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GmailService {
    private static final String APPLICATION_NAME = "Test Automation";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);
    private static final String TOKENS_DIRECTORY = "tokens";
    private static final String CREDENTIALS_FILE = "credentialsSG.json";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int WAIT_BETWEEN_ATTEMPTS_MS = 30000; // 30 секунд

    private final Gmail gmailService;

    public GmailService() throws GeneralSecurityException, IOException {
        this.gmailService = initializeGmailService();
    }

    private Gmail initializeGmailService() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // 1. Попробуем использовать Service Account
        try {
            return createServiceAccountGmailService(HTTP_TRANSPORT);
        } catch (Exception e) {
            System.out.println("Service Account не сработал, пробуем OAuth 2.0: " + e.getMessage());
        }

        // 2. Fallback на OAuth 2.0
        return createOAuthGmailService(HTTP_TRANSPORT);
    }

    private Gmail createServiceAccountGmailService(NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Читаем конфигурацию из properties файла
        Properties props = new Properties();
        props.load(new FileInputStream("gmail.properties"));

        GoogleCredential credential = GoogleCredential
                .fromStream(new FileInputStream(CREDENTIALS_FILE))
                .createScoped(SCOPES)
                .createDelegated(props.getProperty("delegated.user.email"));

        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Gmail createOAuthGmailService(NetHttpTransport HTTP_TRANSPORT) throws IOException {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY,
                new InputStreamReader(new FileInputStream(CREDENTIALS_FILE), StandardCharsets.UTF_8)
        );

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(Paths.get(TOKENS_DIRECTORY).toFile()))
                .setAccessType("offline")
                .build();

        Credential credential = flow.loadCredential("user");
        if (credential == null || credential.getRefreshToken() == null) {
            throw new RuntimeException("Необходима первоначальная авторизация. Запустите GmailAuthHelper");
        }

        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public String waitForConfirmationLink(String email, int maxAttempts) throws Exception {
        System.out.println("\n=== Ожидаем доставки письма ===");
        Thread.sleep(10000);

        System.out.println("\n=== Начало поиска письма ===");
        System.out.println("Ищем письмо для: " + email);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                System.out.printf("\nПопытка %d/%d:%n", attempt, maxAttempts);
                String link = getConfirmationLink(email);
                System.out.println("=== Ссылка найдена ===");
                return link;
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
                if (attempt == maxAttempts) throw e;
                Thread.sleep(15000); // 30 секунд между попытками
            }
        }
        throw new IllegalStateException("Невозможно достичь этого места");
    }

    private String getConfirmationLink(String email) throws Exception {
        // 1. Формируем поисковый запрос
        String query = String.format(
                buildSearchQuery(email),
                email,
                System.currentTimeMillis() / 1000 - 3600 // письма за последний час
        );
        System.out.println("Поисковый запрос: " + query);

        // 2. Ищем письма
        ListMessagesResponse messages = gmailService.users().messages().list("me")
                .setQ(query)
                .execute();

        // 3. Диагностика результатов поиска
        if (messages.getMessages() == null || messages.getMessages().isEmpty()) {
            System.out.println("Письма не найдены. Проверяем наличие любых писем...");
            checkInboxContent(); // Дополнительная диагностика
            throw new RuntimeException("Не найдено писем с подтверждением");
        }

        System.out.println("Найдено писем: " + messages.getMessages().size());

        // 4. Получаем первое письмо
        Message message = gmailService.users().messages().get("me", messages.getMessages().get(0).getId())
                .setFormat("full")
                .execute();

        // 5. Анализируем содержимое
        System.out.println("Тема письма: " + message.getPayload().getHeaders().stream()
                .filter(h -> h.getName().equalsIgnoreCase("Subject"))
                .findFirst()
                .map(h -> h.getValue())
                .orElse("(нет темы)"));

        return extractConfirmationLink(message);
    }

    private void checkInboxContent() throws Exception {
        ListMessagesResponse allMessages = gmailService.users().messages().list("me")
                .setMaxResults(5L)
                .execute();

        if (allMessages.getMessages() != null) {
            System.out.println("Последние 5 писем в inbox:");
            for (Message msg : allMessages.getMessages()) {
                Message fullMsg = gmailService.users().messages().get("me", msg.getId())
                        .setFormat("metadata")
                        .setMetadataHeaders(List.of("Subject", "From", "To"))
                        .execute();

                System.out.println("- " + fullMsg.getPayload().getHeaders().stream()
                        .filter(h -> h.getName().equalsIgnoreCase("Subject"))
                        .findFirst()
                        .map(h -> h.getValue())
                        .orElse("(нет темы)"));
            }
        } else {
            System.out.println("Inbox пуст");
        }
    }


    private String buildSearchQuery(String email) {
        // Ищем письма с разными вариантами тем
        return String.format(
                "to:%s (subject:\"Подтверждение регистрации\" OR subject:\"E-mail address verification\" OR subject:\"Email verification\") newer_than:1h",
                email
        );
    }

    private String extractConfirmationLink(Message message) throws Exception {
        String body = getMessageBody(message);
        System.out.println("Анализ тела письма...");

        // 1. Ищем стандартную ссылку подтверждения
        Pattern pattern = Pattern.compile("https?:\\/\\/[^\\s\"]*confirm[^\\s\"]*");
        Matcher matcher = pattern.matcher(body);

        if (matcher.find()) {
            String link = matcher.group();
            System.out.println("Стандартная ссылка найдена: " + maskLink(link));
            return link;
        }

        // 2. Альтернативные варианты поиска
        System.out.println("Стандартная ссылка не найдена. Ищем альтернативные варианты...");

        // Поиск по кнопке
        Pattern btnPattern = Pattern.compile("<a[^>]+href=\"([^\"]*)\"[^>]*>\\s*(?:Подтвердить|Confirm)");
        Matcher btnMatcher = btnPattern.matcher(body);
        if (btnMatcher.find()) {
            String link = btnMatcher.group(1);
            System.out.println("Ссылка из кнопки найдена: " + maskLink(link));
            return link;
        }

        // Логирование для отладки
        System.out.println("=== Начало тела письма ===");
        System.out.println(body.substring(0, Math.min(500, body.length())));
        System.out.println("=== Конец фрагмента ===");

        throw new RuntimeException("Не удалось найти ссылку подтверждения в теле письма");
    }

    private String getMessageBody(Message message) {
        try {
            if (message.getPayload().getParts() != null) {
                for (MessagePart part : message.getPayload().getParts()) {
                    if (part.getMimeType().equals("text/html")) {
                        return new String(
                                Base64.getUrlDecoder().decode(part.getBody().getData()),
                                StandardCharsets.UTF_8
                        );
                    }
                }
            }
            return new String(
                    Base64.getUrlDecoder().decode(message.getPayload().getBody().getData()),
                    StandardCharsets.UTF_8
            );
        } catch (Exception e) {
            throw new RuntimeException("Ошибка декодирования тела письма", e);
        }
    }

    private String maskLink(String link) {
        return link.replaceAll("(https?://[^/]+/).*", "$1*****");
    }
}