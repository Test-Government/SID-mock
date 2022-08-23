package ee.test_gov.sid.mock.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.micronaut.core.annotation.Introspected;
import lombok.ToString;

import java.util.List;

@ToString
@Introspected
@JsonIgnoreProperties(value = {"sessionType", "documentNumber"})
public class SessionInitData {
    public String relyingPartyUUID;
    public String relyingPartyName;
    public String hash;
    public String hashType;
    public List<AllowedInteractionsOrder> allowedInteractionsOrder;
    public DataProvider.SessionType sessionType;
    public String documentNumber;
}

@ToString
class AllowedInteractionsOrder {
    public String type;
    public String displayText60;
    public String displayText200;

}
