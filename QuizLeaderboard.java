/**
 * SRM Quiz Task - Backend Integration Assignment
 * Submitted for: Bajaj Finserv Health JAVA Qualifier
 * * Note: Designed to run with zero external dependencies using core Java 11+.
 */
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuizLeaderboard {

  
    private static final String REG_NO = "RA2311003030459";
    private static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";

    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();

       
        Set<String> seenEvents = new HashSet<>();

       
        Map<String, Integer> totalScores = new HashMap<>();

        System.out.println("Starting 10 API polls...\n");

        try {
            
            for (int i = 0; i < 10; i++) {
                System.out.println("Polling index " + i + "...");

                HttpRequest getRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/quiz/messages?regNo=" + REG_NO + "&poll=" + i))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
                String jsonResponse = response.body();

               
                Pattern pattern = Pattern.compile("\\{\"roundId\":\"(.*?)\",\"participant\":\"(.*?)\",\"score\":(\\d+)\\}");
                Matcher matcher = pattern.matcher(jsonResponse);

                while (matcher.find()) {
                    String roundId = matcher.group(1);
                    String participant = matcher.group(2);
                    int score = Integer.parseInt(matcher.group(3));

                    String uniqueKey = roundId + "_" + participant;

                   
                    if (!seenEvents.contains(uniqueKey)) {
                        seenEvents.add(uniqueKey);

                        
                        totalScores.put(participant, totalScores.getOrDefault(participant, 0) + score);
                    }
                }

               
                if (i < 9) {
                    Thread.sleep(5000);
                }
            }

           
            System.out.println("\nPolling complete. Calculating leaderboard...");
            List<Map.Entry<String, Integer>> leaderboardList = new ArrayList<>(totalScores.entrySet());

            
            leaderboardList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

           
            StringBuilder jsonPayload = new StringBuilder();
            jsonPayload.append("{\n");
            jsonPayload.append("  \"regNo\": \"").append(REG_NO).append("\",\n");
            jsonPayload.append("  \"leaderboard\": [\n");

            for (int i = 0; i < leaderboardList.size(); i++) {
                Map.Entry<String, Integer> entry = leaderboardList.get(i);
                jsonPayload.append("    { \"participant\": \"").append(entry.getKey())
                        .append("\", \"totalScore\": ").append(entry.getValue()).append(" }");
                if (i < leaderboardList.size() - 1) {
                    jsonPayload.append(",");
                }
                jsonPayload.append("\n");
            }
            jsonPayload.append("  ]\n}");

            System.out.println("\nPayload to submit:\n" + jsonPayload.toString());

           
            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/quiz/submit"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload.toString()))
                    .build();

            HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("\nServer Response:");
            System.out.println(postResponse.body());

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
