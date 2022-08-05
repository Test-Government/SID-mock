package ee.test_gov.sid.mock.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import ee.sk.smartid.AuthenticationResponseValidator;
import ee.test_gov.sid.mock.config.SidMockProperties;
import io.lettuce.core.api.StatefulRedisConnection;
import jakarta.inject.Singleton;
import javassist.NotFoundException;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import static io.lettuce.core.SetArgs.Builder.ex;


@Singleton
@AllArgsConstructor
public class DataProvider {
    private final StatefulRedisConnection<String, String> redisConnection;

    public final SidMockProperties sidMockProperties;

    public static PrivateKey privateKey;

    static {
        try {
            privateKey = getSidMockKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> certificates;

    static {
        try {
            certificates = getCertificates();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, UserResponseType> usersMapping;

    static {
        try {
            usersMapping = getUsersMapping();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public enum SessionType {
        CERTIFICATE_CHOICE("Choice", "certificate choice session"),
        AUTHENTICATION("Auth", "authentication session"),
        SIGNING("Sign", "signing session");

        public final String name;
        public final String label;

        SessionType(String label, String name) {
            this.name = name;
            this.label = label;
        }
    }

    public enum UserResponseType {
        OK,
        USER_REFUSED,
        USER_REFUSED_DISPLAYTEXTANDPIN,
        USER_REFUSED_VC_CHOICE,
        USER_REFUSED_CONFIRMATIONMESSAGE,
        USER_REFUSED_CONFIRMATIONMESSAGE_WITH_VC_CHOICE,
        USER_REFUSED_CERT_CHOICE,
        WRONG_VC,
        TIMEOUT
    }

    private static PrivateKey getSidMockKey() throws Exception {
        try (InputStream is = AuthenticationResponseValidator.class.getResourceAsStream("/sid-mock-ts.jks")) {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, "changeit".toCharArray());
            return (PrivateKey) keystore.getKey("sid-mock-key", "changeit".toCharArray());

        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException |
                 UnrecoverableKeyException e) {
            throw new Exception("Error retrieving SID mock key", e);
        }
    }

    private static Map<String, String> getCertificates() throws Exception {
        Map<String, String> certificates = new HashMap<>();

        try (InputStream is = AuthenticationResponseValidator.class.getResourceAsStream("/sid-mock-ts.jks")) {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, "changeit".toCharArray());
            Enumeration<String> enumeration = keystore.aliases();
            while (enumeration.hasMoreElements()) {
                String alias = enumeration.nextElement();
                if (!Objects.equals(alias, "sid-mock-key")) {
                    X509Certificate certificate = (X509Certificate) keystore.getCertificate(alias);
                    certificates.put(alias, Base64.getEncoder().encodeToString(certificate.getEncoded()));
                }
            }
        } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
            throw new Exception("Error retrieving certificates", e);
        }
        return certificates;
    }

    private static Map<String, UserResponseType> getUsersMapping() throws IOException {
        Map<String, UserResponseType> users = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();
        InputStream is = DataProvider.class.getResourceAsStream("/users.json");
        TypeReference<HashMap<String, List<String>>> typeRef = new TypeReference<>() {
        };
        HashMap<String, List<String>> sortedUsers = mapper.readValue(is, typeRef);
        for (var entry : sortedUsers.entrySet()) {
            for (var user : entry.getValue()) {
                users.put(user, UserResponseType.valueOf(entry.getKey()));
            }
        }
        return users;
    }

    public static void resetUsersMapping() throws IOException {
        usersMapping = getUsersMapping();
    }

    public void putResponseData(UUID sessionId, String identifier, SessionInitData inputData) throws Exception {
        long returnTime = System.currentTimeMillis() + sidMockProperties.delay().toMillis();
        ResponseData responseData = ResponseData.generateResponseData(identifier, inputData, returnTime);
        JsonMapper jsonMapper = new JsonMapper();
        this.redisConnection.sync().set(
                sessionId.toString(),
                jsonMapper.writeValueAsString(responseData),
                ex(sidMockProperties.expiration())
        );
    }

    public Map<String, Object> getResponseData(String sessionId, long timeoutMs) throws Exception {
        long timeLimit = System.currentTimeMillis() + timeoutMs;
        String redisResponseData = this.redisConnection.sync().get(sessionId);
        if (redisResponseData == null) {
            throw new NotFoundException("Session not found");
        }
        JsonMapper jsonMapper = new JsonMapper();
        try {
            ResponseData responseData = jsonMapper.readValue(redisResponseData, ResponseData.class);
            while (System.currentTimeMillis() < timeLimit) {
                if (System.currentTimeMillis() >= responseData.returnTime) {
                    return responseData.jsonBody;
                }
            }
            return Map.of("state", "RUNNING");
        } catch (JsonProcessingException e) {
            throw new Exception("Unable to parse stored response", e);
        }
    }

    public void putRequestData(String Identifier, SessionInitData inputData) throws JsonProcessingException {
        if (sidMockProperties.storeSessionInitRequests()) {
            JsonMapper jsonMapper = new JsonMapper();
            String response = jsonMapper.writeValueAsString(inputData);
            this.redisConnection.sync().set(
                    Identifier + "_Latest" + inputData.sessionType.label + "Request",
                    response,
                    ex(sidMockProperties.expiration())
            );
            this.redisConnection.sync().set(
                    Identifier + "_LatestRequest",
                    response,
                    ex(sidMockProperties.expiration())
            );
            this.redisConnection.sync().set(
                    "LatestRequest",
                    response,
                    ex(sidMockProperties.expiration())
            );
        }
    }

    public Map<String, Object> fetchRequestData(String key) throws Exception {
        String redisResponseData = this.redisConnection.sync().get(key);
        if (redisResponseData == null) {
            throw new NotFoundException("Latest request not found");
        }
        JsonMapper jsonMapper = new JsonMapper();
        try {
            return jsonMapper.readValue(redisResponseData, Map.class);
        } catch (JsonProcessingException e) {
            throw new Exception("Unable to parse stored request", e);
        }
    }
}
