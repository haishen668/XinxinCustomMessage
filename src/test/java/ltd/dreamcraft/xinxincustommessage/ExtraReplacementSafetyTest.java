import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExtraReplacementSafetyTest {
    public static void main(String[] args) throws Exception {
        assertNoRegexReplacement(
                "src/main/java/ltd/dreamcraft/xinxincustommessage/listeners/MessageListener.java",
                "MessageListener should not use replaceAll for {extra}"
        );
        assertNoRegexReplacement(
                "src/main/java/ltd/dreamcraft/xinxincustommessage/objects/CustomImage.java",
                "CustomImage should not use replaceAll for {extra}"
        );

        String extra = "$100\\path";
        String result = "cmd {extra}".replace("{extra}", extra);
        assertEquals("cmd $100\\path", result, "plain replace preserves $ and backslash");

        System.out.println("PASS: extra replacement is literal-safe");
    }

    private static void assertNoRegexReplacement(String path, String message) throws Exception {
        String source = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        if (source.contains("replaceAll(\"\\\\{extra}\"")) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(String expected, String actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }
}
