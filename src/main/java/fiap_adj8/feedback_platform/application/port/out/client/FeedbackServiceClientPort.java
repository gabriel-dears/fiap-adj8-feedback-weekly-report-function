package fiap_adj8.feedback_platform.application.port.out.client;

import fiap_adj8.feedback_platform.infra.adapter.out.client.dto.LessonSummary;

import java.time.LocalDate;
import java.util.List;

public interface FeedbackServiceClientPort {
    List<String> getAdminEmails();

    List<LessonSummary> getMostRatedLessons(LocalDate startDate, LocalDate endDate);

    List<LessonSummary> getHighestRatedLessons(LocalDate startDate, LocalDate endDate);
}