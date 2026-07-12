package dev.haja;

import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.util.regex.Pattern;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(
        packages = "dev.haja",
        importOptions = ArchitectureTest.DoNotIncludeAotGenerated.class
)
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

    @ArchTest
    static final ArchRule layerRule = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Repository").definedBy("..repository..")
            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
//            .allowEmptyShould(true)   // ← 빈 레이어 허용
            ;
}