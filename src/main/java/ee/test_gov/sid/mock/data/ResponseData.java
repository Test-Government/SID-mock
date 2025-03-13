package ee.test_gov.sid.mock.data;

import ee.sk.smartid.HashType;
import javassist.NotFoundException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Getter
@Setter
public class ResponseData {
    Map<String, Object> jsonBody;
    long returnTime;

    public ResponseData() {
    }

    public ResponseData(Map<String, Object> jsonBody, long returnTime) {
        this.jsonBody = jsonBody;
        this.returnTime = returnTime;
    }

    private static byte[] addPadding(byte[] digestInfoPrefix, byte[] digest) {
        final byte[] digestWithPrefix = new byte[digestInfoPrefix.length + digest.length];
        System.arraycopy(digestInfoPrefix, 0, digestWithPrefix, 0, digestInfoPrefix.length);
        System.arraycopy(digest, 0, digestWithPrefix, digestInfoPrefix.length, digest.length);
        return digestWithPrefix;
    }

    private static byte[] generateSignature(String hash, String hashType) throws GeneralSecurityException {
        Signature sign = Signature.getInstance("NONEwithRSA");
        sign.initSign(DataProvider.privateKey);
        byte[] bytes = Base64.getDecoder().decode(hash);
        byte[] paddedHash = addPadding(HashType.valueOf(hashType).getDigestInfoPrefix(), bytes);
        sign.update(paddedHash);
        return sign.sign();
    }

    public static ResponseData generateResponseData(String identifier, SessionInitData inputData, long returnTime) throws Exception {
        Map<String, Object> jsonBody = new HashMap<>();
        Map<String, Object> result = new HashMap<>();

        if (!DataProvider.usersMapping.containsKey(identifier)) {
            throw new NotFoundException("User not found");
        }
        DataProvider.UserResponseType expectedResult = DataProvider.usersMapping.get(identifier);

        if (expectedResult == DataProvider.UserResponseType.OK) {

            String certificate = switch (inputData.getSessionType()) {
                case AUTHENTICATION -> DataProvider.certificates.get(
                        String.format("%s-mock-q.auth", identifier.toLowerCase()));
                case CERTIFICATE_CHOICE, SIGNING -> DataProvider.certificates.get(
                        String.format("%s-mock-q.sign", identifier.toLowerCase()));
            };

            result.put("endResult", "OK");
            result.put("documentNumber", Optional.ofNullable(inputData.getDocumentNumber())
                    .orElse(identifier + "-MOCK-Q"));

            Map<String, Object> cert = new HashMap<>();
            cert.put("value", certificate);
            cert.put("certificateLevel", "QUALIFIED");

            jsonBody.put("state", "COMPLETE");
            jsonBody.put("result", result);
            jsonBody.put("cert", cert);

            if (inputData.getSessionType() == DataProvider.SessionType.AUTHENTICATION
                    || inputData.getSessionType() == DataProvider.SessionType.SIGNING) {
                Map<String, Object> signature = new HashMap<>();
                signature.put("value", Base64.getEncoder()
                        .encodeToString(ResponseData.generateSignature(inputData.getHash(), inputData.getHashType())));
                signature.put("algorithm", inputData.getHashType().toLowerCase() + "WithRSAEncryption");

                jsonBody.put("signature", signature);
                jsonBody.put("interactionFlowUsed", inputData.getAllowedInteractionsOrder().get(0).getType());
            }

        } else if (inputData.getSessionType() == DataProvider.SessionType.CERTIFICATE_CHOICE) {
            result.put("endResult", "OK");
            result.put("documentNumber", Optional.ofNullable(inputData.getDocumentNumber())
                    .orElse(identifier + "-MOCK-Q"));

            Map<String, Object> cert = new HashMap<>();
            cert.put("value", DataProvider.certificates.get(String.format("%s-mock-q.sign", identifier.toLowerCase())));
            cert.put("certificateLevel", "QUALIFIED");

            jsonBody.put("state", "COMPLETE");
            jsonBody.put("result", result);
            jsonBody.put("cert", cert);

        } else {
            result.put("endResult", expectedResult.toString());

            jsonBody.put("state", "COMPLETE");
            jsonBody.put("result", result);
        }
        return new ResponseData(jsonBody, returnTime);
    }
}
