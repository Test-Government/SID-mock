package ee.sk.sid.controller;

import ee.sk.sid.data.DataProvider;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
@Controller("/smart-id-rp/v2/session/{sessionId}")
public class SessionStatus {
    private final DataProvider dataProvider;

    @Get(produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> sessionStatus(@PathVariable String sessionId, @QueryValue long timeoutMs) {
        try {
            return HttpResponse.ok(
                    dataProvider.getResponseData(sessionId, timeoutMs)
            );
        } catch (NotFoundException e) {
            return HttpResponse.notFound(
                    CollectionUtils.mapOf(
                            "code", 404,
                            "message", "Not Found"
                    ));
        } catch (Exception e) {
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", e.getMessage()
                    ));
        }
    }
}
