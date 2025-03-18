package ee.test_gov.sid.mock.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Introspected
@JsonIgnoreProperties(value = {"sessionType", "documentNumber"})
@Getter
@Setter
@ToString
public class SessionInitData {
    String relyingPartyUUID;
    String relyingPartyName;
    String hash;
    String hashType;
    List<AllowedInteractionsOrder> allowedInteractionsOrder;
    DataProvider.SessionType sessionType;
    String documentNumber;
}
