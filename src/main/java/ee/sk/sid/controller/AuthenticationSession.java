package ee.sk.sid.controller;

import ee.sk.sid.data.AuthenticationInitData;
import ee.sk.sid.data.DataProvider;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Controller("/smart-id-rp/v2/authentication/etsi/{identifier}")
public class AuthenticationSession {

    private final DataProvider dataProvider;

    @Post(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> authenticationSession(@PathVariable String identifier, @Body AuthenticationInitData body) {
        UUID sessionId = UUID.randomUUID();
        try {
            dataProvider.putResponseData(sessionId, identifier, body);
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
        return HttpResponse.ok(
                CollectionUtils.mapOf(
                        "sessionID", sessionId
                )
        );
    }
}