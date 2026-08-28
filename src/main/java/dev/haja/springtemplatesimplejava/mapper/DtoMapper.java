package dev.haja.springtemplatesimplejava.mapper;

import java.util.List;

/**
 * 읽기 전용(엔티티 → DTO) 방향만 노출하는 제네릭 매퍼.
 * 양방향이 필요 없는 매퍼는 이 인터페이스만 구현해 API 표면을 최소화한다.
 *
 * @param <E> 엔티티 타입
 * @param <D> DTO 타입
 */
public interface DtoMapper<E, D> {

    D toDto(E entity);

    List<D> toDto(List<E> entityList);
}
