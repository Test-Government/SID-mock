package ee.test_gov.sid.mock

import ee.test_gov.sid.mock.data.DataProvider
import javassist.NotFoundException

class TestUtil {

    static String getTestCert(String identifier, DataProvider.SessionType type) {
        return switch (type) {
            case DataProvider.SessionType.AUTHENTICATION ->
                switch (identifier) {
                    case "PNOEE-30303039914", "PNOEE-30303039914-MOCK-Q" -> "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"
                    default -> throw new NotFoundException("Authentication certificate for identifier '${identifier}' not found")
                }
            case DataProvider.SessionType.SIGNING, DataProvider.SessionType.CERTIFICATE_CHOICE ->
                switch (identifier) {
                    case "PNOEE-30303039914", "PNOEE-30303039914-MOCK-Q" -> "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"
                    default -> throw new NotFoundException("Signing certificate for identifier '${identifier}' not found")
                }
        }
    }
}
