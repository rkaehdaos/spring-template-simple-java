# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

Spring Boot 4.x 기반 Java 템플릿 프로젝트. Java 25 toolchain, Gradle Kotlin DSL, H2 인메모리 DB를 사용한다. 샘플 코드(Sample, SampleDto, SampleMapper 등)는 라이브러리 설정이 실제로 동작함을 증명하는 역할을 겸하므로, 삭제하기 전에 해당 검증 테스트와의 관계를 확인할 것.

## 자주 쓰는 명령어

```bash
./gradlew build                # 전체 빌드 (테스트 포함)
./gradlew test                 # 전체 테스트
./gradlew test --tests "dev.haja.springtemplatesimplejava.mapper.SampleMapperUnitTest"   # 단일 테스트 클래스
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
- `consideringAllDependencies()`를 사용하므로 레이어는 **패키지명 패턴**(`..controller..`, `..service..`, `..repository..`)으로 매칭된다. 레이어에 속하지 않는 새 패키지/클래스 이름에 `controller`·`service`·`repository` 문자열을 넣으면 안 된다 (예: `conversionservice` 패키지는 `..service..`에 매칭되어 규칙 위반이 됨).
- 레이어 무관 코드는 `dto`, `mapper`처럼 별도 패키지에 둔다.
- Spring AOT/GraalVM이 생성하는 클래스(`build/classes/java/aot*`)는 검증에서 제외된다.

## MapStruct + Lombok 연동

이 프로젝트의 핵심 설정이며 깨지기 쉬운 부분:

- **Annotation processor 순서가 중요**: `lombok` → `lombok-mapstruct-binding` → `mapstruct-processor`. `build.gradle.kts`의 main/test 양쪽 모두 이 순서를 유지해야 MapStruct가 Lombok 생성 getter/builder를 인식한다.
- 매퍼는 `@Mapper(componentModel = "spring")`으로 선언하고 Spring `Converter<S, T>`를 구현한다. mapstruct-spring-extensions가 이를 보고 `ConversionServiceAdapter`를 생성한다.
- `mapper/MapstructSpringConfig.java`의 `@SpringMapperConfig`가 adapter 생성 위치를 결정한다. 이 클래스가 mapper 패키지(컴포넌트 스캔 범위 안)에 있어야 adapter가 빈으로 등록된다 — 삭제/이동 금지.
- Lombok binding 검증을 위해 매핑 대상 모델은 record가 아닌 Lombok `@Getter`/`@Builder` 클래스를 사용한다 (record는 accessor를 컴파일러가 직접 생성하므로 binding 동작이 증명되지 않음).
- 연동 검증 테스트: `SampleMapperUnitTest`(스프링 없이 processor/binding 검증), `MapstructSpringIntegrationTest`(빈 등록·ConversionService 자동 등록 검증).

## 기타

- Hibernate bytecode enhancement 플러그인이 활성화되어 있다 (`hibernate { enhancement {} }`).
- `plans/` 디렉터리에 과거 작업 계획 문서가 있다. 설정 변경의 배경이 궁금할 때 참고.
- PR 작성 시 저장소 루트의 `PULL_REQUEST_TEMPLATE.md` 양식을 따른다.
