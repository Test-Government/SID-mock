package ee.test_gov.sid.mock.config.controller;

import ee.test_gov.sid.mock.data.DataProvider;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Controller("/users")
public class Users {

    @Get(produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, DataProvider.UserResponseType>> getUsers() {
        return HttpResponse.ok(
                DataProvider.usersMapping
        );
    }

    @Put(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> addUsers(@Body Map<String, DataProvider.UserResponseType> body) {
        Set<String> duplicates = new HashSet<>(body.keySet());
        duplicates.retainAll(DataProvider.usersMapping.keySet());
        if(!duplicates.isEmpty()) {
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", "An account already exists for identifiers: " + duplicates
                    ));
        }
        try {
            DataProvider.usersMapping.putAll(body);
            return HttpResponse.ok();
        } catch (Exception e) {
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", e.getMessage()
                    ));
        }
    }

    @Post(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> editUsers(@Body Map<String, DataProvider.UserResponseType> body) {
        try {
            DataProvider.usersMapping.putAll(body);
            return HttpResponse.ok();
        } catch (Exception e) {
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", e.getMessage()
                    ));
        }
    }

    @Delete(produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> deleteUsers(@Body List<String> body) {
        try {
            body.forEach(identifier -> DataProvider.usersMapping.remove(identifier));
            return HttpResponse.ok();
        } catch (Exception e) {
            return HttpResponse.serverError(
                    CollectionUtils.mapOf(
                            "error", e.getMessage()
                    ));
        }
    }
}