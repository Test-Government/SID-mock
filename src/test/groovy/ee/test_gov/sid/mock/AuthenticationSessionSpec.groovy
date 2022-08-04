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