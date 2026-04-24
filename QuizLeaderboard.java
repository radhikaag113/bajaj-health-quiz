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

    // TODO: Change this to your actual registration number
    private static final String REG_NO = "RA2311003030459";
    private static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";

    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();

        // HashSet to store unique combinations of "roundId_participant" to handle duplicates
        Set<String> seenEvents = new HashSet<>();

        // HashMap to store the total score for each participant
        Map<String, Integer> totalScores = new HashMap<>();

        System.out.println("Starting 10 API polls...\n");

        try {
            // Step A: Poll the API 10 times
            for (int i = 0; i < 10; i++) {
                System.out.println("Polling index " + i + "...");

                HttpRequest getRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/quiz/messages?regNo=" + REG_NO + "&poll=" + i))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
                String jsonResponse = response.body();

                // Step B: Extract data and Deduplicate
                // Using Regex to extract values since we aren't using external JSON libraries
                Pattern pattern = Pattern.compile("\\{\"roundId\":\"(.*?)\",\"participant\":\"(.*?)\",\"score\":(\\d+)\\}");
                Matcher matcher = pattern.matcher(jsonResponse);

                while (matcher.find()) {
                    String roundId = matcher.group(1);
                    String participant = matcher.group(2);
                    int score = Integer.parseInt(matcher.group(3));

                    String uniqueKey = roundId + "_" + participant;

                    // Only process if we haven't seen this exact round/participant combo before
                    if (!seenEvents.contains(uniqueKey)) {
                        seenEvents.add(uniqueKey);

                        // Add score to the participant's total
                        totalScores.put(participant, totalScores.getOrDefault(participant, 0) + score);
                    }
                }

                // Mandatory 5-second delay between polls
                if (i < 9) {
                    Thread.sleep(5000);
                }
            }

            // Step C: Generate and sort the leaderboard
            System.out.println("\nPolling complete. Calculating leaderboard...");
            List<Map.Entry<String, Integer>> leaderboardList = new ArrayList<>(totalScores.entrySet());

            // Sort in descending order based on score
            leaderboardList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            // Construct the JSON payload for submission manually
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

            // Step D: Submit the final leaderboard
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
