package ee.test_gov.sid.mock.config.controller;

import ee.test_gov.sid.mock.data.DataProvider;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
@Controller("/users/reset")
public class UsersReset {

    @Get(produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> resetUsers() {

        try {
            DataProvider.resetUsersMapping();
        } catch (Exception e) {
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", e.getMessage()
                    ));
        }
        return HttpResponse.ok();
    }
}
