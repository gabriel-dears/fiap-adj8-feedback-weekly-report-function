package fiap_adj8.feedback_platform.infra.adapter.in.http;

import fiap_adj8.feedback_platform.infra.adapter.in.function.WeeklyReportFunction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/test/weekly-report")
@ApplicationScoped
public class WeeklyReportHttpEndpoint {

    private final WeeklyReportFunction weeklyReportFunction;

    public WeeklyReportHttpEndpoint(WeeklyReportFunction weeklyReportFunction) {
        this.weeklyReportFunction = weeklyReportFunction;
    }

    @POST
    public Response triggerManually() {
        WeeklyReportFunction.PubSubMessage mockMessage = new WeeklyReportFunction.PubSubMessage();
        weeklyReportFunction.accept(mockMessage, null);
        return Response.ok("Weekly report executed successfully").build();
    }
}
