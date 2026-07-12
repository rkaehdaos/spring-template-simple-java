# 새 프로젝트 부트스트랩 프롬프트 (spring-template-simple-java 템플릿)

이 문서는 [spring-template-simple-java](https://github.com/rkaehdaos/spring-template-simple-java)를 템플릿 삼아
**빈 디렉토리에 동일한 구성의 새 Spring Boot 프로젝트를 생성**하기 위한 실행 프롬프트다.
Claude Opus/Sonnet 급 모델이 이 문서만 보고 프로젝트 전체를 재현할 수 있도록,
모든 파일의 전체 내용을 포함한다.

## 사용법 (사람용 안내)

1. 새 프로젝트용 **빈 디렉토리**를 만들고 그 안에서 Claude Code를 실행한다.
2. 아래 수평선(`---`) 이하 **실행 프롬프트 전체**를 복사해 붙여넣는다.
   이 파일 전체를 그대로 붙여넣어도 된다 (이 안내 부분은 실행에 영향 없음).
3. 프롬프트 첫 부분의 `PROJECT_NAME` 값만 새 프로젝트 이름으로 바꾸거나,
   붙여넣은 뒤 대화로 이름을 알려준다.
4. 완료 후 §7 "완료 기준 체크리스트"가 모두 만족되는지 확인한다.

> **스냅샷 기준**: 2026-07-12, 원본 저장소 커밋 `ff761a8`(feat: JHipster 제네릭 EntityMapper 패턴 도입) 시점의 워킹트리.
> 원본 템플릿이 갱신되면 이 문서도 함께 갱신할 것.

---

# 실행 프롬프트 (여기부터 모델에게 전달)

**PROJECT_NAME:** `my-new-project` ← 이 값만 새 프로젝트 이름(kebab-case)으로 수정

너는 지금부터 현재 디렉토리(빈 디렉토리)에 위 PROJECT_NAME으로 Spring Boot 4.x 기반 Java 프로젝트를 생성한다.
이 문서에 생성해야 할 **모든 파일의 전체 내용**이 포함되어 있다. 너의 임무는 이 내용을 **정확히 재현**하는 것이며,
창의적 변형은 임무 실패다. 아래 §1부터 §7까지 순서대로 수행하라.

이 프로젝트는 단순한 스켈레톤이 아니다. 각 샘플 코드는 라이브러리 설정이 실제로 동작함을 **증명하는 검증 장치**다:

- **MapStruct + Lombok 연동**: annotation processor 순서(`lombok` → `lombok-mapstruct-binding` → `mapstruct-processor`)가 깨지면 컴파일이 실패하도록 설계됨
- **mapstruct-spring-extensions**: 매퍼가 스프링 `ConversionService`에 자동 등록됨을 통합 테스트로 증명
- **제네릭 매퍼 계층**(JHipster 패턴): `DtoMapper` → `EntityMapper` → 구체 매퍼, `partialUpdate` 부분 갱신 포함
- **ArchUnit**: Controller → Service → Repository 단방향 레이어 강제
- **PMD**: 메서드 NCSS 30줄 제한 커스텀 룰
- **GraalVM 네이티브 이미지** 대응 설정 포함

상세 배경은 §3.30의 CLAUDE.md 내용에 있다. 그 문서가 새 프로젝트의 작업 지침이 된다.

## §0. 작업 원칙 (반드시 준수)

1. **파일 내용은 이 문서의 코드블록 그대로 생성한다.** 재포맷, 주석 제거/추가, 번역, import 정리, "개선" 모두 금지.
2. 치환 대상은 §1에 정의된 플레이스홀더(`{{PROJECT_NAME}}`, `{{BASE_PACKAGE}}`, `{{BASE_PACKAGE_PATH}}`, `{{APP_CLASS}}`)뿐이다. 코드블록 안의 그 외 문자는 한 글자도 바꾸지 않는다.
3. **버전 변경 금지.** `gradle/libs.versions.toml`, Gradle 9.6.1, Java 25 toolchain을 그대로 쓴다. "최신 버전이 있다"는 이유로 올리지 않는다.
4. **Spring Initializr(start.spring.io) 사용 금지.** 모든 파일은 이 문서에서 직접 생성한다.
5. 의존성 추가/제거 금지. `build.gradle.kts`의 annotationProcessor **선언 순서**를 절대 바꾸지 않는다 (main/test 양쪽 모두).
6. 각 파일은 마지막 줄 뒤 개행 문자(newline)로 끝나게 저장한다.
7. 단계 실행 중 실패하면 §6의 진단표를 먼저 확인하고, 원인이 문서와의 불일치라면 해당 파일을 문서 원문과 대조해 수정한다.

## §1. 파라미터와 치환 규칙

`PROJECT_NAME`이 문서 기본값(`my-new-project`) 그대로이고 사용자가 별도로 이름을 지정하지 않았다면,
진행하지 말고 사용자에게 프로젝트 이름을 물어라.

### 1.1 플레이스홀더 도출 규칙

| 플레이스홀더 | 도출 규칙 | 예시 A (원본 템플릿) | 예시 B |
|---|---|---|---|
| `{{PROJECT_NAME}}` | 입력값 그대로 (kebab-case) | `spring-template-simple-java` | `my-shop-api` |
| `{{BASE_PACKAGE}}` | `dev.haja.` + PROJECT_NAME에서 하이픈(`-`) 제거 (모두 소문자) | `dev.haja.springtemplatesimplejava` | `dev.haja.myshopapi` |
| `{{BASE_PACKAGE_PATH}}` | BASE_PACKAGE의 `.`을 `/`로 치환 | `dev/haja/springtemplatesimplejava` | `dev/haja/myshopapi` |
| `{{APP_CLASS}}` | PROJECT_NAME을 하이픈 기준으로 나눠 각 토큰의 첫 글자를 대문자화(PascalCase)한 뒤 `Application`을 붙임 | `SpringTemplateSimpleJavaApplication` | `MyShopApiApplication` |

숫자가 포함된 토큰은 첫 글자만 대문자화한다: `api2-server` → `Api2ServerApplication`.

### 1.2 PROJECT_NAME 유효성 검증 (생성 시작 전에 수행)

다음 중 하나라도 위반하면 진행하지 말고 사용자에게 알려라:

- 정규식 `^[a-z][a-z0-9]*(-[a-z0-9]+)*$`를 만족해야 한다 (소문자 시작, kebab-case).
- 하이픈 제거 결과(패키지 마지막 세그먼트)가 Java 예약어(`class`, `int`, `import` 등)면 안 된다.
- 하이픈 제거 결과가 정확히 `controller`, `service`, `repository`면 안 된다.
  ArchUnit 레이어 규칙(`ArchitectureTest`)이 이 이름의 패키지 세그먼트를 레이어로 간주해 빌드가 깨진다.
  (실측상 `..service..` 패턴은 정확히 `service`라는 세그먼트에만 매칭되고 `paymentservice` 같은
  부분 문자열에는 매칭되지 않으므로 그런 합성어는 동작한다. 다만 혼동을 피하려면
  프로젝트 이름에 이 세 단어를 아예 쓰지 않는 것을 권장한다.)

## §2. 사전 조건 확인

```bash
ls -A .            # 빈 디렉토리인지 확인
mise --version     # mise 필요 (Java 툴체인 설치용)
git --version
```

- 디렉토리가 비어 있지 않으면 **중단**하고 사용자에게 확인받는다.
  단, `.git` 디렉토리만 존재하는 경우(빈 저장소를 clone한 경우)는 계속 진행하되 §7의 `git init`만 건너뛴다.
- `mise`가 없으면 사용자에게 설치를 요청한다 (<https://mise.jdx.dev>). 대안으로 JDK 25 + Gradle이 직접 설치되어 있어도 되지만, 이 문서는 mise 기준으로 기술한다.
- Maven Central·Gradle 배포 서버에 접근 가능한 네트워크가 필요하다.

## §3. 파일 생성

아래 트리의 파일 31개를 §3.1 ~ §3.31의 내용으로 생성한다.
`gradlew`, `gradlew.bat`, `gradle/wrapper/*`는 §4에서 만든다.

```text
{{PROJECT_NAME}}/
├── .gitattributes
├── .github/
│   └── pmd/
│       └── ruleset.xml
├── .gitignore
├── .gitmessage.txt
├── CLAUDE.md
├── PULL_REQUEST_TEMPLATE.md
├── build.gradle.kts
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/                        ← §4에서 생성
├── gradlew                             ← §4에서 생성
├── gradlew.bat                         ← §4에서 생성
├── mise.toml                           (.gitignore에 의해 git 미추적 — 의도된 동작)
├── settings.gradle.kts
└── src/
    ├── main/
    │   ├── java/{{BASE_PACKAGE_PATH}}/
    │   │   ├── {{APP_CLASS}}.java
    │   │   ├── controller/SampleController.java
    │   │   ├── domain/SampleEntity.java
    │   │   ├── dto/Sample.java
    │   │   ├── dto/SampleDto.java
    │   │   ├── dto/SampleEntityDto.java
    │   │   ├── mapper/CentralMapperConfig.java
    │   │   ├── mapper/DtoMapper.java
    │   │   ├── mapper/EntityMapper.java
    │   │   ├── mapper/MapstructSpringConfig.java
    │   │   ├── mapper/SampleEntityMapper.java
    │   │   ├── mapper/SampleMapper.java
    │   │   ├── repository/SampleRepository.java
    │   │   └── service/SampleService.java
    │   └── resources/
    │       └── application.yaml
    └── test/
        └── java/
            ├── dev/haja/ArchitectureTest.java      ← 주의: BASE_PACKAGE가 아니라 dev/haja 바로 아래
            └── {{BASE_PACKAGE_PATH}}/
                ├── {{APP_CLASS}}Tests.java
                └── mapper/
                    ├── MapstructSpringIntegrationTest.java
                    ├── SampleEntityMapperUnitTest.java
                    └── SampleMapperUnitTest.java
```

> **주의**: `ArchitectureTest.java`는 `src/test/java/dev/haja/`에 둔다. `{{BASE_PACKAGE_PATH}}` 하위가 아니다.
> `importPackages("dev.haja")`로 그룹 전체를 스캔하기 위한 의도적 배치다.

### §3.1 `mise.toml`

Java 툴체인을 프로젝트 단위로 고정한다. `.gitignore`에 의해 git에는 올라가지 않는다(로컬 전용).

````toml
[tools]
java = "oracle-graalvm-25.0.3"
````

### §3.2 `.gitattributes`

````text
/gradlew text eol=lf
*.bat text eol=crlf
*.jar binary
````

### §3.3 `.gitignore`

````text
HELP.md
.gradle
build/
!gradle/wrapper/gradle-wrapper.jar
!**/src/main/**/build/
!**/src/test/**/build/

### STS ###
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans
.sts4-cache
bin/
!**/src/main/**/bin/
!**/src/test/**/bin/

### IntelliJ IDEA ###
.idea
*.iws
*.iml
*.ipr
out/
!**/src/main/**/out/
!**/src/test/**/out/

### NetBeans ###
/nbproject/private/
/nbbuild/
/dist/
/nbdist/
/.nb-gradle/

### VS Code ###
.vscode/

### 로그 파일 ###
logs/
*.log

### 데이터베이스 ###
*.db
*.sqlite
*.h2.db

### OS별 파일 ###
.DS_Store
Thumbs.db
Desktop.ini

### claude code ###
.claude/
plans/
doc

### jdx.mise ###
mise.toml
````

### §3.4 `.gitmessage.txt`

커밋 메시지 템플릿. §7에서 `git config commit.template`으로 연결한다.

````text
# <type>(<scope>): <subject>
# |<----    최대 50자 사용 맞추려고 노력하기       ---->|
# <body>
# |<----               각 줄을 최대 72자로 제한해야 한다.                ---->|
# <footer>
#
# --- COMMIT END ---

# Type can be
#    ✨ feat      - 새로운 기능
#    🐛 fix       - 버그 수정
#    ⚡ perf      - 성능 개선
#    💎 improve   - 코드 개선
#    ♻️  refactor  - 리팩토링
#    📝 docs      - 문서 수정
#    💄 style     - 코드 스타일 변경 (포맷팅, 세미콜론 등)
#    ✅ test      - 테스트 추가/수정
#    📦 chore     - 기타 변경사항 (빌드 스크립트, 패키지 매니저 등)
#    🎉 release   - 새 버전 릴리즈
#    🔨 build     - 빌드 시스템 또는 외부 의존성 변경
#    💚 ci        - CI 설정 파일 및 스크립트 변경
#    🔥 remove    - 코드/파일 삭제
#    ⏪ revert    - 커밋 되돌리기
#    🔒 security  - 보안 이슈 수정
#    🚨 hotfix    - 긴급 수정 (프로덕션 이슈)
#    ⬆️  upgrade   - 의존성 업그레이드
#    ⬇️  downgrade - 의존성 다운그레이드
# --------------------
# Scope can be
#    auth     - 인증 관련
#    api      - API 관련
#    ui       - UI/UX 관련
#    db       - 데이터베이스 관련
#    config   - 설정 관련
#    core     - 핵심 비즈니스 로직
#    util     - 유틸리티 함수
#    test     - 테스트 관련
#    deps     - 의존성 관련
#    infra    - 인프라 관련
#    admin    - 어드민 관련
#    user     - 사용자 기능 관련
# --------------------
# Subject Rules:
#    1. 명령문, 현재 시제 사용 ("changed" 대신 "change")
#    2. 첫 글자 소문자 사용
#    3. 마침표(.) 사용하지 않기
#    4. 50자 이내로 작성
# --------------------
# Body Rules:
#    1. 한 줄당 72자 이내로 작성
#    2. 어떻게(How)보다 무엇을(What), 왜(Why) 설명
#    3. 변경 이유와 이전과의 차이점 설명
#    4. 현재 시제 사용
# --------------------
# Footer Rules:
#    중대한 변경 사항: <중대한 변경 사항 설명>
#    See also: #<issue-number>, #<issue-number>
#    Co-authored-by: Kai Ahn <13996827+rkaehdaos@users.noreply.github.com>
#    Refs: [JIRA-XXX] #<GitHub issue-number>
````

### §3.5 `settings.gradle.kts`

````kotlin
rootProject.name = "{{PROJECT_NAME}}"
````

### §3.6 `gradle.properties`

````properties
# gradle.properties의 이상적 사용법
# 1. 환경 설정값 (Build Environment Configuration)
#   - 빌드 도구와 JVM 실행 환경을 구성하는 설정들
group=dev.haja
releaseVer=0.0.1-SNAPSHOT

#   1) 콘솔 출력 설정
#     - CI/CD 환경에서 로그 가독성 향상
#     - Jenkins, GitLab CI 등에서 ANSI 색상 코드가 깨지는 것 방지
org.gradle.console=auto

#     - 빌드 도구의 콘솔 인코딩을 제어하는 환경 설정
#     - 한글, 일본어, 중국어 등 멀티바이트 문자 출력 시 필수
#     - Windows 환경에서 특히 중요 (기본값이 CP949일 수 있음)
org.gradle.console.encoding=UTF-8

#   2) 빌드 성능 최적화
org.gradle.caching=true
# ci에서는 아래 비활성화
org.gradle.daemon=true
# 소규모 프로젝트(5모듈 이하)에서는 오버헤드가 더 클 수 있음
org.gradle.parallel=true

#   3) 네트워크 설정
#systemProp.http.proxyHost=proxy.haja.dev
#systemProp.http.proxyPort=8080

# =========================================================
# Gradle JVM Arguments
#
# 주의: 백슬래시(\) 뒤에 공백이 있으면 안됨
# 문서: https://docs.gradle.org/current/userguide/gradle_daemon.html
# =========================================================
#
# [인코딩] UTF-8 파일 처리
# [시간대] 한국 표준시 (KST)
# [Native] Java 17+ FFM API 허용
# [호환성] 미인식 JVM 옵션 무시
#
org.gradle.jvmargs=-Dfile.encoding=UTF-8 \
    -Duser.timezone=Asia/Seoul \
    --enable-native-access=ALL-UNNAMED \
    -XX:+IgnoreUnrecognizedVMOptions


# 2. 의존성/플러그인 버전 정보
# - Gradle Version Catalog로 이전: gradle/libs.versions.toml 참조
#   언어/프레임워크/라이브러리/플러그인 버전을 [versions]/[libraries]/[plugins]에서 일괄 관리

# KAPT 설정 (버전이 아닌 빌드 동작 설정이므로 여기서 관리)
kapt.include.compile.classpath=false
kapt.incremental.apt=true

# 3. 환경별 설정 (Environment-specific Configuration)
#  - 개발/스테이징/프로덕션 환경에 따라 달라지는 설정
#  - local, dev, demo, prod
application.env=local
````

### §3.7 `gradle/libs.versions.toml`

````toml
[versions]
spring-boot = "4.1.0"
spring-dependency-management = "1.1.7"
hibernate = "7.4.1.Final"
graalvm-native = "1.1.4"
archunit = "1.4.2"
mapstruct = "1.6.3"
mapstruct-spring = "1.1.3"
lombok-mapstruct-binding = "0.2.0"
pmd = "7.24.0"

[libraries]
# Spring Boot BOM 관리 대상 → 버전 미지정 (module만 선언)
spring-boot-h2console = { module = "org.springframework.boot:spring-boot-h2console" }
spring-boot-starter-data-jpa = { module = "org.springframework.boot:spring-boot-starter-data-jpa" }
spring-boot-starter-webmvc = { module = "org.springframework.boot:spring-boot-starter-webmvc" }
spring-boot-devtools = { module = "org.springframework.boot:spring-boot-devtools" }
spring-boot-configuration-processor = { module = "org.springframework.boot:spring-boot-configuration-processor" }
spring-boot-starter-data-jpa-test = { module = "org.springframework.boot:spring-boot-starter-data-jpa-test" }
spring-boot-starter-webmvc-test = { module = "org.springframework.boot:spring-boot-starter-webmvc-test" }
lombok = { module = "org.projectlombok:lombok" }
h2 = { module = "com.h2database:h2" }
junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher" }
# 명시적 버전 관리 대상
archunit = { module = "com.tngtech.archunit:archunit", version.ref = "archunit" }
mapstruct = { module = "org.mapstruct:mapstruct", version.ref = "mapstruct" }
mapstruct-processor = { module = "org.mapstruct:mapstruct-processor", version.ref = "mapstruct" }
mapstruct-spring-annotations = { module = "org.mapstruct.extensions.spring:mapstruct-spring-annotations", version.ref = "mapstruct-spring" }
mapstruct-spring-extensions = { module = "org.mapstruct.extensions.spring:mapstruct-spring-extensions", version.ref = "mapstruct-spring" }
lombok-mapstruct-binding = { module = "org.projectlombok:lombok-mapstruct-binding", version.ref = "lombok-mapstruct-binding" }

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
spring-dependency-management = { id = "io.spring.dependency-management", version.ref = "spring-dependency-management" }
hibernate = { id = "org.hibernate.orm", version.ref = "hibernate" }
graalvm-native = { id = "org.graalvm.buildtools.native", version.ref = "graalvm-native" }
````

### §3.8 `build.gradle.kts`

annotationProcessor 선언 **순서가 곧 기능**이다. 절대 재정렬하지 말 것.

````kotlin
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
description = "{{PROJECT_NAME}}"

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
    testImplementation(libs.archunit)
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
````

### §3.9 `.github/pmd/ruleset.xml`

````xml
<?xml version="1.0"?>
<ruleset name="Custom ruleset"
         xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0 https://pmd.sourceforge.io/ruleset_2_0_0.xsd">
    <description>
        메서드 길이를 30줄로 제한하는 custom ruleset
    </description>

    <!-- https://docs.pmd-code.org/latest/pmd_rules_java_design.html#ncsscount-->
    <rule ref="category/java/design.xml/NcssCount">
        <properties>
            <property name="methodReportLevel" value="30"/> <!-- NcssCount default 60 -->
            <property name="classReportLevel" value="1500"/> <!--default-->
        </properties>
    </rule>

</ruleset>
````

### §3.10 `src/main/resources/application.yaml`

````yaml
spring:
  application:
    name: {{PROJECT_NAME}}
````

### §3.11 `src/main/java/{{BASE_PACKAGE_PATH}}/{{APP_CLASS}}.java`

````java
package {{BASE_PACKAGE}};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class {{APP_CLASS}} {

    public static void main(String[] args) {
        SpringApplication.run({{APP_CLASS}}.class, args);
    }

}
````

### §3.12 `src/main/java/{{BASE_PACKAGE_PATH}}/controller/SampleController.java`

````java
package {{BASE_PACKAGE}}.controller;

import {{BASE_PACKAGE}}.service.SampleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    private final SampleService sampleService;

    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @GetMapping("/sample")
    public String sample() {
        return sampleService.getMessage();
    }
}
````

### §3.13 `src/main/java/{{BASE_PACKAGE_PATH}}/service/SampleService.java`

````java
package {{BASE_PACKAGE}}.service;

import {{BASE_PACKAGE}}.repository.SampleRepository;
import org.springframework.stereotype.Service;

@Service
public class SampleService {

    private final SampleRepository sampleRepository;

    public SampleService(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    public String getMessage() {
        return sampleRepository.findMessage();
    }
}
````

### §3.14 `src/main/java/{{BASE_PACKAGE_PATH}}/repository/SampleRepository.java`

````java
package {{BASE_PACKAGE}}.repository;

import org.springframework.stereotype.Repository;

@Repository
public class SampleRepository {

    public String findMessage() {
        return "Hello from Repository";
    }
}
````

### §3.15 `src/main/java/{{BASE_PACKAGE_PATH}}/domain/SampleEntity.java`

````java
package {{BASE_PACKAGE}}.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MapStruct 매핑 검증용 JPA 엔티티.
 * <p>
 * {@code @Setter} + no-arg 생성자를 쓰는 이유:
 * <ul>
 *     <li>{@code partialUpdate}의 {@code @MappingTarget}은 setter로 기존 인스턴스를 갱신한다.</li>
 *     <li>MapStruct가 setter로 값을 채우므로 lombok-mapstruct-binding 동작이 setter 방향으로도 증명된다.</li>
 * </ul>
 * 엔티티 identity/lazy-loading 함정을 피하려 {@code @Data}/{@code @EqualsAndHashCode}/{@code @Builder}는 쓰지 않는다.
 * {@code age}는 {@code partialUpdate}의 null 스킵을 증명하려 boxed {@link Integer}로 둔다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class SampleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer age;
}
````

### §3.16 `src/main/java/{{BASE_PACKAGE_PATH}}/dto/Sample.java`

````java
package {{BASE_PACKAGE}}.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * MapStruct 매핑의 소스 모델.
 * Lombok {@code @Getter}가 생성한 getter를 MapStruct가 읽어야 하므로,
 * 이 클래스가 정상 매핑되면 lombok-mapstruct-binding 동작이 증명된다.
 */
@Getter
@Builder
public class Sample {
    private final Long id;
    private final String name;
    private final int age;
}
````

### §3.17 `src/main/java/{{BASE_PACKAGE_PATH}}/dto/SampleDto.java`

````java
package {{BASE_PACKAGE}}.dto;

import lombok.Builder;
import lombok.Value;

/**
 * MapStruct 매핑의 타깃 DTO.
 * Lombok {@code @Builder}로 생성된 builder를 MapStruct가 사용해 객체를 만든다.
 * {@code fullName}은 소스의 {@code name}과 이름이 달라 {@code @Mapping} 동작 검증에 쓰인다.
 */
@Value
@Builder
public class SampleDto {
    Long id;
    String fullName;
    int age;
}
````

### §3.18 `src/main/java/{{BASE_PACKAGE_PATH}}/dto/SampleEntityDto.java`

````java
package {{BASE_PACKAGE}}.dto;

import lombok.Builder;
import lombok.Value;

/**
 * {@code SampleEntity}의 DTO.
 * <ul>
 *     <li>{@code fullName}은 엔티티의 {@code name}과 이름이 달라, {@code unmappedTargetPolicy = ERROR}
 *         정책 하에서 구체 매퍼가 {@code @Mapping} 오버라이드를 강제로 선언해야 함을 증명한다.</li>
 *     <li>{@code age}는 boxed {@link Integer}라 {@code partialUpdate}의 null 스킵이 동작한다.</li>
 * </ul>
 */
@Value
@Builder
public class SampleEntityDto {
    Long id;
    String fullName;
    Integer age;
}
````

### §3.19 `src/main/java/{{BASE_PACKAGE_PATH}}/mapper/CentralMapperConfig.java`

````java
package {{BASE_PACKAGE}}.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * 모든 매퍼가 공유하는 중앙 정책 설정.
 * 각 매퍼는 {@code @Mapper(config = CentralMapperConfig.class)}로 이 정책을 상속한다.
 * <ul>
 *     <li>{@code componentModel = SPRING} → 생성 구현체를 스프링 빈으로 등록</li>
 *     <li>{@code injectionStrategy = CONSTRUCTOR} → uses 매퍼 주입 시 생성자 주입 사용</li>
 *     <li>{@code unmappedTargetPolicy = ERROR} → 매핑 누락을 컴파일 타임 오류로 처리.
 *         의도적으로 무시할 필드는 {@code @Mapping(target = "x", ignore = true)}로 명시해야 한다.</li>
 * </ul>
 * mapstruct-spring-extensions 설정인 {@link MapstructSpringConfig}(@SpringMapperConfig)와는
 * 관심사가 달라 별도 파일로 분리한다.
 */
@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CentralMapperConfig {
}
````

### §3.20 `src/main/java/{{BASE_PACKAGE_PATH}}/mapper/MapstructSpringConfig.java`

````java
package {{BASE_PACKAGE}}.mapper;

import org.mapstruct.extensions.spring.SpringMapperConfig;

/**
 * mapstruct-spring-extensions 설정.
 * 이 클래스가 위치한 패키지에 {@code ConversionServiceAdapter}가 생성되므로,
 * 컴포넌트 스캔 범위 안에 두기 위해 mapper 패키지에 배치한다.
 * 이 설정이 없으면 adapter가 라이브러리 기본 패키지에 생성되어 빈으로 등록되지 않는다.
 */
@SpringMapperConfig
public interface MapstructSpringConfig {
}
````

### §3.21 `src/main/java/{{BASE_PACKAGE_PATH}}/mapper/DtoMapper.java`

````java
package {{BASE_PACKAGE}}.mapper;

import java.util.List;

/**
 * 읽기 전용(엔티티 → DTO) 방향만 노출하는 제네릭 매퍼.
 * 양방향이 필요 없는 매퍼는 이 인터페이스만 구현해 API 표면을 최소화한다.
 *
 * @param <E> 엔티티 타입
 * @param <D> DTO 타입
 */
public interface DtoMapper<E, D> {

    D toDto(E entity);

    List<D> toDto(List<E> entityList);
}
````

### §3.22 `src/main/java/{{BASE_PACKAGE_PATH}}/mapper/EntityMapper.java`

````java
package {{BASE_PACKAGE}}.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * 양방향(엔티티 ↔ DTO) 제네릭 매퍼. {@link DtoMapper}의 읽기 방향에 쓰기 방향과
 * 부분 갱신을 더한다.
 * <p>
 * <strong>주의</strong>: 구체 매퍼가 여기의 메서드를 {@code @Override}하면 이 인터페이스에 붙은
 * {@link Named}/{@link BeanMapping} 어노테이션은 <em>병합되지 않고 무시된다</em>.
 * 따라서 비대칭 필드 때문에 {@code partialUpdate}를 오버라이드할 때는 {@code @Named}와
 * {@code @BeanMapping}을 구체 매퍼에서 다시 선언해야 한다.
 *
 * @param <D> DTO 타입
 * @param <E> 엔티티 타입
 */
public interface EntityMapper<D, E> extends DtoMapper<E, D> {

    E toEntity(D dto);

    List<E> toEntity(List<D> dtoList);

    /**
     * DTO의 null이 아닌 필드만 기존 엔티티에 덮어써 부분 갱신한다.
     * null 스킵은 {@code NullValuePropertyMappingStrategy.IGNORE}가 담당하며,
     * boxed 타입 필드에만 동작한다(primitive는 null이 될 수 없어 항상 복사됨).
     */
    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(@MappingTarget E entity, D dto);
}
````

### §3.23 `src/main/java/{{BASE_PACKAGE_PATH}}/mapper/SampleMapper.java`

````java
package {{BASE_PACKAGE}}.mapper;

import {{BASE_PACKAGE}}.dto.Sample;
import {{BASE_PACKAGE}}.dto.SampleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;

/**
 * Sample → SampleDto 매퍼.
 * <ul>
 *     <li>{@code config = CentralMapperConfig.class} → 중앙 정책(componentModel=spring 등)을 상속해
 *         생성된 구현체가 스프링 빈으로 등록된다.</li>
 *     <li>{@link Converter} 구현 → mapstruct-spring-extensions가 {@code ConversionServiceAdapter}를 생성한다.</li>
 * </ul>
 */
@Mapper(config = CentralMapperConfig.class)
public interface SampleMapper extends Converter<Sample, SampleDto> {

    @Override
    @Mapping(source = "name", target = "fullName")
    SampleDto convert(Sample source);
}
````

### §3.24 `src/main/java/{{BASE_PACKAGE_PATH}}/mapper/SampleEntityMapper.java`

````java
package {{BASE_PACKAGE}}.mapper;

import {{BASE_PACKAGE}}.domain.SampleEntity;
import {{BASE_PACKAGE}}.dto.SampleEntityDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * {@link EntityMapper}를 구현한 구체 매퍼 데모.
 * <p>
 * {@code name} ↔ {@code fullName} 비대칭 때문에 {@link CentralMapperConfig}의
 * {@code unmappedTargetPolicy = ERROR} 정책 하에서는 {@code @Mapping} 오버라이드가 필수다
 * (누락 시 컴파일 오류). {@code partialUpdate} 오버라이드는 상위 인터페이스의 어노테이션이
 * 상속되지 않으므로 {@code @Named}/{@code @BeanMapping}까지 다시 선언한다.
 * <p>
 * 양방향 매퍼는 단방향 {@code Converter}와 부합하지 않으므로 {@code Converter}는 구현하지 않는다
 * (불필요한 {@code ConversionServiceAdapter} 메서드 생성 방지).
 */
@Mapper(config = CentralMapperConfig.class)
public interface SampleEntityMapper extends EntityMapper<SampleEntityDto, SampleEntity> {

    @Override
    @Mapping(source = "name", target = "fullName")
    SampleEntityDto toDto(SampleEntity entity);

    @Override
    @Mapping(source = "fullName", target = "name")
    SampleEntity toEntity(SampleEntityDto dto);

    @Override
    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "fullName", target = "name")
    void partialUpdate(@MappingTarget SampleEntity entity, SampleEntityDto dto);
}
````

### §3.25 `src/test/java/dev/haja/ArchitectureTest.java`

**주의**: 이 파일의 패키지는 `dev.haja`다. `{{BASE_PACKAGE}}`로 치환하지 않는다.

````java
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
````

### §3.26 `src/test/java/{{BASE_PACKAGE_PATH}}/{{APP_CLASS}}Tests.java`

````java
package {{BASE_PACKAGE}};

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class {{APP_CLASS}}Tests {

    @Test
    void contextLoads() {
    }

}
````

### §3.27 `src/test/java/{{BASE_PACKAGE_PATH}}/mapper/SampleMapperUnitTest.java`

````java
package {{BASE_PACKAGE}}.mapper;

import {{BASE_PACKAGE}}.dto.Sample;
import {{BASE_PACKAGE}}.dto.SampleDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 스프링 없이 생성된 매퍼 구현체를 직접 사용해 매핑을 검증한다.
 * 이 테스트가 통과하면 mapstruct + mapstruct-processor + lombok-mapstruct-binding이
 * 정상 동작함이 증명된다(Lombok 생성 getter/builder를 MapStruct가 사용).
 */
class SampleMapperUnitTest {

    private final SampleMapper mapper = new SampleMapperImpl();

    @Test
    @DisplayName("Lombok 소스 getter와 타깃 builder를 통해 필드가 매핑된다")
    void mapsFieldsThroughLombokGetterAndBuilder() {
        Sample sample = Sample.builder()
                .id(1L)
                .name("kai")
                .age(30)
                .build();

        SampleDto dto = mapper.convert(sample);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFullName()).isEqualTo("kai"); // @Mapping(name -> fullName) 증명
        assertThat(dto.getAge()).isEqualTo(30);
    }
}
````

### §3.28 `src/test/java/{{BASE_PACKAGE_PATH}}/mapper/SampleEntityMapperUnitTest.java`

````java
package {{BASE_PACKAGE}}.mapper;

import {{BASE_PACKAGE}}.domain.SampleEntity;
import {{BASE_PACKAGE}}.dto.SampleEntityDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 스프링 없이 생성된 {@link SampleEntityMapper} 구현체를 직접 사용해 제네릭 EntityMapper 패턴을 검증한다.
 * 제네릭 상속 메서드(toDto/toEntity/list/partialUpdate)가 타입 변수 해석 후 정상 생성되고,
 * {@code @MappingTarget} + {@code NullValuePropertyMappingStrategy.IGNORE}로 부분 갱신이 동작함을 증명한다.
 */
class SampleEntityMapperUnitTest {

    private final SampleEntityMapper mapper = new SampleEntityMapperImpl();

    private SampleEntity entity(Long id, String name, Integer age) {
        SampleEntity entity = new SampleEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setAge(age);
        return entity;
    }

    @Test
    @DisplayName("toDto: 엔티티의 name이 DTO의 fullName으로 매핑된다")
    void toDtoMapsRenamedField() {
        SampleEntityDto dto = mapper.toDto(entity(1L, "kai", 30));

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFullName()).isEqualTo("kai"); // @Mapping(name -> fullName)
        assertThat(dto.getAge()).isEqualTo(30);
    }

    @Test
    @DisplayName("toEntity: DTO의 fullName이 엔티티의 name으로 setter를 통해 매핑된다")
    void toEntityMapsRenamedFieldViaSetter() {
        SampleEntityDto dto = SampleEntityDto.builder().id(2L).fullName("ahn").age(40).build();

        SampleEntity entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getName()).isEqualTo("ahn"); // setter 방향 lombok-mapstruct-binding 증명
        assertThat(entity.getAge()).isEqualTo(40);
    }

    @Test
    @DisplayName("리스트 변형은 단건 메서드에 위임되어 각 요소를 매핑한다")
    void listMethodsMapEachElement() {
        List<SampleEntityDto> dtos = mapper.toDto(List.of(entity(1L, "a", 10), entity(2L, "b", 20)));

        assertThat(dtos).extracting(SampleEntityDto::getFullName).containsExactly("a", "b");

        List<SampleEntity> entities = mapper.toEntity(dtos);

        assertThat(entities).extracting(SampleEntity::getName).containsExactly("a", "b");
    }

    @Test
    @DisplayName("partialUpdate: DTO의 null 필드는 기존 엔티티 값을 유지한다")
    void partialUpdateIgnoresNullFields() {
        SampleEntity target = entity(1L, "kai", 30);
        SampleEntityDto patch = SampleEntityDto.builder().fullName("new-name").age(null).build();

        mapper.partialUpdate(target, patch);

        assertThat(target.getId()).isEqualTo(1L);          // patch.id == null → 유지
        assertThat(target.getName()).isEqualTo("new-name"); // non-null → 갱신
        assertThat(target.getAge()).isEqualTo(30);          // patch.age == null → 유지
    }

    @Test
    @DisplayName("partialUpdate: DTO의 non-null 필드는 기존 엔티티 값을 덮어쓴다")
    void partialUpdateOverwritesNonNullFields() {
        SampleEntity target = entity(1L, "kai", 30);
        SampleEntityDto patch = SampleEntityDto.builder().fullName("kai").age(40).build();

        mapper.partialUpdate(target, patch);

        assertThat(target.getAge()).isEqualTo(40);
    }
}
````

### §3.29 `src/test/java/{{BASE_PACKAGE_PATH}}/mapper/MapstructSpringIntegrationTest.java`

참고: `ConversionServiceAdapter`는 import가 없다. 컴파일 시 같은 패키지에 생성되는 클래스라 import가 불필요하다. import를 "고치지" 말 것.

````java
package {{BASE_PACKAGE}}.mapper;

import {{BASE_PACKAGE}}.domain.SampleEntity;
import {{BASE_PACKAGE}}.dto.Sample;
import {{BASE_PACKAGE}}.dto.SampleDto;
import {{BASE_PACKAGE}}.dto.SampleEntityDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 스프링 컨텍스트 안에서 MapStruct 스프링 연동을 검증한다.
 * <ul>
 *     <li>{@link SampleMapper} 빈 주입 → {@code componentModel = "spring"} 증명</li>
 *     <li>{@link ConversionServiceAdapter} 빈 주입 → mapstruct-spring-annotations + extensions 증명</li>
 *     <li>{@link ConversionService} 등록 → Converter 빈이 자동 등록됨을 증명</li>
 *     <li>{@link SampleEntityMapper} 빈 주입 → {@code @Mapper(config = CentralMapperConfig.class)}가
 *         componentModel=spring 정책을 상속해 빈으로 등록됨을 증명</li>
 * </ul>
 */
@SpringBootTest
class MapstructSpringIntegrationTest {

    @Autowired
    SampleMapper sampleMapper;

    @Autowired
    ConversionServiceAdapter conversionServiceAdapter;

    @Autowired
    ConversionService conversionService;

    @Autowired
    SampleEntityMapper sampleEntityMapper;

    private Sample sample() {
        return Sample.builder().id(1L).name("kai").age(30).build();
    }

    @Test
    @DisplayName("SampleMapper가 스프링 빈으로 등록되어 매핑한다")
    void mapperIsRegisteredAsSpringBean() {
        SampleDto dto = sampleMapper.convert(sample());

        assertThat(dto).isNotNull();
        assertThat(dto.getFullName()).isEqualTo("kai");
    }

    @Test
    @DisplayName("생성된 ConversionServiceAdapter 빈이 변환을 위임한다")
    void adapterBeanIsGeneratedAndConverts() {
        SampleDto dto = conversionServiceAdapter.mapSampleToSampleDto(sample());

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFullName()).isEqualTo("kai");
        assertThat(dto.getAge()).isEqualTo(30);
    }

    @Test
    @DisplayName("매퍼가 Converter로 ConversionService에 자동 등록된다")
    void converterIsRegisteredInConversionService() {
        assertThat(conversionService.canConvert(Sample.class, SampleDto.class)).isTrue();

        SampleDto dto = conversionService.convert(sample(), SampleDto.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getFullName()).isEqualTo("kai");
    }

    @Test
    @DisplayName("SampleEntityMapper가 config 상속으로 스프링 빈으로 등록되어 매핑한다")
    void entityMapperIsRegisteredAsSpringBean() {
        SampleEntity entity = new SampleEntity();
        entity.setId(1L);
        entity.setName("kai");
        entity.setAge(30);

        SampleEntityDto dto = sampleEntityMapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getFullName()).isEqualTo("kai");
        assertThat(dto.getAge()).isEqualTo(30);
    }
}
````

### §3.30 `CLAUDE.md`

새 프로젝트에서 Claude Code가 사용할 작업 지침. 원본 템플릿과 동일하며 패키지 경로만 치환된다.

````markdown
# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

Spring Boot 4.x 기반 Java 템플릿 프로젝트. Java 25 toolchain, Gradle Kotlin DSL, H2 인메모리 DB를 사용한다. 샘플 코드(Sample, SampleDto, SampleMapper 등)는 라이브러리 설정이 실제로 동작함을 증명하는 역할을 겸하므로, 삭제하기 전에 해당 검증 테스트와의 관계를 확인할 것.

## 자주 쓰는 명령어

```bash
./gradlew build                # 전체 빌드 (테스트 포함)
./gradlew test                 # 전체 테스트
./gradlew test --tests "{{BASE_PACKAGE}}.mapper.SampleMapperUnitTest"   # 단일 테스트 클래스
./gradlew test --tests "*SampleMapperUnitTest.mapsFieldsThroughLombokGetterAndBuilder"   # 단일 테스트 메서드
./gradlew bootRun              # 애플리케이션 실행
./gradlew nativeCompile        # GraalVM 네이티브 이미지 빌드
./gradlew pmdMain              # PMD 정적 분석 (main 소스만)
```

PMD(7.24.0) 정적 분석이 활성화되어 있다. 커스텀 룰셋 `.github/pmd/ruleset.xml`(메서드 NCSS 30줄 제한)만 적용되며 Gradle 기본 룰셋은 제외된다. `pmdMain`은 `check`/`build`에 포함되고, **테스트 코드는 검사 대상에서 제외**된다 (`build.gradle.kts`의 `pmd { sourceSets = listOf(...main...) }`). 버전은 `libs.versions.toml`의 `pmd`로 관리한다.

## 의존성 관리

- 모든 의존성 버전은 `gradle/libs.versions.toml` 버전 카탈로그로 관리한다. `build.gradle.kts`에 버전을 직접 쓰지 않는다.
- 카탈로그는 두 섹션으로 나뉜다: **Spring Boot BOM 관리 대상**(버전 미지정, module만 선언)과 **명시적 버전 관리 대상**(version.ref 사용). 의존성 추가 시 이 구분을 유지할 것.

## 아키텍처 제약 (ArchUnit)

`src/test/java/dev/haja/ArchitectureTest.java`가 레이어드 아키텍처를 강제한다:

- Controller → Service → Repository 단방향 접근만 허용
- `consideringAllDependencies()`를 사용하므로 레이어는 **패키지명 패턴**(`..controller..`, `..service..`, `..repository..`)으로 매칭된다. 패턴은 정확히 그 이름인 패키지 **세그먼트**에만 매칭된다 (예: `foo.service`·`foo.service.bar`는 매칭되지만 `conversionservice`처럼 다른 단어와 붙여 쓴 세그먼트는 매칭되지 않음 — ArchUnit 1.4.2 `PackageMatcher` 실측). 레이어 무관 코드에 `controller`·`service`·`repository`라는 이름의 패키지를 만들면 안 된다.
- 레이어 무관 코드는 `dto`, `mapper`처럼 별도 패키지에 둔다.
- Spring AOT/GraalVM이 생성하는 클래스(`build/classes/java/aot*`)는 검증에서 제외된다.
- ArchUnit은 `.class` 바이트코드를 런타임에 읽어 분석하므로 `.class` 파일이 없는 GraalVM 네이티브 이미지에서는 동작 불가하다. 따라서 `ArchitectureTest`는 일반 `@Test` + ArchUnit core API 방식으로 작성하고 `@DisabledInNativeImage`로 `nativeTest`에서 제외한다 (ArchUnit 전용 엔진의 `@ArchTest` 방식은 Jupiter 조건부 실행을 평가하지 않아 사용하지 않는다). 의존성은 `archunit-junit5-engine`이 아닌 `archunit`(core)을 쓴다.

## MapStruct + Lombok 연동

이 프로젝트의 핵심 설정이며 깨지기 쉬운 부분:

- **Annotation processor 순서가 중요**: `lombok` → `lombok-mapstruct-binding` → `mapstruct-processor`. `build.gradle.kts`의 main/test 양쪽 모두 이 순서를 유지해야 MapStruct가 Lombok 생성 getter/builder를 인식한다.
- 매퍼 정책은 `mapper/CentralMapperConfig.java`의 `@MapperConfig`로 중앙화한다(`componentModel = SPRING`, `injectionStrategy = CONSTRUCTOR`, **`unmappedTargetPolicy = ERROR`**). 각 매퍼는 `@Mapper(config = CentralMapperConfig.class)`로 상속받는다. ERROR 정책이므로 매핑 누락은 컴파일 오류이며, 의도적으로 무시할 필드는 `@Mapping(target = "x", ignore = true)`로 명시해야 한다. `@MapperConfig`(mapstruct-processor)와 `@SpringMapperConfig`(mapstruct-spring-extensions)는 관심사가 달라 별도 파일로 유지한다.
- `mapper/MapstructSpringConfig.java`의 `@SpringMapperConfig`가 adapter 생성 위치를 결정한다. 이 클래스가 mapper 패키지(컴포넌트 스캔 범위 안)에 있어야 adapter가 빈으로 등록된다 — 삭제/이동 금지.
- 제네릭 매퍼 계층: `mapper/DtoMapper<E, D>`(읽기 전용: toDto)와 이를 상속하는 `mapper/EntityMapper<D, E>`(양방향: toEntity + `@Named("partialUpdate")` 부분 갱신). 구체 매퍼는 `SampleEntityMapper`(엔티티 ↔ DTO 양방향, non-Converter)와 `SampleMapper`(Converter 구현, mapstruct-spring-extensions adapter 증명용)로 나뉜다.
- **오버라이드 시 어노테이션 비상속 주의**: 비대칭 필드(예: `name` ↔ `fullName`) 때문에 제네릭 메서드를 `@Override`하면 상위 인터페이스의 `@Named`/`@BeanMapping`이 병합되지 않는다. `partialUpdate` 오버라이드에서 `@Named`/`@BeanMapping(...IGNORE)`/`@Mapping`을 모두 다시 선언해야 한다. `partialUpdate`의 null 스킵은 boxed 타입 필드에만 동작하므로 `SampleEntityDto.age`는 `Integer`를 쓴다.
- Lombok binding 검증을 위해 매핑 대상 모델은 record가 아닌 Lombok 클래스를 쓴다: 불변 모델은 `@Getter`/`@Builder`(getter/builder 방향), JPA 엔티티 `domain/SampleEntity`는 `@Getter`/`@Setter`/`@NoArgsConstructor`(setter 방향 binding + `@MappingTarget` 부분 갱신 지원). 엔티티는 identity 함정 회피를 위해 `@Data`/`@EqualsAndHashCode`/`@Builder`를 쓰지 않는다.
- 연동 검증 테스트: `SampleMapperUnitTest`·`SampleEntityMapperUnitTest`(스프링 없이 processor/binding·partialUpdate 검증), `MapstructSpringIntegrationTest`(빈 등록·ConversionService 자동 등록·config 상속 검증).

## 기타

- Hibernate ORM Gradle 플러그인(`org.hibernate.orm`)이 적용되어 있으나 `hibernate { enhancement {} }` 설정 블록이 없어 bytecode enhancement는 실제로 수행되지 않는다 (빌드된 엔티티 클래스에 `$$_hibernate_` 멤버 없음). enhancement가 필요해지면 `build.gradle.kts`에 해당 블록을 추가할 것.
- `plans/` 디렉터리에 과거 작업 계획 문서가 있다. 설정 변경의 배경이 궁금할 때 참고.
- PR 작성 시 저장소 루트의 `PULL_REQUEST_TEMPLATE.md` 양식을 따른다.
````

### §3.31 `PULL_REQUEST_TEMPLATE.md`

저장소 루트에 둔다 (GitHub이 루트의 이 파일명을 인식한다).

````markdown
# Pull Request Template

## 📋 JIRA 티켓 / Issue
**JIRA:** [SMART-XXX](링크 첨부)
**(선택적) 깃허브 이슈 Issues:** #issue_number

---

## 🏷️ PR 타입 / Change Type
<!-- 해당하는 항목에 체크 -->
- [ ] ✨ **Feature** - 새로운 기능 추가
- [ ] 🐛 **Bug Fix** - 버그 수정
- [ ] ♻️ **Refactor** - 코드 리팩토링
- [ ] 🎨 **Style** - 코드 포맷팅, 세미콜론 누락 등
- [ ] 📝 **Docs** - 문서 수정
- [ ] ⚡ **Performance** - 성능 개선
- [ ] ✅ **Test** - 테스트 코드 추가/수정
- [ ] 🔧 **Chore** - 빌드 과정, 보조 도구 변경
- [ ] 🔒 **Security** - 보안 관련 변경

## 🎯 영향 범위 / Scope
- [ ] 🎨 **Frontend** - React/TypeScript
- [ ] 🛠️ **Backend** - Spring Boot/Kotlin
- [ ] 📊 **Database** - Schema/Migration
- [ ] 🔧 **Config** - 설정 파일 변경
- [ ] 📖 **Documentation**

---

## 📋 작업 내용 / Summary
### 간단 요약 / Brief Description
<!-- 이 PR에서 수행한 작업을 간단하게 설명해 주세요 -->

### 상세 변경사항 / Detailed Changes
<!-- 주요 변경사항을 구체적으로 작성해 주세요 -->
- 
-
-

### 동기 및 배경 / Motivation
<!-- 왜 이 변경이 필요한지 설명해 주세요 -->

---

## 🧪 테스트 / Testing
### 테스트 방법 / How to Test
<!-- 이 변경사항을 어떻게 테스트할 수 있는지 설명해 주세요 -->
1.
2.
3.

### 테스트 결과 / Test Results
- [ ] Unit Test 통과
- [ ] Integration Test 통과
- [ ] Manual Test 완료

---

## ✅ 체크리스트 / Checklist

### 공통 / Common
- [ ] 🔍 **코드 리뷰가 가능한 상태입니다**
- [ ] 🎫 **JIRA 티켓이 연결되어 있습니다**
- [ ] 📝 **커밋 메시지가 명확합니다**
- [ ] 🔀 **Conflict가 해결되었습니다**

### Frontend (해당하는 경우만)
- [ ] ✅ **TypeScript 타입 체크 통과** (`yarn build`)
- [ ] 🧪 **Vitest 테스트 통과** (`yarn test --run`)
- [ ] 🎨 **ESLint 통과** (`yarn lint`)
- [ ] 📱 **반응형 디자인 확인 완료**
- [ ] ♿ **접근성 고려사항 검토 완료**

### Backend (해당하는 경우만)
- [ ] 🏗️ **Gradle 빌드 성공** (`./gradlew build`)
- [ ] 🧪 **단위 테스트 통과** (`./gradlew test`)
- [ ] 🔌 **API 문서 업데이트** (필요시)
- [ ] 🛡️ **보안 검토 완료** (인증/권한)
- [ ] 📊 **DB 스키마 변경 검토** (필요시)

---

## 📸 스크린샷 / Screenshots
<!-- UI 변경이 있는 경우 Before/After 스크린샷을 첨부해 주세요 -->

### Before
<!-- 변경 전 스크린샷 -->

### After
<!-- 변경 후 스크린샷 -->

---

## 🔄 API 변경사항 / API Changes
<!-- API 변경이 있는 경우 작성해 주세요 -->

### 신규 API
```
GET/POST/PUT/DELETE /api/v1/endpoint
```

### 변경된 API
```
기존: GET /api/v1/old-endpoint
신규: GET /api/v1/new-endpoint
```

### Request/Response 예시
```json
{
  "example": "data"
}
```

---

## 🚀 배포 고려사항 / Deployment Notes

### 환경변수 변경
- [ ] **새로운 환경변수 추가 없음**
- [ ] **환경변수 변경 사항 있음**
    - `NEW_ENV_VAR=value`

### 데이터베이스
- [ ] **DB 마이그레이션 필요 없음**
- [ ] **DB 마이그레이션 필요함**
    - 마이그레이션 스크립트:

### 의존성 변경
- [ ] **새로운 의존성 추가 없음**
- [ ] **새로운 의존성 추가됨**
    - Frontend:
    - Backend:

### 롤백 계획
<!-- 문제 발생 시 롤백 방법을 설명해 주세요 -->
- 

---

## 👥 리뷰어 / Reviewers
- [ ] **Frontend 리뷰 필요**: @frontend-reviewer
- [ ] **Backend 리뷰 필요**: @backend-reviewer
- [ ] **DevOps 리뷰 필요**: @devops-reviewer
- [ ] **PM/기획자 확인 필요**: @product-manager

---

## 📝 추가 노트 / Additional Notes
<!-- 리뷰어가 알아야 할 기타 정보를 작성해 주세요 -->

### 참고 문서
- 

### 관련 PR
- 

### 후속 작업
- 

---

> 💡 **리뷰어를 위한 팁**
> - 중요한 변경사항이나 복잡한 로직에는 코드 내 주석으로 설명을 추가했습니다
> - 특별히 검토가 필요한 부분이 있다면 PR 코멘트로 표시했습니다
````

## §4. 툴체인 설치 및 Gradle Wrapper 생성

Gradle Wrapper의 `gradle-wrapper.jar`는 바이너리라 이 문서에 담을 수 없다. 아래처럼 생성한다.

```bash
# 1) Java 툴체인 설치 (mise.toml 기반)
mise trust && mise install
mise x -- java -version        # 25 버전 확인

# 2) Gradle 9.6.1로 wrapper 생성 (일회성 실행 — 시스템에 gradle을 설치하지 않음)
#    build.gradle.kts 평가를 위해 플러그인 다운로드가 발생하므로 수 분 걸릴 수 있다
mise x gradle@9.6.1 -- gradle wrapper --gradle-version 9.6.1 --distribution-type bin

# 3) 생성 확인
ls gradlew gradlew.bat gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.properties
```

생성 후 `gradle/wrapper/gradle-wrapper.properties`를 아래 내용으로 **덮어쓴다** (원본 템플릿과 동일하게 고정):

````properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-9.6.1-bin.zip
networkTimeout=10000
retries=0
retryBackOffMs=500
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
````

- 대안(mise로 gradle을 받을 수 없는 환경): 시스템에 설치된 Gradle(8.14 이상 아무 버전)로 `gradle wrapper --gradle-version 9.6.1 --distribution-type bin`을 실행해도 된다. 핵심은 wrapper가 9.6.1을 가리키는 것이다.
- `chmod +x gradlew`가 되어 있는지 확인한다 (wrapper 태스크가 기본으로 실행 권한을 부여한다).

## §5. 빌드 검증

```bash
mise x -- ./gradlew build
```

`mise x --`는 mise가 셸에 활성화되지 않은 환경에서도 `mise.toml`의 Java로 실행되게 한다.
(mise가 셸에 활성화되어 있으면 `./gradlew build`만으로 충분하다.)

### 기대 결과

1. `BUILD SUCCESSFUL` — 컴파일, PMD(`pmdMain`, 위반 0건), 테스트 모두 통과.
2. 테스트 **12개 전부 통과** (`passed`):

| 테스트 클래스 | 개수 | 증명하는 것 |
|---|---|---|
| `SampleMapperUnitTest` | 1 | MapStruct가 Lombok getter/builder 인식 (processor 순서·binding) |
| `SampleEntityMapperUnitTest` | 5 | 제네릭 EntityMapper 상속, setter 방향 binding, partialUpdate null 스킵 |
| `MapstructSpringIntegrationTest` | 4 | 매퍼 빈 등록, ConversionServiceAdapter 생성·등록, config 상속 |
| `{{APP_CLASS}}Tests` | 1 | 스프링 컨텍스트 로드 (JPA + H2 포함) |
| `ArchitectureTest` | 1 | Controller → Service → Repository 레이어 규칙 |

3. annotation processing 산출물 확인:

```bash
ls build/generated/sources/annotationProcessor/java/main/{{BASE_PACKAGE_PATH}}/mapper/
# 기대: ConversionServiceAdapter.java, SampleEntityMapperImpl.java, SampleMapperImpl.java
```

4. (선택) 실행 스모크 테스트:

```bash
mise x -- ./gradlew bootRun   # 백그라운드로 띄운 뒤
curl -s localhost:8080/sample   # 기대 응답: Hello from Repository
# 확인 후 bootRun 프로세스 종료
```

`nativeCompile`(GraalVM 네이티브 이미지)은 시간이 오래 걸리므로 부트스트랩 단계에서는 실행하지 않는다.

## §6. 실패 시 진단표

| 증상 | 원인 | 조치 |
|---|---|---|
| 컴파일 오류 `Unmapped target property: "..."` | `unmappedTargetPolicy = ERROR` 정책 하에서 `@Mapping` 오버라이드 누락 — 파일 내용이 문서와 다름 | `SampleMapper`, `SampleEntityMapper`를 §3.23·§3.24 원문과 대조 |
| 컴파일 오류 `No property named "name" exists` 또는 builder 인식 실패 | annotationProcessor 순서가 깨짐 (lombok-mapstruct-binding이 lombok보다 앞이거나 누락) | `build.gradle.kts`의 annotationProcessor·testAnnotationProcessor 순서를 §3.8과 대조 |
| 테스트 컴파일 오류 `cannot find symbol: SampleMapperImpl` | testAnnotationProcessor 3종 누락 → 테스트 컴파일 시 매퍼 구현체 미생성 | §3.8의 testAnnotationProcessor 3줄 확인 |
| 테스트 컴파일 오류 `cannot find symbol: ConversionServiceAdapter` | `MapstructSpringConfig` 누락 또는 mapper 패키지 밖에 있음 / main annotationProcessor에서 `mapstruct.spring.extensions` 누락 | §3.20 파일 위치·내용, §3.8 annotationProcessor 확인 |
| `NoSuchBeanDefinitionException: ConversionServiceAdapter` | adapter가 컴포넌트 스캔 범위 밖 패키지에 생성됨 | `MapstructSpringConfig`가 `{{BASE_PACKAGE}}.mapper`에 있는지 확인 |
| `ArchitectureTest.layerRule` 실패 | `controller`/`service`/`repository`라는 패키지 세그먼트가 레이어 규칙 위반 위치에 생김 | 패키지 구조를 §3 트리와 대조. PROJECT_NAME 자체가 문제라면 §1.2 재검토 |
| `No matching toolchains found for ... Java 25` | mise Java 미설치 또는 셸에서 미인식 | `mise install` 후 `mise x -- ./gradlew build`로 실행 |
| PMD 위반 발생 | 파일을 문서 원문과 다르게 생성함 (원문은 위반 0건) | 해당 파일을 원문과 대조 |
| `gradle wrapper` 실행 중 플러그인 다운로드 실패 | 네트워크/프록시 문제 | 네트워크 확인. 프록시 환경이면 `gradle.properties`의 proxy 주석 참고 |

## §7. git 초기화 및 최초 커밋

```bash
git init -b main                                # .git이 이미 있으면 생략
git config commit.template .gitmessage.txt      # 커밋 메시지 템플릿 연결 (저장소 로컬 설정)
git add -A
git status --short                              # mise.toml이 목록에 없어야 정상 (.gitignore 동작 확인)
git commit -m "chore: {{PROJECT_NAME}} 프로젝트 초기 설정"
```

- 커밋 전 반드시 §5의 빌드 검증이 통과해야 한다.
- `mise.toml`·`build/`·`.gradle/`이 스테이징되지 않았는지 확인한다 (커밋 대상은 §3 트리에서 mise.toml을 뺀 파일들 + §4의 wrapper 4개 파일이다).
- 원격 저장소 생성/푸시는 사용자가 요청한 경우에만 수행한다 (예: `gh repo create`).

## 완료 기준 체크리스트

- [ ] §1.2 PROJECT_NAME 유효성 검증 통과
- [ ] §3의 파일 31개가 트리 구조·내용 그대로 생성됨 (플레이스홀더만 치환)
- [ ] `ArchitectureTest.java`가 `src/test/java/dev/haja/`에 위치 (BASE_PACKAGE 아님)
- [ ] §4 wrapper 생성 및 `gradle-wrapper.properties` 덮어쓰기 완료
- [ ] `mise x -- ./gradlew build` → `BUILD SUCCESSFUL`, 테스트 12/12 통과, PMD 위반 0건
- [ ] `build/generated/.../mapper/`에 `SampleMapperImpl`·`SampleEntityMapperImpl`·`ConversionServiceAdapter` 생성 확인
- [ ] git 최초 커밋 완료, `commit.template` 설정됨, `mise.toml` 미추적
- [ ] 사용자에게 결과 요약 보고: 생성 파일 수, 테스트 결과, (수행했다면) 스모크 테스트 응답
