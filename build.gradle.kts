plugins {
    java
    pmd
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.hibernate)
    alias(libs.plugins.graalvm.native)
}

group = "dev.haja"
version = "0.0.1-SNAPSHOT"
description = "spring-template-simple-java"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.h2console)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.mapstruct)
    implementation(libs.mapstruct.spring.annotations)
    compileOnly(libs.lombok)
    developmentOnly(libs.spring.boot.devtools)
    runtimeOnly(libs.h2)
    // 순서 중요: lombok → lombok-mapstruct-binding → mapstruct-processor
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.lombok.mapstruct.binding)
    annotationProcessor(libs.mapstruct.processor)
    annotationProcessor(libs.mapstruct.spring.extensions)
    annotationProcessor(libs.spring.boot.configuration.processor)
    testImplementation(libs.spring.boot.starter.data.jpa.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testCompileOnly(libs.lombok)
    testRuntimeOnly(libs.junit.platform.launcher)
    // 순서 중요: lombok → lombok-mapstruct-binding → mapstruct-processor
    testAnnotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.lombok.mapstruct.binding)
    testAnnotationProcessor(libs.mapstruct.processor)
    testImplementation(libs.archunit.junit5)
}

pmd {
    toolVersion = libs.versions.pmd.get()
    ruleSetFiles = files(".github/pmd/ruleset.xml")
    ruleSets = emptyList() // Gradle 기본 룰셋 제거, 커스텀 룰만 적용
    sourceSets = listOf(project.sourceSets["main"]) // 테스트 코드 룰 적용 제외
    isConsoleOutput = true
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

// 일반 Java 컴파일에서는 경고 활성화
tasks.named("compileJava", JavaCompile::class) {
    options.compilerArgs.add("-Xlint:unchecked")
}

// AOT 컴파일 태스크에서는 생성된 코드의 경고 완전 제거
tasks.named("compileAotJava", JavaCompile::class) {
    options.compilerArgs.addAll(listOf(
        "-Xlint:none"  // 모든 경고 완전 제거
    ))
}

// NOTE: processTestAot는 활성화 유지.
// 네이티브 테스트(nativeTest)에서 Spring TestContext 프레임워크가 동작하려면
// 테스트 AOT가 생성하는 리플렉션/리소스 메타데이터가 필요하다.
// (비활성화 시 BootstrapUtils 초기화 실패 → WebAppConfiguration ClassNotFoundException)


// GraalVM 네이티브 이미지: Hibernate ByteBuddy BytecodeProvider 서비스 디스크립터를 이미지에서 제외.
// 최신 GraalVM(JDK 25)은 서비스 디스크립터 리소스를 무조건 이미지에 포함하는데, spring-orm은
// ServiceLoaderFeature 등록만 배제하므로 런타임 ServiceLoader가 디스크립터는 읽되 클래스는 못 찾아
// "BytecodeProviderImpl not found"로 JPA 컨텍스트 로드가 실패한다(spring-framework#35118).
// 리소스 자체를 제외하면 ServiceLoader 결과가 비고, Hibernate 7.x가 no-op(none) BytecodeProvider로
// 폴백한다(BytecodeProviderInitiator.getBytecodeProvider: 빈 iterator → new none.BytecodeProviderImpl).
// 네이티브 런타임은 런타임 바이트코드 생성이 불가하므로 none provider가 정상 경로다.
graalvmNative {
    binaries.all {
        buildArgs.add("-H:ExcludeResources=META-INF/services/org\\.hibernate\\.bytecode\\.spi\\.BytecodeProvider")
    }
}
