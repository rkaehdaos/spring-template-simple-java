package dev.haja;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;

import java.util.regex.Pattern;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * 레이어드 아키텍처(Controller → Service → Repository 단방향)를 검증한다.
 *
 * <p>ArchUnit은 클래스패스의 {@code .class} 바이트코드를 런타임에 읽어 분석하므로
 * {@code .class} 파일이 존재하지 않는 GraalVM 네이티브 이미지에서는 동작할 수 없다.
 * 따라서 {@link DisabledInNativeImage}로 {@code nativeTest} 실행 시 건너뛴다.
 * (ArchUnit 전용 엔진의 {@code @ArchTest} 방식은 Jupiter 조건부 실행을 평가하지 않으므로,
 * 조건이 동작하도록 일반 {@code @Test} + ArchUnit core API 방식으로 작성한다.)
 */
@DisabledInNativeImage
class ArchitectureTest {

    /**
     * Spring Boot AOT(및 GraalVM native)가 생성하는 클래스는
     * {@code build/classes/java/aot}, {@code aotTest} 아래에 컴파일되며
     * 모든 레이어를 직접 참조하므로 아키텍처 검증 대상에서 제외한다.
     */
    static final class DoNotIncludeAotGenerated implements ImportOption {
        private static final Pattern AOT_CLASSES =
                Pattern.compile(".*/classes/java/aot(Test)?/.*");

        @Override
        public boolean includes(Location location) {
            return !location.matches(AOT_CLASSES);
        }
    }

    @Test
    void layerRule() {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(new DoNotIncludeAotGenerated())
                .importPackages("dev.haja");

        layeredArchitecture()
                .consideringAllDependencies()
                .layer("Controller").definedBy("..controller..")
                .layer("Service").definedBy("..service..")
                .layer("Repository").definedBy("..repository..")
                .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
                .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
                .check(classes);
    }
}
