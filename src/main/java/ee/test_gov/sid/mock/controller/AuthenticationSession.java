package ee.test_gov.sid.mock.controller;

import ee.test_gov.sid.mock.data.DataProvider;
import ee.test_gov.sid.mock.data.SessionInitData;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Controller("/smart-id-rp/v2/authentication")
public class AuthenticationSession extends Session {

    public AuthenticationSession(DataProvider dataProvider) {
        super(dataProvider);
    }

    @Post("/etsi/{identifier}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> authenticationByEtsi(
            @PathVariable String identifier, @Body SessionInitData body) {
        body.setSessionType(DataProvider.SessionType.AUTHENTICATION);
        return processSession(identifier, body);
    }

    @Post("/document/{documentNumber}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> authenticationByDocumentNumber(
            @PathVariable String documentNumber, @Body SessionInitData body) {
        body.setSessionType(DataProvider.SessionType.AUTHENTICATION);
        return processSessionByDocumentNumber(documentNumber, body);
    }
}
