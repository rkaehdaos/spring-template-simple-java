package dev.haja.springtemplatesimplejava.mapper;

import dev.haja.springtemplatesimplejava.dto.Sample;
import dev.haja.springtemplatesimplejava.dto.SampleDto;
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
