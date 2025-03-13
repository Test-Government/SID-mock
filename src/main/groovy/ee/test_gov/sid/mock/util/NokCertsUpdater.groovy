package ee.test_gov.sid.mock.util


import ee.test_gov.sid.mock.data.DataProvider
import groovy.json.JsonSlurper
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import jakarta.inject.Inject

import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.LocalDateTime

class NokCertsUpdater {

    static void main(String[] args) {
        ApplicationContext context = ApplicationContext.run()
        NokCertsUpdater updater = context.getBean(NokCertsUpdater)
        updater.updateNokCerts()
        context.close()
    }

    @Inject
    @Client("/")
    HttpClient client

    void updateNokCerts() {
        InputStream is = this.class.getResourceAsStream("/sid-mock-nok-ts.jks")
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(is, "changeit".toCharArray())

        for (String user : getNokUsers()) {
            def cert = CertificateFactory.getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(getCertificate(user).bytes)) as X509Certificate
            keyStore.setCertificateEntry("${user.toLowerCase()}-mock-q.sign", cert)
        }

        new FileOutputStream("src/main/resources/sid-mock-nok-ts.jks").withCloseable {
            FileOutputStream keyStoreOutputStream -> keyStore.store(keyStoreOutputStream, "changeit".toCharArray())
        }
    }

    private static List<String> getNokUsers() {
        InputStream inputStream = DataProvider.class.classLoader.getResourceAsStream('users.json')
        Map jsonObject = new JsonSlurper().parseText(inputStream.text) as Map
        List<String> nokUsers = jsonObject.findAll { key, value -> key != "OK" }.values().flatten() as List<String>
        return nokUsers
    }

    private String getCertificate(String identifier) {
        def requestParameters = """
{
  "relyingPartyUUID" : "00000000-0000-0000-0000-000000000000",
  "relyingPartyName" : "DEMO"
}"""

        HttpRequest initSessionRequest = HttpRequest.POST(
                "https://sid.demo.sk.ee/smart-id-rp/v2/certificatechoice/etsi/${identifier}", requestParameters)
        HttpResponse initSessionResponse = client.toBlocking().exchange(initSessionRequest, Map<String, Object>)

        assert initSessionResponse.status == HttpStatus.OK
        assert initSessionResponse.getContentType().get().toString() == MediaType.APPLICATION_JSON.toString()
        assert initSessionResponse.getBody().isPresent()

        def sessionId = initSessionResponse.body().get("sessionID")
        HttpRequest sessionRequest = HttpRequest.GET(
                "https://sid.demo.sk.ee/smart-id-rp/v2/session/${sessionId}?timeoutMs=2000")

        def body = completeSession(sessionRequest, LocalDateTime.now().plusSeconds(15))

        def result = body.get("result") as Map
        assert result.get("documentNumber") == "${identifier.toUpperCase()}-MOCK-Q"
        assert result.get("endResult") == "OK"

        def cert = body.get("cert") as Map
        assert cert.get("certificateLevel") == "QUALIFIED"

        def fullCert = """-----BEGIN CERTIFICATE-----
${cert.get("value")}
-----END CERTIFICATE-----"""
        return fullCert
    }

    private Map completeSession(HttpRequest sessionRequest, LocalDateTime timeout) {
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
