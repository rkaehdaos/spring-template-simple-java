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
- `consideringAllDependencies()`를 사용하므로 레이어는 **패키지명 패턴**(`..controller..`, `..service..`, `..repository..`)으로 매칭된다. 패턴은 정확히 그 이름인 패키지 **세그먼트**에만 매칭된다 (예: `foo.service`·`foo.service.bar`는 매칭되지만 `conversionservice`처럼 다른 단어와 붙여 쓴 세그먼트는 매칭되지 않음 — ArchUnit 1.4.2 `PackageMatcher` 실측). 레이어 무관 코드에 `controller`·`service`·`repository`라는 이름의 패키지를 만들면 안 된다.
- 레이어 무관 코드는 `dto`, `mapper`처럼 별도 패키지에 둔다.
- Spring AOT/GraalVM이 생성하는 클래스(`build/classes/java/aot*`)는 검증에서 제외된다.
- ArchUnit은 `.class` 바이트코드를 런타임에 읽어 분석하므로 `.class` 파일이 없는 GraalVM 네이티브 이미지에서는 동작 불가하다. 따라서 `ArchitectureTest`는 일반 `@Test` + ArchUnit core API 방식으로 작성하고 `@DisabledInNativeImage`로 `nativeTest`에서 제외한다 (ArchUnit 전용 엔진의 `@ArchTest` 방식은 Jupiter 조건부 실행을 평가하지 않아 사용하지 않는다). 의존성은 `archunit-junit5-engine`이 아닌 `archunit`(core)을 쓴다.

## MapStruct + Lombok 연동

이 프로젝트의 핵심 설정이며 깨지기 쉬운 부분:

- **Annotation processor 순서가 중요**: `lombok` → `lombok-mapstruct-binding` → `mapstruct-processor`. `build.gradle.kts`의 main/test 양쪽 모두 이 순서를 유지해야 MapStruct가 Lombok 생성 getter/builder를 인식한다.
- 매퍼 정책은 `mapper/CentralMapperConfig.java`의 `@MapperConfig`로 중앙화한다(`componentModel = SPRING`, `injectionStrategy = CONSTRUCTOR`, **`unmappedTargetPolicy = ERROR`**, `nullValuePropertyMappingStrategy = IGNORE`). 각 매퍼는 `@Mapper(config = CentralMapperConfig.class)`로 상속받는다. ERROR 정책이므로 매핑 누락은 컴파일 오류이며, 의도적으로 무시할 필드는 `@Mapping(target = "x", ignore = true)`로 명시해야 한다. `nullValuePropertyMappingStrategy = IGNORE`는 `@MappingTarget` 갱신 메서드(예: `partialUpdate`)에만 적용되어, DTO의 null 필드가 기존 값을 지우지 않는다. `@MapperConfig`(mapstruct-processor)와 `@SpringMapperConfig`(mapstruct-spring-extensions)는 관심사가 달라 별도 파일로 유지한다.
- `mapper/MapstructSpringConfig.java`의 `@SpringMapperConfig`가 adapter 생성 위치를 결정한다. 이 클래스가 mapper 패키지(컴포넌트 스캔 범위 안)에 있어야 adapter가 빈으로 등록된다 — 삭제/이동 금지.
- 제네릭 매퍼 계층: `mapper/DtoMapper<E, D>`(읽기 전용: toDto)와 이를 상속하는 `mapper/EntityMapper<D, E>`(양방향: toEntity + `@Named("partialUpdate")` 부분 갱신, `id`는 항상 `@Mapping(target = "id", ignore = true)`로 무시). 구체 매퍼는 `SampleEntityMapper`(엔티티 ↔ DTO 양방향, non-Converter)와 `SampleMapper`(Converter 구현, mapstruct-spring-extensions adapter 증명용)로 나뉜다.
- **오버라이드 시 어노테이션 비상속 주의**: 비대칭 필드(예: `name` ↔ `fullName`) 때문에 제네릭 메서드를 `@Override`하면 상위 인터페이스의 `@Named`/`@Mapping`이 병합되지 않는다. `partialUpdate` 오버라이드에서 `@Named`와 **`@Mapping(target = "id", ignore = true)`를 반드시** 다시 선언해야 한다(누락 시 관리 중인 엔티티의 기본키가 patch DTO의 id로 덮어써진다). null 스킵 전략은 `CentralMapperConfig`에 중앙 선언되어 있어 `@BeanMapping` 재선언은 불필요하다. `partialUpdate`의 null 스킵은 boxed 타입 필드에만 동작하므로 `SampleEntityDto.age`는 `Integer`를 쓴다.
- Lombok binding 검증을 위해 매핑 대상 모델은 record가 아닌 Lombok 클래스를 쓴다: 불변 모델은 `@Getter`/`@Builder`(getter/builder 방향), JPA 엔티티 `domain/SampleEntity`는 `@Getter`/`@Setter`/`@NoArgsConstructor`(setter 방향 binding + `@MappingTarget` 부분 갱신 지원). 엔티티는 identity 함정 회피를 위해 `@Data`/`@EqualsAndHashCode`/`@Builder`를 쓰지 않는다.
- 연동 검증 테스트: `SampleMapperUnitTest`·`SampleEntityMapperUnitTest`(스프링 없이 processor/binding·partialUpdate 검증), `MapstructSpringIntegrationTest`(빈 등록·ConversionService 자동 등록·config 상속 검증).

## 기타

- Hibernate ORM Gradle 플러그인(`org.hibernate.orm`)이 적용되어 있으나 `hibernate { enhancement {} }` 설정 블록이 없어 bytecode enhancement는 실제로 수행되지 않는다 (빌드된 엔티티 클래스에 `$$_hibernate_` 멤버 없음). enhancement가 필요해지면 `build.gradle.kts`에 해당 블록을 추가할 것.
- `plans/` 디렉터리에 과거 작업 계획 문서가 있다. 설정 변경의 배경이 궁금할 때 참고.
- PR 작성 시 `.github/PULL_REQUEST_TEMPLATE.md` 양식을 따른다.
