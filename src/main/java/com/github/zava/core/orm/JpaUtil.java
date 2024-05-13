package com.github.zava.core.orm;

import com.github.zava.core.spring.SpringUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class JpaUtil {
    public static <T> List<T> query(String query, Class<T> clazz) {
        EntityManager em = SpringUtil.getBean(EntityManager.class);
        return em.createNativeQuery(query, clazz).getResultList();
    }
}
