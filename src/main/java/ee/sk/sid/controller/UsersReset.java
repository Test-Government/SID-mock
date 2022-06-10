package ee.sk.sid.controller;

import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static ee.sk.sid.data.DataProvider.resetUsersMapping;

@RequiredArgsConstructor
@Controller("/users/reset")
public class UsersReset {

    @Get(produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> resetUsers() {

        try {
            resetUsersMapping();
        } catch (Exception e) {
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", e.getMessage()
                    ));
        }
        return HttpResponse.ok();
    }
}
