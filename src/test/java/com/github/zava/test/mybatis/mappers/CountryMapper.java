package com.github.zava.test.mybatis.mappers;

import com.github.zava.test.mybatis.Country;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CountryMapper {
    List<Country> findAll(Map<String, Object> params);

    void create(Country country);
}
