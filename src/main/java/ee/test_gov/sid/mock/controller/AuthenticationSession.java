package ee.test_gov.sid.mock.controller;

import ee.test_gov.sid.mock.data.AuthenticationInitData;
import ee.test_gov.sid.mock.data.DataProvider;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Controller("/smart-id-rp/v2/authentication/etsi/{identifier}")
public class AuthenticationSession {

    private final DataProvider dataProvider;

    @Post(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> authenticationSession(@PathVariable String identifier, @Body AuthenticationInitData body) {
        UUID sessionId = UUID.randomUUID();
        log.info("Received request to authenticate '{}' with session id '{}'", identifier, sessionId);
        try {
            dataProvider.putResponseData(sessionId, identifier, body);
            log.info("Response stored for '{}' authentication with session id '{}'", identifier, sessionId);
            return HttpResponse.ok(
                    CollectionUtils.mapOf(
                            "sessionID", sessionId
                    )
            );
        } catch (NotFoundException e) {
            log.info("User not found for identifier '{}'", identifier);
            return HttpResponse.notFound(
                    CollectionUtils.mapOf(
                            "code", 404,
                            "message", "Not Found"
                    ));
        } catch (Exception e) {
            log.error("Unable to create authentication response", e);
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", e.getMessage()
                    ));
        }
    }
}