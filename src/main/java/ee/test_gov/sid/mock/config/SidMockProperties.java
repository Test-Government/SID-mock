package ee.test_gov.sid.mock.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;

import java.time.Duration;

@Context
@ConfigurationProperties("sid-mock")
public record SidMockProperties(
        Duration delay,

        Duration expiration,

        Boolean storeSessionInitRequests,

        Boolean overrideDocumentNumber
) {
}
