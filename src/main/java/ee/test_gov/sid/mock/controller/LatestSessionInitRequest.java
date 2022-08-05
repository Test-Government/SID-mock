package ee.test_gov.sid.mock.controller;

import ee.test_gov.sid.mock.data.DataProvider;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller("/latest-request")
public class LatestSessionInitRequest {
    private final DataProvider dataProvider;

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> latestRequest() {
        try {
            return HttpResponse.ok(
                    dataProvider.fetchRequestData("LatestRequest")
            );
        } catch (NotFoundException e) {
            log.info("Latest session init request not found", e);
            return HttpResponse.notFound(
                    CollectionUtils.mapOf(
                            "code", 404,
                            "message", "Not Found"
                    ));
        } catch (Exception e) {
            log.error("Unable to retrieve latest session init request", e);
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", e.getMessage()
                    ));
        }
    }

    @Get("/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> usersLatestRequest(@PathVariable String identifier) {
        try {
            return HttpResponse.ok(
                    dataProvider.fetchRequestData(identifier + "_LatestRequest")
            );
        } catch (NotFoundException e) {
            log.info("Latest session init request for '{}' not found", identifier, e);
            return HttpResponse.notFound(
                    CollectionUtils.mapOf(
                            "code", 404,
                            "message", "Not Found"
                    ));
        } catch (Exception e) {
            log.error("Unable to retrieve latest session init request", e);
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", e.getMessage()
                    ));
        }
    }

    @Get("/certificatechoice/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> usersLatestChoiceRequest(@PathVariable String identifier) {
        var sessionType = DataProvider.SessionType.CERTIFICATE_CHOICE;
        return latestRequestBySessionType(identifier, sessionType);
    }

    @Get("/authentication/{identifier}")
    public HttpResponse<Map<String, Object>> usersLatestAuthRequest(@PathVariable String identifier) {
        var sessionType = DataProvider.SessionType.AUTHENTICATION;
        return latestRequestBySessionType(identifier, sessionType);
    }

    @Get("/signature/{identifier}")
    public HttpResponse<Map<String, Object>> usersLatestSignRequest(@PathVariable String identifier) {
        var sessionType = DataProvider.SessionType.SIGNING;
        return latestRequestBySessionType(identifier, sessionType);
    }

    private HttpResponse<Map<String, Object>> latestRequestBySessionType(String identifier, DataProvider.SessionType sessionType) {
        try {
            return HttpResponse.ok(
                    dataProvider.fetchRequestData(identifier + "_Latest" + sessionType.label + "Request")
            );
        } catch (NotFoundException e) {
            log.info("Latest {} init request for '{}' not found", sessionType.name, identifier, e);
            return HttpResponse.notFound(
                    CollectionUtils.mapOf(
                            "code", 404,
                            "message", "Not Found"
                    ));
        } catch (Exception e) {
            log.error("Unable to retrieve latest {} init request", sessionType.name, e);
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", e.getMessage()
                    ));
        }
    }
}
