package dev.haja.springtemplatesimplejava.dto;

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
