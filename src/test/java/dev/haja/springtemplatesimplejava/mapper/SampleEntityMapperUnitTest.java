package dev.haja.springtemplatesimplejava.mapper;

import dev.haja.springtemplatesimplejava.domain.SampleEntity;
import dev.haja.springtemplatesimplejava.dto.SampleEntityDto;
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
