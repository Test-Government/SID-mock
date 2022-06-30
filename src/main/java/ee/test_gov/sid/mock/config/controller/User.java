package ee.test_gov.sid.mock.config.controller;

import ee.test_gov.sid.mock.data.UserInfo;
import ee.test_gov.sid.mock.data.DataProvider;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
@Controller("/users/{identifier}")
public class User {

    @Get(produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> getUser(@PathVariable String identifier) {

        if (DataProvider.usersMapping.containsKey(identifier)) {
            return HttpResponse.ok(
                    CollectionUtils.mapOf(
                            "Identifier", identifier,
                            "ResponseType", DataProvider.usersMapping.get(identifier)
                    ));
        }
        return HttpResponse.notFound(
                CollectionUtils.mapOf(
                        "error", "Account not found for identifier: " + identifier
                ));
    }

    @Put(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> addUser(@PathVariable String identifier, @Body UserInfo body) {

        if (DataProvider.usersMapping.containsKey(identifier)) {
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", "An account already exists for identifier: " + identifier
                    ));
        }
        try {
            DataProvider.usersMapping.put(identifier, body.responseType);
            return HttpResponse.ok();
        } catch (Exception e) {
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", e.getMessage()
                    ));
        }
    }

    @Post(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> editUser(@PathVariable String identifier, @Body UserInfo body) {

        if (!DataProvider.usersMapping.containsKey(identifier)) {
            return HttpResponse.notFound(
                    CollectionUtils.mapOf(
                            "error", "No existing account to edit for identifier: " + identifier
                    ));
        }
        try {
            DataProvider.usersMapping.put(identifier, body.responseType);
            return HttpResponse.ok();
        } catch (Exception e) {
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", e.getMessage()
                    ));
        }
    }

    @Delete(produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> deleteUser(@PathVariable String identifier) {
        if (!DataProvider.usersMapping.containsKey(identifier)) {
            return HttpResponse.notFound(
                    CollectionUtils.mapOf(
                            "error", "No existing account to delete for identifier: " + identifier
                    ));
        }
        try {
            DataProvider.usersMapping.remove(identifier);
            return HttpResponse.ok();
        } catch (Exception e) {
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", e.getMessage()
                    ));
        }
    }
}