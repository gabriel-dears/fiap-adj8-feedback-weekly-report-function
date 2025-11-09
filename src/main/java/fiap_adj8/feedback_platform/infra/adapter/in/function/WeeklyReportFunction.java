package fiap_adj8.feedback_platform.infra.adapter.in.function;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import fiap_adj8.feedback_platform.application.port.out.client.FeedbackAppClientPort;
import fiap_adj8.feedback_platform.application.port.out.email.EmailSender;
import fiap_adj8.feedback_platform.application.port.out.email.input.EmailInput;
import fiap_adj8.feedback_platform.infra.adapter.out.client.dto.LessonSummary;
import fiap_adj8.feedback_platform.infra.helper.LessonSummaryTableBuilder;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class WeeklyReportFunction implements BackgroundFunction<WeeklyReportFunction.PubSubMessage> {
    private static final Logger logger = Logger.getLogger(WeeklyReportFunction.class.getName());

    @Inject
    EmailSender sender;

    @Inject
    FeedbackAppClientPort feedbackAppClientPort;

    @Inject
    @ConfigProperty(name = "email.template.path", defaultValue = "templates/weekly-report.html")
    String templatePath;

    @Inject
    LessonSummaryTableBuilder lessonSummaryTableBuilder;

    private String htmlTemplate;

    void onStart(@Observes StartupEvent event) {
        try {
            Path path = Path.of("src/main/resources", templatePath);
            htmlTemplate = Files.readString(path);
            logger.info("✅ Email template loaded from: " + path.toAbsolutePath());
        } catch (IOException e) {
            logger.severe("❌ Failed to load email template: " + e.getMessage());
            htmlTemplate = "<p>Template not found</p>";
        }
    }

    public static class PubSubMessage {
        public String data;
    }

    @Override
    public void accept(PubSubMessage message, Context context) {
        // TODO: change rating value from enum to int - 1 to 5

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7);

        List<String> adminEmails = feedbackAppClientPort.getAdminEmails();
        if (adminEmails.isEmpty()) {
            logger.info("⚠️ No admin emails found.");
            return;
        }

        sendWeeklyReport(adminEmails, startDate, endDate);
    }

    private void sendWeeklyReport(List<String> adminEmails, LocalDate startDate, LocalDate endDate) {
        String htmlContent = getHtmlContent(startDate, endDate);
        sendEmailToAllAdmins(adminEmails, htmlContent);
    }

    private void sendEmailToAllAdmins(List<String> adminEmails, String htmlContent) {
        for (String adminEmail : adminEmails) {
            try {
                sendEmailToAdmin(htmlContent, adminEmail);
            } catch (Exception e) {
                logger.warning("❌ Failed to send email to " + adminEmail + ": " + e.getMessage());
            }
        }
    }

    private void sendEmailToAdmin(String htmlContent, String adminEmail) {
        EmailInput emailInput = new EmailInput(
                adminEmail,
                "Weekly Report",
                htmlContent
        );
        sender.send(emailInput);
        logger.info("✅ Email sent to: " + adminEmail);
    }

    private String getHtmlContent(LocalDate startDate, LocalDate endDate) {
        List<LessonSummary> highestRatedLessons = feedbackAppClientPort.getHighestRatedLessons(startDate, endDate);
        List<LessonSummary> mostRatedLessons = feedbackAppClientPort.getMostRatedLessons(startDate, endDate);
        String highestRatedRows = lessonSummaryTableBuilder.buildTableRows(highestRatedLessons);
        String mostRatedRows = lessonSummaryTableBuilder.buildTableRows(mostRatedLessons);

        return htmlTemplate
                .replace("{week_start}", startDate.toString())
                .replace("{week_end}", endDate.toString())
                .replace("{most_rated_rows}", mostRatedRows)
                .replace("{highest_rated_rows}", highestRatedRows)
                .replace("{generation_date}", LocalDate.now().toString());
    }

}
