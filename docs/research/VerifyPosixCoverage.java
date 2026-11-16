import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Fails CI if the POSIX-specific master-key tests did not actually run. */
public final class VerifyPosixCoverage {
    private VerifyPosixCoverage() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java VerifyPosixCoverage.java <Surefire XML>");
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
        Set<String> required = new HashSet<>(Set.of("rejectsUnsafePosixFileAndDirectoryPermissions", "rejectsSymbolicLink"));
        var cases = document.getElementsByTagName("testcase");
        for (int i = 0; i < cases.getLength(); i++) {
            var test = (Element) cases.item(i);
            String name = test.getAttribute("name");
            if (required.contains(name)) {
                if (test.getElementsByTagName("skipped").getLength() != 0
                        || test.getElementsByTagName("failure").getLength() != 0
                        || test.getElementsByTagName("error").getLength() != 0) {
                    throw new IllegalStateException("Required POSIX security test did not pass: " + name);
                }
                required.remove(name);
            }
        }
        if (!required.isEmpty()) {
            throw new IllegalStateException("Required POSIX security tests missing: " + required);
        }
        System.out.println("JOSS03_POSIX_COVERAGE=PASS required permission and symlink tests executed");
    }
}
