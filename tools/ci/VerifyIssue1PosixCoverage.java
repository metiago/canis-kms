import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;

/** Fails CI if the Issue 1 POSIX master-key tests did not pass. */
public final class VerifyIssue1PosixCoverage {
    private static final Set<String> REQUIRED = Set.of(
            "rejectsSymbolicLink",
            "rejectsAncestorDirectorySymbolicLinks",
            "rejectsUnsafePosixFileAndDirectoryPermissions",
            "rejectsUnreadablePosixKey",
            "rejectsPosixFileAndDirectoryOwnerMismatch");

    private VerifyIssue1PosixCoverage() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java VerifyIssue1PosixCoverage.java <Surefire XML>");
        }
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        var document = factory.newDocumentBuilder().parse(Path.of(args[0]).toFile());
        Set<String> passed = new HashSet<>();
        var cases = document.getElementsByTagName("testcase");
        for (int i = 0; i < cases.getLength(); i++) {
            var test = (Element) cases.item(i);
            String name = test.getAttribute("name");
            if (REQUIRED.contains(name)) {
                if (test.getElementsByTagName("skipped").getLength() != 0
                        || test.getElementsByTagName("failure").getLength() != 0
                        || test.getElementsByTagName("error").getLength() != 0) {
                    throw new IllegalStateException("Required Issue 1 POSIX security test did not pass: " + name);
                }
                passed.add(name);
            }
        }
        Set<String> missing = new HashSet<>(REQUIRED);
        missing.removeAll(passed);
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Required Issue 1 POSIX security tests missing: " + missing);
        }
        System.out.println("CANIS_ISSUE1_POSIX_COVERAGE=PASS required owner, permission, unreadable-file, and symlink tests executed");
    }
}
