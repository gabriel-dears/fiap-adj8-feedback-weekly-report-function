package fiap_adj8.feedback_platform.infra.adapter.out.client.dto;

public record LessonSummary(
        String lessonName,
        Integer totalFeedbacks,
        Double averageRating
) {
}
