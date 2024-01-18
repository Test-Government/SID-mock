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
class AuthenticationSessionSpec extends RedisDependantSpecification {
    @Inject
    @Client("/")
    HttpClient client

    def "successful authentication session"() {
        given:
        def requestParameters = """
{
  "relyingPartyUUID" : "00000000-0000-0000-0000-000000000000",
  "relyingPartyName" : "DEMO",
  "hash" : "7rzjHadXTwqsjKrlSqkT5JQ4vejD7VHhsd/zA9173tNFZjChDuh0L8lCHDAVR/ogVe3mRTxEeskmkYQ7cfding==",
  "hashType" : "SHA512",
  "allowedInteractionsOrder" : [ {
    "type" : "verificationCodeChoice",
    "displayText60" : null
  } ]
}"""

        when:
        HttpRequest initSessionRequest = HttpRequest.POST(
                "/smart-id-rp/v2/authentication${endpoint}/${identifier}", requestParameters)
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
        assert body.get("interactionFlowUsed") == "verificationCodeChoice"

        def result = body.get("result") as Map
        assert result.get("documentNumber") == "PNOEE-30303039914-MOCK-Q"
        assert result.get("endResult") == "OK"

        def cert = body.get("cert") as Map
        assert cert.get("certificateLevel") == "QUALIFIED"
        assert cert.get("value") == "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"

        def signature = body.get("signature") as Map
        assert signature.get("algorithm") == "sha512WithRSAEncryption"

        where:
        endpoint    | identifier
        "/etsi"     | "PNOEE-30303039914"
        "/document" | "PNOEE-30303039914-MOCK-Q"
    }
}