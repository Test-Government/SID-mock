package ee.test_gov.sid.mock.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class AllowedInteractionsOrder {
    String type;
    String displayText60;
    String displayText200;

    public boolean containsNullByte() {
        return displayText60 != null && displayText60.contains("\u0000")
                || displayText200 != null && displayText200.contains("\u0000");
    }
}
