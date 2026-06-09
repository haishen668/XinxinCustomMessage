import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DownloadAndZipSafetyTest {
    public static void main(String[] args) throws Exception {
        String source = new String(
                Files.readAllBytes(Paths.get("src/main/java/ltd/dreamcraft/xinxincustommessage/XinxinCustomMessage.java")),
                StandardCharsets.UTF_8
        );

        assertContains(source, "connection.setConnectTimeout", "saveWebResource should set connect timeout");
        assertContains(source, "connection.setReadTimeout", "saveWebResource should set read timeout");
        assertContains(source, "getCanonicalPath()", "unzip should canonicalize entry paths");
        assertContains(source, "startsWith(destDir.getCanonicalPath()", "unzip should reject entries outside destination");
        assertContains(source, ".download", "saveWebResource should write to a temporary download file first");
        assertContains(source, "Files.move", "saveWebResource should move temp file into place only after successful download");

        System.out.println("PASS: download and zip safety guards are present");
    }

    private static void assertContains(String source, String expected, String message) {
        if (!source.contains(expected)) {
            throw new AssertionError(message);
        }
    }
}
