package ee.test_gov.sid.mock.controller;

import ee.test_gov.sid.mock.data.DataProvider;
import ee.test_gov.sid.mock.data.SessionInitData;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Controller("/smart-id-rp/v2/certificatechoice")
public class CertificateChoiceSession extends Session {

    public CertificateChoiceSession(DataProvider dataProvider) {
        super(dataProvider);
    }

    @Post("/etsi/{identifier}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> certificateChoiceByEtsi(
            @PathVariable String identifier, @Body SessionInitData body) {
        body.sessionType = DataProvider.SessionType.CERTIFICATE_CHOICE;
        return processSession(identifier, body);
    }

    @Post("/document/{documentNumber}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> certificateChoiceByDocumentNumber(
            @PathVariable String documentNumber, @Body SessionInitData body) {
        body.sessionType = DataProvider.SessionType.CERTIFICATE_CHOICE;
        return processSessionByDocumentNumber(documentNumber, body);
    }
}
