package ru.practicum.hit;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StatsMapper {

    static ViewStatsDto objToViewStats(Object[] obj) {
        String app = obj[0].toString();
        String uri = obj[1].toString();
        Long hits = ((Number) obj[2]).longValue();

        return new ViewStatsDto(app, uri, hits);
    }
}
