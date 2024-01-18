package ee.test_gov.sid.mock

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import setup.RedisDependantSpecification

@MicronautTest
class CertificateChoiceSessionSpec extends RedisDependantSpecification {
    @Inject
    @Client("/")
    HttpClient client

    def "successful certificate choice session"() {
        given:
        def requestParameters = [
                "relyingPartyUUID": "00000000-0000-0000-0000-000000000000",
                "relyingPartyName": "DEMO"
        ]
        when:
        HttpRequest initSessionRequest = HttpRequest.POST(
                "/smart-id-rp/v2/certificatechoice${endpoint}/${identifier}", requestParameters)
        HttpResponse initSessionResponse = client.toBlocking().exchange(initSessionRequest, Map<String, Object>)

        then:
        assert initSessionResponse.status == HttpStatus.OK
        assert initSessionResponse.getContentType().get().toString() == MediaType.APPLICATION_JSON.toString()
        assert initSessionResponse.getBody().isPresent()

        when:
        def sessionId = initSessionResponse.body().get("sessionID")
        HttpRequest sessionRequest = HttpRequest.GET(
                "/smart-id-rp/v2/session/${sessionId}?timeoutMs=2000")
        HttpResponse sessionStatusResponse = client.toBlocking().exchange(sessionRequest, Map<String, Object>)

        then:
        assert sessionStatusResponse.status == HttpStatus.OK
        assert sessionStatusResponse.getContentType().get().toString() == MediaType.APPLICATION_JSON.toString()

        and:
        assert sessionStatusResponse.getBody().isPresent()
        def body = sessionStatusResponse.getBody().get()
        assert body.get("state") == "COMPLETE"

        def result = body.get("result") as Map
        assert result.get("documentNumber") == "PNOEE-30303039914-MOCK-Q"
        assert result.get("endResult") == "OK"

        def cert = body.get("cert") as Map
        assert cert.get("certificateLevel") == "QUALIFIED"
        assert cert.get("value") == "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"

        where:
        endpoint    | identifier
        "/etsi"     | "PNOEE-30303039914"
        "/document" | "PNOEE-30303039914-MOCK-Q"
    }
}