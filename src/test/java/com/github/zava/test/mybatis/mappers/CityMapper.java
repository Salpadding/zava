package com.github.zava.test.mybatis.mappers;

import com.github.zava.test.mybatis.City;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mapper
public interface CityMapper {
    List<City> findAll(Map<String, Object> query);

    default List<City> findAll() {
        return findAll(Collections.emptyMap());
    }

    void create(City city);
}
