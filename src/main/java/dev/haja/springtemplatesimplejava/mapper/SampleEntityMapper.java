package dev.haja.springtemplatesimplejava.mapper;

import dev.haja.springtemplatesimplejava.domain.SampleEntity;
import dev.haja.springtemplatesimplejava.dto.SampleEntityDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * {@link EntityMapper}를 구현한 구체 매퍼 데모.
 * <p>
 * {@code name} ↔ {@code fullName} 비대칭 때문에 {@link CentralMapperConfig}의
 * {@code unmappedTargetPolicy = ERROR} 정책 하에서는 {@code @Mapping} 오버라이드가 필수다
 * (누락 시 컴파일 오류). {@code partialUpdate} 오버라이드는 상위 인터페이스의 어노테이션이
 * 상속되지 않으므로 {@code @Named}와 {@code @Mapping}(id ignore 포함)까지 다시 선언한다.
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
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "fullName", target = "name")
    void partialUpdate(@MappingTarget SampleEntity entity, SampleEntityDto dto);
}
