package ee.test_gov.sid.mock

import ee.test_gov.sid.mock.data.DataProvider
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.bouncycastle.asn1.*
import org.bouncycastle.asn1.x500.RDN
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import setup.RedisDependantSpecification
import spock.lang.Ignore

import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Ignore("Local usage only.")
@MicronautTest
class DemoMockComparisonSpec extends RedisDependantSpecification {
    @Inject
    @Client("/")
    HttpClient client

    def "Dummy test"() {
        given:
        ArrayList mockFields = getCertificateFields("30303039914")
    }

    def "When mock returns a certificate, then it matches with the certificate from SK demo"() {
        when:
        ArrayList demoAuthResponse = getCertificateFields(identifier, DataProvider.SessionType.AUTHENTICATION, true)
        ArrayList mockAuthResponse = getCertificateFields(identifier, DataProvider.SessionType.AUTHENTICATION, false)

        then:
        assert mockAuthResponse.sort() == demoAuthResponse.sort()

        when:
        ArrayList demoSignResponse = getCertificateFields(identifier, DataProvider.SessionType.SIGNING, true)
        ArrayList mockSignResponse = getCertificateFields(identifier, DataProvider.SessionType.SIGNING, false)

        then:
        assert mockSignResponse.sort() == demoSignResponse.sort()

        where:
        identifier           | _
        "PNOEE-50001029996"  | _
        "PNOLT-50001029996"  | _
        "PNOLV-020100-29990" | _
        "PNOEE-30303039914"  | _
        "PNOLT-30303039914"  | _
        "PNOLV-030303-10012" | _
        "PNOEE-30303039816"  | _
        "PNOLT-30303039816"  | _
        "PNOLV-030303-10215" | _
        "PNOEE-50701019992"  | _
        "PNOLT-50701019992"  | _
        "PNOLV-010107-20007" | _
//        Exists in demo (as of 18.01.2024) but missing from mock
//        "PNOLV-329999-88807" | _
        "PNOEE-30303039903"  | _
        "PNOLT-30303039903"  | _
        "PNOLV-030303-10004" | _
        "PNOEE-40404049996"  | _
        "PNOLT-40404049996"  | _
        "PNOLV-040404-19999" | _
        "PNOEE-40404049985"  | _
        "PNOLT-40404049985"  | _
        "PNOLV-040404-19980" | _
        "PNOLV-311299-19993" | _
        "PNOLV-329999-99901" | _
        "PNOLV-329999-99709" | _
//        Custom additions to mock (Barbados and Iceland), have never existed in demo.
//        "PNOBB-0303039903" | _
//        "PNOIS-30303039903" | _
//        Legacy entries, that once existed in demo, but have been removed - kept in mock for backwards compatibility.
//        "PNOEE-39912319997" | _
//        "PNOLT-39912319997" | _
//        "PNOLV-010404-29990" | _
//        "PNOLV-329999-99805" | _
    }

    ArrayList getCertificateFields(
            String identifier,
            DataProvider.SessionType sessionType = DataProvider.SessionType.AUTHENTICATION,
            Boolean isDemo = false) {
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

        String sessionTypePath = switch (sessionType) {
            case DataProvider.SessionType.AUTHENTICATION -> "authentication"
            case DataProvider.SessionType.SIGNING -> "signature"
            case DataProvider.SessionType.CERTIFICATE_CHOICE -> throw new IllegalArgumentException()
        }

        HttpRequest initSessionRequest = HttpRequest.POST(
                "${isDemo ? "https://sid.demo.sk.ee" : ""}/smart-id-rp/v2/${sessionTypePath}/etsi/${identifier}", requestParameters)
        HttpResponse initSessionResponse = client.toBlocking().exchange(initSessionRequest, Map<String, Object>)

        assert initSessionResponse.status == HttpStatus.OK
        assert initSessionResponse.getContentType().get().toString() == MediaType.APPLICATION_JSON.toString()
        assert initSessionResponse.getBody().isPresent()

        def sessionId = initSessionResponse.body().get("sessionID")
        HttpRequest sessionRequest = HttpRequest.GET(
                "${isDemo ? "https://sid.demo.sk.ee" : ""}/smart-id-rp/v2/session/${sessionId}?timeoutMs=2000")

        def body = completeSession(sessionRequest, LocalDateTime.now().plusSeconds(15))
        assert body.get("interactionFlowUsed") == "verificationCodeChoice"

        def result = body.get("result") as Map
        assert result.get("documentNumber") == "${identifier.toUpperCase()}-MOCK-Q"
        assert result.get("endResult") == "OK"

        def cert = body.get("cert") as Map
        assert cert.get("certificateLevel") == "QUALIFIED"

        def fullCert = """-----BEGIN CERTIFICATE-----
${cert.get("value")}
-----END CERTIFICATE-----"""
        CertificateFactory cf = CertificateFactory.getInstance("X.509")
        def certificate = cf.generateCertificate(new ByteArrayInputStream(fullCert.getBytes())) as X509Certificate

        ArrayList certData = []

        // Get subject fields
        X500Name x500name = new JcaX509CertificateHolder(certificate).getSubject()
        x500name.getRDNs().each { RDN rdn ->
            ASN1ObjectIdentifier asn1ObjectIdentifier = rdn.getFirst().getType()
            String oidString = BCStyle.INSTANCE.oidToDisplayName(asn1ObjectIdentifier)
            certData.add([(oidString): rdn.getFirst().getValue().toString()])
        }

        // Get birthdate field
        byte[] extVal = certificate.getExtensionValue("2.5.29.9")
        if (extVal != null) {
            ASN1InputStream ais1 = new ASN1InputStream(new ByteArrayInputStream(extVal))
            ASN1OctetString octs = (ASN1OctetString) ais1.readObject()
            ASN1InputStream ais2 = new ASN1InputStream(new ByteArrayInputStream(octs.getOctets()))
            ASN1Sequence seq = (ASN1Sequence) ais2.readObject()
            ASN1Set set = (ASN1Set) seq.getObjectAt(0).getObjectAt(1)
            ASN1GeneralizedTime time = (ASN1GeneralizedTime) set.getObjectAt(0)
            // Convert birthdate into more readable datetime format
            ZonedDateTime birthdate = ZonedDateTime.parse(time.getTime(), DateTimeFormatter.ofPattern("yyyyMMddHHmmssO"))
            certData.add(["BIRTHDATE": birthdate])
        }
        println("${isDemo ? "DEMO" : "MOCK"}: ${certData}")
        return certData
    }

    Map completeSession(HttpRequest sessionRequest, LocalDateTime timeout) {
        HttpResponse sessionStatusResponse = client.toBlocking().exchange(sessionRequest, Map<String, Object>)
        assert sessionStatusResponse.status == HttpStatus.OK
        assert sessionStatusResponse.getContentType().get().toString() == MediaType.APPLICATION_JSON.toString()
        assert sessionStatusResponse.getBody().isPresent()
        Map body = sessionStatusResponse.getBody().get()

        if (body.get("state") == "RUNNING") {
            if (LocalDateTime.now() < timeout) {
                sleep(1000)
                return completeSession(sessionRequest, timeout)
            } else {
                throw new Exception("Session in state 'RUNNING' for too long, aborting.")
            }
        }
        assert body.get("state") == "COMPLETE"
        return body
    }
}