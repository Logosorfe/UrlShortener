package com.telran.org.urlshortener.mapper;

public interface Converter<Entity, EntityCreateDTO, DTO> {
    DTO entityToDto(Entity entity);

    Entity dtoToEntity(EntityCreateDTO dto);
}
