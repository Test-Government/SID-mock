package ee.test_gov.sid.mock.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;

import javax.validation.constraints.NotBlank;
import java.time.Duration;

@Context
@ConfigurationProperties("sid-mock")
public record SidMockProperties(
        @NotBlank
        Duration delay,

        @NotBlank
        Duration expiration,

        @NotBlank
        Boolean storeSessionInitRequests,

        @NotBlank
        Boolean overrideDocumentNumber
//
//    @NotBlank(message = "truststore location must be provided")
//    String trustStorePath
) {
}
