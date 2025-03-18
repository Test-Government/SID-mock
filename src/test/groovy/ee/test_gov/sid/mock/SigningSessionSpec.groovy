package ee.test_gov.sid.mock

import ee.test_gov.sid.mock.data.DataProvider
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import setup.RedisDependantSpecification

@MicronautTest
class SigningSessionSpec extends RedisDependantSpecification {
    @Inject
    @Client("/")
    HttpClient client

    def "successful signing session"() {
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
                "/smart-id-rp/v2/signature${endpoint}/${identifier}", requestParameters)
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
        assert cert.get("value") == TestUtil.getTestCert(identifier, DataProvider.SessionType.SIGNING)

        def signature = body.get("signature") as Map
        assert signature.get("algorithm") == "sha512WithRSAEncryption"

        where:
        endpoint    | identifier
        "/etsi"     | "PNOEE-30303039914"
        "/document" | "PNOEE-30303039914-MOCK-Q"
    }

    def "given bad char '#badCharName' in #displayTextType, then 400 bad request"() {
        given:
        def requestParameters = """
{
  "relyingPartyUUID" : "00000000-0000-0000-0000-000000000000",
  "relyingPartyName" : "DEMO",
  "hash" : "7rzjHadXTwqsjKrlSqkT5JQ4vejD7VHhsd/zA9173tNFZjChDuh0L8lCHDAVR/ogVe3mRTxEeskmkYQ7cfding==",
  "hashType" : "SHA512",
  "allowedInteractionsOrder" : [ {
    "type" : "verificationCodeChoice",
    ${displayTextType} : "Special char = ${badChar}"
  } ]
}"""

        when:
        HttpRequest initSessionRequest = HttpRequest.POST(
                "/smart-id-rp/v2/signature${endpoint}/${identifier}", requestParameters)
        client.toBlocking().exchange(initSessionRequest, Map<String, Object>)

        then:
        HttpClientResponseException e = thrown()
        e.status == HttpStatus.BAD_REQUEST

        where:
        endpoint    | identifier                 | displayTextType  | badChar  | badCharName
        "/etsi"     | "PNOEE-30303039914"        | "displayText60"  | '\u0000' | "NUL"
        "/etsi"     | "PNOEE-30303039914"        | "displayText60"  | '\n'     | "LINE_FEED"
        "/etsi"     | "PNOEE-30303039914"        | "displayText60"  | '\r'     | "CARRIAGE_RETURN"
        "/etsi"     | "PNOEE-30303039914"        | "displayText200" | '\u0000' | "NUL"
        "/etsi"     | "PNOEE-30303039914"        | "displayText200" | '\n'     | "LINE_FEED"
        "/etsi"     | "PNOEE-30303039914"        | "displayText200" | '\r'     | "CARRIAGE_RETURN"
        "/document" | "PNOEE-30303039914-MOCK-Q" | "displayText60"  | '\u0000' | "NUL"
        "/document" | "PNOEE-30303039914-MOCK-Q" | "displayText60"  | '\n'     | "LINE_FEED"
        "/document" | "PNOEE-30303039914-MOCK-Q" | "displayText60"  | '\r'     | "CARRIAGE_RETURN"
        "/document" | "PNOEE-30303039914-MOCK-Q" | "displayText200" | '\u0000' | "NUL"
        "/document" | "PNOEE-30303039914-MOCK-Q" | "displayText200" | '\n'     | "LINE_FEED"
        "/document" | "PNOEE-30303039914-MOCK-Q" | "displayText200" | '\r'     | "CARRIAGE_RETURN"
    }
}
