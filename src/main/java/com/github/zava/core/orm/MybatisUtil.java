package com.github.zava.core.orm;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;

public class MybatisUtil {
    // 推荐使用 spring 的 DataSourceBuilder
    public static Configuration createConfig(String env, DataSource dataSource) {
        TransactionFactory transactionFactory =
            new JdbcTransactionFactory();
        Environment environment =
            new Environment(env, transactionFactory, dataSource);
        return new Configuration(environment);
    }

    public static SqlSessionFactory createFactory(String env, DataSource dataSource) {
        return new SqlSessionFactoryBuilder().build(createConfig(env, dataSource));
    }
}
