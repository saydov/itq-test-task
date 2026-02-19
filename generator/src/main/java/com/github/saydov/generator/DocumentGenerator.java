package com.github.saydov.generator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class  DocumentGenerator {

    private static final int HTTP_OK_CODE = 201;

    public static void main(String[] args) throws Exception {
        var configPath = args.length > 0 ? args[0] : "generator-config.properties";

        var props = new Properties();
        try (var fis = new FileInputStream(configPath)) {
            props.load(fis);
        }

        int count = Integer.parseInt(props.getProperty("count", "10000"));
        var apiUrl = props.getProperty("api.url", "http://localhost:8080/api/documents");
        var author = props.getProperty("author", "Generator");
        var titlePrefix = props.getProperty("title.prefix", "Generated Document");

        System.out.printf("Starting generation: N=%d, API=%s%n", count, apiUrl);
        var totalStart = System.currentTimeMillis();

        int success = 0;
        int failed = 0;

        for (int i = 1; i <= count; i++) {
            long start = System.currentTimeMillis();
            try {
                createDocument(apiUrl, author, titlePrefix + " #" + i);
                success++;
                long elapsed = System.currentTimeMillis() - start;
                System.out.printf("[%d/%d] Created in %dms%n", i, count, elapsed);
            } catch (Exception e) {
                failed++;
                System.err.printf("[%d/%d] FAILED: %s%n", i, count, e.getMessage());
            }
        }

        long totalElapsed = System.currentTimeMillis() - totalStart;
        System.out.printf("Done: success=%d, failed=%d, total time=%dms%n", success, failed, totalElapsed);
    }

    private static void createDocument(String apiUrl, String author, String title) throws IOException {
        var json = String.format("{\"author\":\"%s\",\"title\":\"%s\"}", author, title);

        var conn = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (var os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        if (code != HTTP_OK_CODE) {
            throw new IOException("HTTP " + code);
        }
        conn.disconnect();
    }
}
