package ee.sk.sid.data;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;

import javax.validation.constraints.NotBlank;
import java.time.Duration;

@Context
@ConfigurationProperties("sid-mock")
public record SidMockProperties (
    @NotBlank
    Duration delay, // = Duration.ofSeconds(1);

    @NotBlank
    Duration expiration // = Duration.ofMinutes(5);
//
//    @NotBlank(message = "truststore location must be provided")
//    String trustStorePath
) {
//    public Duration getDelay() {
//        return delay;
//    }
//
//    public void setDelay(Duration delay) {
//        this.delay = delay;
//    }
//
//    public Duration getExpiration() {
//        return expiration;
//    }
//
//    public void setExpiration(Duration expiration) {
//        this.expiration = expiration;
//    }


//    public KeyStore getTrustStorePath() {
//        return trustStorePath;
//    }
//
//    public void setTrustStorePath(KeyStore trustStorePath) {
//        this.trustStorePath = trustStorePath;
//    }
}
