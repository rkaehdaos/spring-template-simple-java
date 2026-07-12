package dev.haja.springtemplatesimplejava.mapper;

import dev.haja.springtemplatesimplejava.dto.Sample;
import dev.haja.springtemplatesimplejava.dto.SampleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;

/**
 * Sample → SampleDto 매퍼.
 * <ul>
 *     <li>{@code componentModel = "spring"} → 생성된 구현체가 스프링 빈으로 등록된다.</li>
 *     <li>{@link Converter} 구현 → mapstruct-spring-extensions가 {@code ConversionServiceAdapter}를 생성한다.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface SampleMapper extends Converter<Sample, SampleDto> {

    @Override
    @Mapping(source = "name", target = "fullName")
    SampleDto convert(Sample source);
}
