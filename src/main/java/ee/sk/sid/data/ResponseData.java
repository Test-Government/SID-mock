package ee.sk.sid.data;

import ee.sk.smartid.HashType;
import javassist.NotFoundException;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ResponseData {
    public Map<String, Object> jsonBody;
    public long returnTime;

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

    private static byte[] generateSignature(String hash) throws GeneralSecurityException {
        Signature sign = Signature.getInstance("NONEwithRSA");
        sign.initSign(DataProvider.privateKey);
        byte[] bytes = Base64.getDecoder().decode(hash);
        byte[] paddedHash = addPadding(HashType.valueOf("SHA512").getDigestInfoPrefix(), bytes);
        sign.update(paddedHash);
        return sign.sign();
    }

    public static ResponseData generateResponseData(String identifier, AuthenticationInitData inputData, long returnTime) throws Exception {
        Map<String, Object> jsonBody = new HashMap<>();
        Map<String, Object> result = new HashMap<>();

        if (!DataProvider.usersMapping.containsKey(identifier)) {
            throw new NotFoundException("User not found");
        }
        DataProvider.UserResponseType expectedResult = DataProvider.usersMapping.get(identifier);

        if (expectedResult == DataProvider.UserResponseType.OK) {

            String certificate = DataProvider.certificates.get(
                    String.format("%s-mock-q.auth", identifier.toLowerCase()));

            result.put("endResult", "OK");
            result.put("documentNumber", identifier);

            Map<String, Object> signature = new HashMap<>();
            signature.put("value", Base64.getEncoder().encodeToString(ResponseData.generateSignature(inputData.hash)));
            signature.put("algorithm", "sha512WithRSAEncryption");

            Map<String, Object> cert = new HashMap<>();
            cert.put("value", certificate);
            cert.put("certificateLevel", "QUALIFIED");

            jsonBody.put("state", "COMPLETE");
            jsonBody.put("result", result);
            jsonBody.put("signature", signature);
            jsonBody.put("cert", cert);
            jsonBody.put("interactionFlowUsed", "verificationCodeChoice");

        } else {
            result.put("endResult", expectedResult.toString());

            jsonBody.put("state", "COMPLETE");
            jsonBody.put("result", result);
        }
        return new ResponseData(jsonBody, returnTime);
    }
}
