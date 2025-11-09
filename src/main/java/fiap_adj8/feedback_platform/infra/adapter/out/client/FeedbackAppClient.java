package fiap_adj8.feedback_platform.infra.adapter.out.client;

import fiap_adj8.feedback_platform.infra.adapter.out.client.dto.LessonSummary;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.LocalDate;
import java.util.List;

@Path("/api/report")
@RegisterRestClient(configKey = "feedback-app-api")
public interface FeedbackAppClient {

    @GET
    @Path("/most-rated")
    List<LessonSummary> getMostRatedLessons(
            @QueryParam("startDate") LocalDate startDate,
            @QueryParam("endDate") LocalDate endDate
    );

    @GET
    @Path("/highest-rated")
    List<LessonSummary> getHighestRatedLessons(
            @QueryParam("startDate") LocalDate startDate,
            @QueryParam("endDate") LocalDate endDate
    );

    @GET
    @Path("/admins")
    List<String> getAdminEmails();
}
