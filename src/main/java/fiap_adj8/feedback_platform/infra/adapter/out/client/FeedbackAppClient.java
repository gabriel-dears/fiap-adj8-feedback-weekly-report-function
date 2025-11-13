package fiap_adj8.feedback_platform.infra.adapter.out.client;

import fiap_adj8.feedback_platform.infra.adapter.out.client.dto.LessonSummary;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

/**
 * REST client interface for communicating with the Feedback Application API.
 * Provides access to lesson rating summaries and admin email listings.
 */
@RegisterRestClient(configKey = "feedback-app-api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface FeedbackAppClient {

    /**
     * Retrieves the most rated lessons within the specified date range.
     *
     * @param startDate Start of the date range (inclusive)
     * @param endDate   End of the date range (inclusive)
     * @return List of lessons sorted by total feedback count (descending)
     */
    @GET
    @Path("/feedback/most-rated")
    List<LessonSummary> getMostRatedLessons(
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate
    );

    /**
     * Retrieves the highest-rated lessons within the specified date range.
     *
     * @param startDate Start of the date range (inclusive)
     * @param endDate   End of the date range (inclusive)
     * @return List of lessons sorted by average rating (descending)
     */
    @GET
    @Path("/feedback/highest-ranked")
    List<LessonSummary> getHighestRatedLessons(
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate
    );

    /**
     * Retrieves the list of administrator email addresses.
     *
     * @return List of admin email addresses
     */
    @GET
    @Path("/user/admin/email")
    List<String> getAdminEmails();
}
