package com.worldofwonder.util;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ApiService {

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    private static File cacheDir() {
        File dir = new File("data/cache/avatars");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private static String htmlDecode(String s) {
        if (s == null) return null;
        return s.replace("&quot;", "\"")
                .replace("&#039;", "'")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&shy;", "")
                .replace("&rsquo;", "'")
                .replace("&ldquo;", "\"")
                .replace("&rdquo;", "\"");
    }

    public static boolean isOnline() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://clients3.google.com/generate_204"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 204;
        } catch (Exception e) {
            return false;
        }
    }

    public static int getOpenTDBCategory(int worldId) {
        switch (worldId) {
            case 1: return 23; // Egypt -> History
            case 2: return 17; // Space -> Science & Nature
            case 3: return 27; // Ocean -> Animals
            case 4: return 17; // Dinosaurs -> Science & Nature
            case 5: return 23; // Medieval -> History
            case 6: return 22; // Rainforest -> Geography
            default: return 9; // General Knowledge
        }
    }

    private static <T> void executeAsync(HttpRequest request, AsyncHandler<T> handler, Consumer<T> onSuccess, Consumer<Exception> onError) {
        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() throws Exception {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return handler.handle(response.body());
                } else {
                    throw new RuntimeException("HTTP Error: " + response.statusCode());
                }
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                } catch (Exception e) {
                    if (onError != null) {
                        onError.accept(e instanceof java.util.concurrent.ExecutionException ? (Exception) e.getCause() : e);
                    }
                }
            }
        };
        worker.execute();
    }

    private interface AsyncHandler<T> {
        T handle(String responseBody) throws Exception;
    }

    // A. OpenTDB Trivia API
    @SuppressWarnings("unchecked")
    public static void fetchTrivia(int categoryId, String difficulty, int amount, Consumer<List<Map<String, Object>>> onSuccess, Consumer<Exception> onError) {
        String url = String.format("https://opentdb.com/api.php?amount=%d&category=%d&difficulty=%s&type=multiple",
                amount, categoryId, difficulty);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(6))
                .GET()
                .build();
                
        executeAsync(request, body -> {
            Map<String, Object> map = JsonUtil.parseObject(body);
            if (map.containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) map.get("results");
                for (Map<String, Object> q : results) {
                    if (q.containsKey("question")) q.put("question", htmlDecode((String) q.get("question")));
                    if (q.containsKey("correct_answer")) q.put("correct_answer", htmlDecode((String) q.get("correct_answer")));
                    if (q.containsKey("incorrect_answers")) {
                        List<Object> incores = (List<Object>) q.get("incorrect_answers");
                        List<Object> decoded = new ArrayList<>();
                        for (Object inc : incores) {
                            decoded.add(htmlDecode((String) inc));
                        }
                        q.put("incorrect_answers", decoded);
                    }
                }
                return results;
            }
            throw new Exception("Invalid OpenTDB response");
        }, onSuccess, onError);
    }

    // B. Datamuse Word API
    public static void fetchThemedWords(String theme, int wordLength, int maxResults, Consumer<List<String>> onSuccess, Consumer<Exception> onError) {
        String pattern = wordLength > 0 ? "?".repeat(wordLength) : "*";
        String encodedTheme = URLEncoder.encode(theme, StandardCharsets.UTF_8);
        String url = String.format("https://api.datamuse.com/words?ml=%s&sp=%s&max=%d", encodedTheme, pattern, maxResults);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(6))
                .GET()
                .build();
                
        executeAsync(request, body -> {
            List<String> words = new ArrayList<>();
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"word\"\\s*:\\s*\"([^\"]+)\"").matcher(body);
            while (m.find() && words.size() < maxResults) {
                String word = m.group(1);
                if (!word.contains(" ")) {
                    words.add(word.toUpperCase());
                }
            }
            return words;
        }, onSuccess, onError);
    }

    // C. Fun Facts (Numbers API)
    public static void fetchNumberFact(int number, Consumer<String> onSuccess, Consumer<Exception> onError) {
        String url = String.format("http://numbersapi.com/%d/trivia?json", number);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(6))
                .GET()
                .build();
        executeAsync(request, body -> {
            Map<String, Object> map = JsonUtil.parseObject(body);
            if (map.containsKey("text")) return (String) map.get("text");
            throw new Exception("Invalid Numbers API response");
        }, onSuccess, onError);
    }

    // D. Random Fun Fact (Useless Facts API)
    public static void fetchRandomFact(Consumer<String> onSuccess, Consumer<Exception> onError) {
        String url = "https://uselessfacts.jsph.pl/api/v2/facts/random?language=en";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(6))
                .GET()
                .build();
        executeAsync(request, body -> {
            Map<String, Object> map = JsonUtil.parseObject(body);
            if (map.containsKey("text")) return (String) map.get("text");
            throw new Exception("Invalid Useless Facts response");
        }, onSuccess, onError);
    }

    // E. DiceBear Avatar
    public static void fetchAvatar(String seed, int size, Consumer<BufferedImage> onSuccess, Consumer<Exception> onError) {
        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                File dir = cacheDir();
                File cacheFile = new File(dir, seed + ".png");
                if (cacheFile.exists()) {
                    try {
                        return ImageIO.read(cacheFile);
                    } catch (Exception ignored) { }
                }
                
                String encodedSeed = URLEncoder.encode(seed, StandardCharsets.UTF_8);
                String urlStr = String.format("https://api.dicebear.com/9.x/adventurer/png?seed=%s&size=%d", encodedSeed, size);
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(urlStr))
                        .timeout(Duration.ofSeconds(6))
                        .GET()
                        .build();
                        
                HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() == 200) {
                    Files.write(cacheFile.toPath(), response.body());
                    return ImageIO.read(cacheFile);
                } else {
                    throw new RuntimeException("HTTP Error fetching avatar: " + response.statusCode());
                }
            }

            @Override
            protected void done() {
                try {
                    BufferedImage img = get();
                    if (onSuccess != null) onSuccess.accept(img);
                } catch (Exception e) {
                    if (onError != null) onError.accept(e instanceof java.util.concurrent.ExecutionException ? (Exception) e.getCause() : e);
                }
            }
        };
        worker.execute();
    }

    // F. Wikipedia Summary
    @SuppressWarnings("unchecked")
    public static void fetchWikiSummary(String title, Consumer<Map<String, String>> onSuccess, Consumer<Exception> onError) {
        String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        String url = String.format("https://en.wikipedia.org/api/rest_v1/page/summary/%s", encodedTitle);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "WorldOfWonderApp/1.0")
                .timeout(Duration.ofSeconds(6))
                .GET()
                .build();
        executeAsync(request, body -> {
            Map<String, Object> map = JsonUtil.parseObject(body);
            Map<String, String> res = new HashMap<>();
            res.put("title", (String) map.get("title"));
            res.put("extract", (String) map.get("extract"));
            res.put("description", (String) map.get("description"));
            
            if (map.containsKey("thumbnail")) {
                Map<String, Object> thumb = (Map<String, Object>) map.get("thumbnail");
                res.put("thumbnail", (String) thumb.get("source"));
            } else {
                res.put("thumbnail", null);
            }
            return res;
        }, onSuccess, onError);
    }

    // G. Dreamlo Leaderboard
    public static void submitScore(String privateCode, String username, int score, Runnable onSuccess, Consumer<Exception> onError) {
        String encodedUser = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String url = String.format("http://dreamlo.com/lb/%s/add/%s/%d", privateCode, encodedUser, score);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(6))
                .GET()
                .build();
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new RuntimeException("HTTP Error: " + response.statusCode());
                }
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    if (onSuccess != null) onSuccess.run();
                } catch (Exception e) {
                    if (onError != null) onError.accept(e instanceof java.util.concurrent.ExecutionException ? (Exception) e.getCause() : e);
                }
            }
        };
        worker.execute();
    }

    @SuppressWarnings("unchecked")
    public static void fetchLeaderboard(String publicCode, int limit, Consumer<List<Map<String, Object>>> onSuccess, Consumer<Exception> onError) {
        String url = String.format("http://dreamlo.com/lb/%s/json/%d", publicCode, limit);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(6))
                .GET()
                .build();
        executeAsync(request, body -> {
            Map<String, Object> map = JsonUtil.parseObject(body);
            if (map.containsKey("dreamlo")) {
                Map<String, Object> dl = (Map<String, Object>) map.get("dreamlo");
                if (dl.containsKey("leaderboard")) {
                    Object lbObj = dl.get("leaderboard");
                    if (lbObj instanceof Map) {
                        Map<String, Object> lb = (Map<String, Object>) lbObj;
                        if (lb.containsKey("entry")) {
                            Object entry = lb.get("entry");
                            if (entry instanceof List) {
                                return (List<Map<String, Object>>) entry;
                            } else if (entry instanceof Map) {
                                List<Map<String, Object>> list = new ArrayList<>();
                                list.add((Map<String, Object>) entry);
                                return list;
                            }
                        }
                    }
                }
            }
            return new ArrayList<>();
        }, onSuccess, onError);
    }
}
