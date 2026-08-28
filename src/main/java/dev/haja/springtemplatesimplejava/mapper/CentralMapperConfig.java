package dev.haja.springtemplatesimplejava.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * 모든 매퍼가 공유하는 중앙 정책 설정.
 * 각 매퍼는 {@code @Mapper(config = CentralMapperConfig.class)}로 이 정책을 상속한다.
 * <ul>
 *     <li>{@code componentModel = SPRING} → 생성 구현체를 스프링 빈으로 등록</li>
 *     <li>{@code injectionStrategy = CONSTRUCTOR} → uses 매퍼 주입 시 생성자 주입 사용</li>
 *     <li>{@code unmappedTargetPolicy = ERROR} → 매핑 누락을 컴파일 타임 오류로 처리.
 *         의도적으로 무시할 필드는 {@code @Mapping(target = "x", ignore = true)}로 명시해야 한다.</li>
 *     <li>{@code nullValuePropertyMappingStrategy = IGNORE} → {@code @MappingTarget} 갱신 메서드
 *         (예: {@code partialUpdate})에서만 적용되며, DTO의 null 필드가 기존 엔티티 값을 지우지 않는다.
 *         중앙 선언이므로 구체 매퍼가 {@code @BeanMapping}을 재선언할 필요가 없다.</li>
 * </ul>
 * mapstruct-spring-extensions 설정인 {@link MapstructSpringConfig}(@SpringMapperConfig)와는
 * 관심사가 달라 별도 파일로 분리한다.
 */
@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CentralMapperConfig {
}
