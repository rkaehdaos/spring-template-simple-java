package dev.haja.springtemplatesimplejava.mapper;

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
