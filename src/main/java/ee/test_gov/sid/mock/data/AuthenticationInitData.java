package ee.test_gov.sid.mock.data;

import io.micronaut.core.annotation.Introspected;
import lombok.ToString;

import java.util.List;

@ToString
@Introspected
public class AuthenticationInitData {
    public String relyingPartyUUID;
    public String relyingPartyName;
    public String hash;
    public String hashType;
    public List<AllowedInteractionsOrder> allowedInteractionsOrder;
}

@ToString
class AllowedInteractionsOrder {
    public String type;
    public String displayText60;
    public String displayText200;

}
