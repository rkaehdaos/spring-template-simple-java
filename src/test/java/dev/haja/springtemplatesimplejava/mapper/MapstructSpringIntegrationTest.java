package dev.haja.springtemplatesimplejava.mapper;

import dev.haja.springtemplatesimplejava.domain.SampleEntity;
import dev.haja.springtemplatesimplejava.dto.Sample;
import dev.haja.springtemplatesimplejava.dto.SampleDto;
import dev.haja.springtemplatesimplejava.dto.SampleEntityDto;
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
