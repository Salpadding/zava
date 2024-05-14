package com.github.zava.core.orm;

import lombok.SneakyThrows;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.w3c.dom.Document;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

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

    public static SqlSessionFactory createFactory(Class<?> basePackage, String mapperDir, DataSource dataSource) {
        SqlSessionFactory factory = createFactory("app", dataSource);
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(createXmlLoader(loader, mapperDir));
        factory.getConfiguration().addMappers(basePackage.getPackageName());
        Thread.currentThread().setContextClassLoader(loader);
        return factory;
    }

    // 魔改 class loader, 让 mybatis 正确的发现 xml
    @SneakyThrows
    public static ClassLoader createXmlLoader(ClassLoader delegate, String prefix) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(ClassLoaderProxyHandler.ClassLoaderImpl.class);
        enhancer.setCallback(new ClassLoaderProxyHandler(prefix, delegate));
        return (ClassLoader) enhancer.create();
    }

    @SneakyThrows
    public static Document readXml(InputStream ins) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(new XMLMapperEntityResolver());
        return builder.parse(ins);
    }
}
