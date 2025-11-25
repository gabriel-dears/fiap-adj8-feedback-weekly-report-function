package fiap_adj8.feedback_platform.infra.adapter.out.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fiap_adj8.feedback_platform.application.port.out.client.FeedbackServiceClientPort;
import fiap_adj8.feedback_platform.infra.adapter.out.client.dto.LessonSummary;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

public class FeedbackServiceClientAdapter implements FeedbackServiceClientPort {

    private static final String AUTH_HEADER = "Basic YWRtaW5AZW1haWwuY29tOmFkbWlu";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public List<String> getAdminEmails() {
        return getList(
                "/user/admin/email",
                new TypeToken<List<String>>() {}.getType(),
                "Admin Service"
        );
    }

    @Override
    public List<LessonSummary> getMostRatedLessons(LocalDate startDate, LocalDate endDate) {
        return getList(
                String.format("/feedback/most-rated?startDate=%s&endDate=%s", startDate, endDate),
                new TypeToken<List<LessonSummary>>() {}.getType(),
                "most-rated feedback"
        );
    }

    @Override
    public List<LessonSummary> getHighestRatedLessons(LocalDate startDate, LocalDate endDate) {
        return getList(
                String.format("/feedback/highest-ranked?startDate=%s&endDate=%s", startDate, endDate),
                new TypeToken<List<LessonSummary>>() {}.getType(),
                "highest-ranked feedback"
        );
    }

    private <T> T getList(String path, Type type, String context) {
        try {
            String baseUrl = "https://fiap-feedback-app-dot-fiap-adj8-feedback-platform.uc.r.appspot.com";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(baseUrl + path))
                    .header("Accept", "application/json")
                    .header("Authorization", AUTH_HEADER)
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Failed to fetch " + context +
                        ". Status: " + response.statusCode() +
                        " Body: " + response.body()
                );
            }

            return gson.fromJson(response.body(), type);

        } catch (Exception e) {
            throw new RuntimeException("Error calling " + context, e);
        }
    }
}
