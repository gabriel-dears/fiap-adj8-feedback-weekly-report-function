package fiap_adj8.feedback_platform.infra.adapter.in;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import fiap_adj8.feedback_platform.application.port.out.client.FeedbackServiceClientPort;
import fiap_adj8.feedback_platform.application.port.out.email.EmailSender;
import fiap_adj8.feedback_platform.application.port.out.email.input.EmailInput;
import fiap_adj8.feedback_platform.application.port.out.template.TemplateProvider;
import fiap_adj8.feedback_platform.domain.model.PubSubMessage;
import fiap_adj8.feedback_platform.infra.adapter.out.client.FeedbackServiceClientAdapter;
import fiap_adj8.feedback_platform.infra.adapter.out.client.dto.LessonSummary;
import fiap_adj8.feedback_platform.infra.adapter.out.email.JakartaMailSender;
import fiap_adj8.feedback_platform.infra.adapter.out.template.TemplateLoader;
import fiap_adj8.feedback_platform.infra.helper.LessonSummaryTableBuilder;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

public class WeeklyReportFunction implements BackgroundFunction<PubSubMessage> {

    private static final Logger logger = Logger.getLogger(WeeklyReportFunction.class.getName());

    private final EmailSender emailSender = new JakartaMailSender();
    private final FeedbackServiceClientPort feedbackServiceClientPort = new FeedbackServiceClientAdapter();
    private final LessonSummaryTableBuilder lessonSummaryTableBuilder = new LessonSummaryTableBuilder();

    private final String template;

    public WeeklyReportFunction() {
        TemplateProvider templateProvider = new TemplateLoader();
        this.template = templateProvider.getTemplate("weekly-report.html");
    }

    @Override
    public void accept(PubSubMessage message, Context context) {

        try {
            logger.info("📨 Weekly Report Trigger received");

            if (message != null && message.data != null) {
                String decoded = new String(Base64.getDecoder().decode(message.data));
                logger.info("📨 Decoded trigger message: " + decoded);
            }

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(7);

            List<String> adminEmails = feedbackServiceClientPort.getAdminEmails();

            if (adminEmails.isEmpty()) {
                logger.warning("⚠️ No admin emails found.");
                return;
            }

            sendWeeklyReport(adminEmails, startDate, endDate);

        } catch (Exception e) {
            logger.severe("❌ Error in WeeklyReportFunction: " + e.getMessage());
        }
    }

    private void sendWeeklyReport(List<String> adminEmails, LocalDate startDate, LocalDate endDate) {
        String htmlContent = buildHtmlContent(startDate, endDate);

        for (String adminEmail : adminEmails) {
            try {
                sendEmailToAdmin(adminEmail, htmlContent);
                logger.info("✅ Weekly report sent to: " + adminEmail);
            } catch (Exception e) {
                logger.warning("❌ Failed to send weekly report to " + adminEmail + ": " + e.getMessage());
            }
        }
    }

    private void sendEmailToAdmin(String adminEmail, String htmlContent) {
        EmailInput emailInput = new EmailInput(
                adminEmail,
                "Weekly Feedback Report",
                htmlContent
        );
        emailSender.send(emailInput);
    }

    private String buildHtmlContent(LocalDate startDate, LocalDate endDate) {

        List<LessonSummary> highestRatedLessons =
                feedbackServiceClientPort.getHighestRatedLessons(startDate, endDate);

        List<LessonSummary> mostRatedLessons =
                feedbackServiceClientPort.getMostRatedLessons(startDate, endDate);

        String highestRatedRows = lessonSummaryTableBuilder.buildTableRows(highestRatedLessons);
        String mostRatedRows = lessonSummaryTableBuilder.buildTableRows(mostRatedLessons);

        return template
                .replace("{week_start}", startDate.toString())
                .replace("{week_end}", endDate.toString())
                .replace("{most_rated_rows}", mostRatedRows)
                .replace("{highest_rated_rows}", highestRatedRows)
                .replace("{generation_date}", LocalDate.now().toString());
    }
}
