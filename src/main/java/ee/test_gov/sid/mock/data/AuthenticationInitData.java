package ee.test_gov.sid.mock.data;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class AuthenticationInitData {
    public String relyingPartyUUID;
    public String relyingPartyName;
    public String hash;
    public String hashType;

}
