package fiap_adj8.feedback_platform.infra.adapter.out.client;

import fiap_adj8.feedback_platform.application.port.out.client.FeedbackAppClientPort;
import fiap_adj8.feedback_platform.infra.adapter.out.client.dto.LessonSummary;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDate;
import java.util.List;

@Singleton
public class FeedbackAppClientAdapter implements FeedbackAppClientPort {
    private final FeedbackAppClient feedbackAppClient;

    public FeedbackAppClientAdapter(@RestClient FeedbackAppClient feedbackAppClient) {
        this.feedbackAppClient = feedbackAppClient;
    }

    @Override
    public List<String> getAdminEmails() {
        return feedbackAppClient.getAdminEmails();
    }

    @Override
    public List<LessonSummary> getMostRatedLessons(LocalDate startDate, LocalDate endDate) {
        return feedbackAppClient.getMostRatedLessons(startDate.toString(), endDate.toString());
    }

    @Override
    public List<LessonSummary> getHighestRatedLessons(LocalDate startDate, LocalDate endDate) {
        return feedbackAppClient.getHighestRatedLessons(startDate.toString(), endDate.toString());
    }
}
