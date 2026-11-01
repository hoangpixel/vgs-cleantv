import com.sun.source.util.JavacTask;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer;
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement;
import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.kotlin.psi.KtPsiFactory;

/** Parse only. This does not resolve Android APIs, Compose, or Gradle plugins and is not an Android build. */
public class SyntaxCheck {
    public static void main(String[] args) throws Exception {
        List<Path> java = new ArrayList<>(), kotlin = new ArrayList<>();
        for (String root : args) try (var paths = Files.walk(Path.of(root))) {
            paths.filter(Files::isRegularFile).forEach(p -> {
                if (p.toString().endsWith(".java")) java.add(p);
                if (p.toString().endsWith(".kt") || p.toString().endsWith(".kts")) kotlin.add(p);
            });
        }
        int errors = 0;
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (var manager = compiler.getStandardFileManager(diagnostics, Locale.ROOT, StandardCharsets.UTF_8)) {
            var task = (JavacTask) compiler.getTask(null, manager, diagnostics,
                List.of("--release", "17", "-proc:none"), null,
                manager.getJavaFileObjectsFromPaths(java));
            task.parse();
            for (var d : diagnostics.getDiagnostics()) if (d.getKind() == Diagnostic.Kind.ERROR) {
                System.err.println(d); errors++;
            }
        }
        var disposable = Disposer.newDisposable();
        try {
            var env = KotlinCoreEnvironment.createForProduction(disposable, new CompilerConfiguration(), EnvironmentConfigFiles.JVM_CONFIG_FILES);
            var factory = new KtPsiFactory(env.getProject(), false);
            for (Path p : kotlin) {
                var tree = factory.createFile(p.getFileName().toString(), Files.readString(p, StandardCharsets.UTF_8).replace("\r\n", "\n"));
                for (var error : PsiTreeUtil.collectElementsOfType(tree, PsiErrorElement.class)) {
                    if (errors < 20) System.err.println(p + ": " + error.getErrorDescription());
                    errors++;
                }
            }
        } finally { Disposer.dispose(disposable); }
        System.out.println("Parsed Java=" + java.size() + ", Kotlin/scripts=" + kotlin.size() + ", syntax errors=" + errors);
        if (errors != 0) System.exit(1);
    }
}
