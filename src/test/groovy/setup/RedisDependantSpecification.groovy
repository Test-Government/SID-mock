package setup

import io.micronaut.test.support.TestPropertyProvider
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
abstract class RedisDependantSpecification extends Specification implements TestPropertyProvider {

    @Shared
    GenericContainer redis = new GenericContainer("redis:alpine")
            .withExposedPorts(6379).with {
        it.start()
        it
    }

    @Override
    Map<String, String> getProperties() {
        return [
                'redis.port': redis.getMappedPort(6379) as String,
                'redis.uri' : 'redis://' + redis.getContainerIpAddress() + ":" + redis.getMappedPort(6379)
        ]
    }
}
