import com.android.apksig.ApkVerifier;
import com.android.apksig.internal.apk.AndroidBinXmlParser;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.zip.ZipFile;

/** Read-only binary verification using the locally cached Android apksig library. */
public class VerifyApk {
    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            Path apk = Path.of(arg);
            var verified = new ApkVerifier.Builder(apk.toFile()).setMinCheckedPlatformVersion(28)
                .setMaxCheckedPlatformVersion(28).build().verify();
            System.out.println(arg + ": verified for API 28=" + verified.isVerified()
                + ", v2=" + verified.isVerifiedUsingV2Scheme() + ", v3=" + verified.isVerifiedUsingV3Scheme());
            if (!verified.isVerified()) throw new IllegalStateException(verified.getErrors().toString());
            for (var certificate : verified.getSignerCertificates()) {
                System.out.println("signer SHA-256=" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(certificate.getEncoded())));
            }
            try (var zip = new ZipFile(apk.toFile())) {
                var manifest = new AndroidBinXmlParser(ByteBuffer.wrap(zip.getInputStream(zip.getEntry("AndroidManifest.xml")).readAllBytes()));
                for (int e; (e = manifest.next()) != AndroidBinXmlParser.EVENT_END_DOCUMENT;) {
                    if (e != AndroidBinXmlParser.EVENT_START_ELEMENT || !(manifest.getName().equals("manifest") || manifest.getName().equals("uses-sdk"))) continue;
                    for (int i = 0; i < manifest.getAttributeCount(); i++) {
                        String name = manifest.getAttributeName(i);
                        if (name.equals("package") || name.equals("versionCode") || name.equals("minSdkVersion") || name.equals("targetSdkVersion")) {
                            Object value = manifest.getAttributeValueType(i) == AndroidBinXmlParser.VALUE_TYPE_INT
                                ? manifest.getAttributeIntValue(i) : manifest.getAttributeStringValue(i);
                            System.out.println(name + "=" + value);
                        }
                    }
                }
                System.out.println("native library entries=" + zip.stream().filter(e -> e.getName().startsWith("lib/")).count());
                System.out.println("Aqua defaults present=" + (zip.getEntry("assets/aqua-defaults.json") != null));
            }
        }
    }
}
