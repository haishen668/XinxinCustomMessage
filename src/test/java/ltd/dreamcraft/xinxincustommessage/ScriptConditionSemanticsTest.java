import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ScriptConditionSemanticsTest {
    public static void main(String[] args) throws Exception {
        String source = new String(
                Files.readAllBytes(Paths.get("src/main/java/ltd/dreamcraft/xinxincustommessage/listeners/MessageListener.java")),
                StandardCharsets.UTF_8
        );

        assertTrue(
                source.contains("if (conditionMet)"),
                "scripts should execute the blocking action when the blocking condition evaluates to true"
        );
        assertFalse(
                source.contains("if (!conditionMet)"),
                "scripts must not execute the blocking action when the blocking condition evaluates to false"
        );

        System.out.println("PASS: script condition actions run when condition is true");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }
}
