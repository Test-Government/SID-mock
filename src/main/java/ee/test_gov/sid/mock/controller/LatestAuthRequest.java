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
@Controller("/latest-auth-request")
public class LatestAuthRequest {
    private final DataProvider dataProvider;

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> latestAuthRequest() {
        try {
            return HttpResponse.ok(
                    dataProvider.getRequestData()
            );
        } catch (NotFoundException e) {
            log.info("Latest auth request not found", e);
            return HttpResponse.notFound(
                    CollectionUtils.mapOf(
                            "code", 404,
                            "message", "Not Found"
                    ));
        } catch (Exception e) {
            log.error("Unable to retrieve latest auth request", e);
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", e.getMessage()
                    ));
        }
    }

    @Get("/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> usersLatestAuthRequest(@PathVariable String identifier) {
        try {
            return HttpResponse.ok(
                    dataProvider.getRequestData(identifier)
            );
        } catch (NotFoundException e) {
            log.info("Latest auth request not found", e);
            return HttpResponse.notFound(
                    CollectionUtils.mapOf(
                            "code", 404,
                            "message", "Not Found"
                    ));
        } catch (Exception e) {
            log.error("Unable to retrieve latest auth request", e);
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", e.getMessage()
                    ));
        }
    }
}
