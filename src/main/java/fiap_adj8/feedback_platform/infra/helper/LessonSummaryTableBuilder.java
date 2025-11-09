package fiap_adj8.feedback_platform.infra.helper;

import fiap_adj8.feedback_platform.infra.adapter.out.client.dto.LessonSummary;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class LessonSummaryTableBuilder {

    public String buildTableRows(List<LessonSummary> lessons) {
        return lessons.stream()
                .map(l -> String.format(
                        "<tr><td>%s</td><td>%d</td><td>%.2f ⭐</td></tr>",
                        escapeHtml(l.lessonName()),
                        l.totalFeedbacks(),
                        l.averageRating()))
                .collect(Collectors.joining());
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

}
