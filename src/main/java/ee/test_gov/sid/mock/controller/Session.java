package ee.test_gov.sid.mock.controller;

import ee.test_gov.sid.mock.data.AllowedInteractionsOrder;
import ee.test_gov.sid.mock.data.DataProvider;
import ee.test_gov.sid.mock.data.SessionInitData;
import io.micronaut.http.HttpResponse;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
public class Session {
    private final DataProvider dataProvider;

    HttpResponse<Map<String, Object>> notFound() {
        return HttpResponse.notFound(
                Map.of(
                        "code", 404,
                        "message", "Not Found"
                ));
    }

    HttpResponse<Map<String, Object>> processSession(String identifier, SessionInitData inputData) {

        UUID sessionId = UUID.randomUUID();
        log.info("Received {} request for '{}' with session id '{}'", inputData.getSessionType().name, identifier, sessionId);
        log.debug(inputData.toString());

        try {
            dataProvider.putRequestData(identifier, inputData);
        } catch (Exception e) {
            log.error("Failed to store request", e);
        }
        try {
            dataProvider.putResponseData(sessionId, identifier, inputData);
            log.info("Response stored for '{}' authentication with session id '{}'", identifier, sessionId);
            return HttpResponse.ok(
                    Map.of(
                            "sessionID", sessionId
                    )
            );
        } catch (NotFoundException e) {
            log.info("User not found for identifier '{}'", identifier);
            return notFound();

        } catch (Exception e) {
            log.error("Unable to create authentication response", e);
            return HttpResponse.serverError(
                    Map.of(
                            "error", e.getMessage()
                    ));
        }
    }

    HttpResponse<Map<String, Object>> processSessionByDocumentNumber(String documentNumber, SessionInitData inputData) {

        Pattern pattern = Pattern.compile("^(?<identifier>PNO[a-zA-Z]{2}-.*)-(?<documentNrSuffix>[a-zA-Z\\d]{4}-[a-zA-Z]{1,2})$");
        Matcher matcher = pattern.matcher(documentNumber);

        if (!matcher.find()) {
            log.info("Invalid document number '{}'", documentNumber);
            return notFound();
        }

        // Document number override.
        //  If override not permitted document number suffix must match "MOCK-Q" (currently used in mock certificates).
        //  NOTE! This will only change the value in response body, not inside the certificate.
        if (dataProvider.sidMockProperties.overrideDocumentNumber()) {
            log.info("Override document number. Using '{}'", documentNumber);
            inputData.setDocumentNumber(documentNumber);
        } else {
            String documentNrSuffix = matcher.group("documentNrSuffix");
            if (!Objects.equals(documentNrSuffix, "MOCK-Q")) {
                log.info("Invalid document number suffix '{}', expected '{}'", documentNrSuffix, "MOCK-Q");
                return notFound();
            }
        }

        String identifier = matcher.group("identifier");
        return processSession(identifier, inputData);
    }
}
