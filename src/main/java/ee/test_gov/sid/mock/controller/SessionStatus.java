package ee.test_gov.sid.mock.controller;

import ee.test_gov.sid.mock.data.DataProvider;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller("/smart-id-rp/v2/session/{sessionId}")
public class SessionStatus {
    private final DataProvider dataProvider;

    @Get(produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> sessionStatus(@PathVariable String sessionId, @QueryValue(defaultValue = "60500") long timeoutMs) {
        log.info("Received status check request for session id '{}'", sessionId);
        try {
            Map<String, Object> responseData = dataProvider.getResponseData(sessionId, timeoutMs);
            log.info("Session status found for session id '{}'", sessionId);
            log.debug(responseData.toString());
            return HttpResponse.ok(
                    responseData
            );
        } catch (NotFoundException e) {
            log.info("Session not found for session id '{}'", sessionId);
            return HttpResponse.notFound(
                    Map.of(
                            "code", 404,
                            "message", "Not Found"
                    ));
        } catch (Exception e) {
            log.error("Unable to create authentication response", e);
            return HttpResponse.serverError(
                    Map.of(
                            "error", e.getMessage()
                    ));
        }
    }
}
