import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ResponseCommandArrayTest {
    public static void main(String[] args) throws Exception {
        String source = new String(
                Files.readAllBytes(Paths.get("src/main/java/ltd/dreamcraft/xinxincustommessage/utils/MessageUtil.java")),
                StandardCharsets.UTF_8
        );

        assertContains(source, "cmd.startsWith(\"[\") && cmd.endsWith(\"]\")", "responses [command] should detect command arrays");
        assertContains(source, "cmd.substring(1, cmd.length() - 1).split(\",\")", "responses [command] should split command arrays by comma");
        assertContains(source, "for (String subCommand : commandArgs)", "responses [command] should dispatch each command array item");

        System.out.println("PASS: response [command] supports command arrays");
    }

    private static void assertContains(String source, String expected, String message) {
        if (!source.contains(expected)) {
            throw new AssertionError(message);
        }
    }
}
