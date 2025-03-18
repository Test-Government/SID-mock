package ee.test_gov.sid.mock.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class AllowedInteractionsOrder {

    // Known (but undocumented) limitation of Smart-ID API
    private static final char[] BAD_CHARS = {'\u0000', '\n', '\r'};

    String type;
    String displayText60;
    String displayText200;

    public boolean containsBadChars() {
        return containsBadChars(displayText60) || containsBadChars(displayText200);
    }

    private boolean containsBadChars(String text) {
        if (text == null) {
            return false;
        }
        for (char badChar : BAD_CHARS) {
            if (text.indexOf(badChar) >= 0) {
                return true;
            }
        }
        return false;
    }
}
