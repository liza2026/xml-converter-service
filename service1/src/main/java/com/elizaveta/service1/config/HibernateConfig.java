package com.elizaveta.service1.config;

import com.elizaveta.service1.entity.ConversionRequest;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class HibernateConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${hibernate.dialect}")
    private String dialect;

    @Value("${hibernate.show_sql}")
    private String showSql;

    @Value("${hibernate.format_sql}")
    private String formatSql;

    @Bean
    public SessionFactory sessionFactory() {
        org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();

        configuration.setProperty("hibernate.connection.url", url);
        configuration.setProperty("hibernate.connection.username", username);
        configuration.setProperty("hibernate.connection.password", password);
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");

        configuration.setProperty("hibernate.dialect", dialect);
        configuration.setProperty("hibernate.show_sql", showSql);
        configuration.setProperty("hibernate.format_sql", formatSql);
        configuration.setProperty("hibernate.hbm2ddl.auto", "none");

        configuration.addAnnotatedClass(ConversionRequest.class);

        return configuration.buildSessionFactory();
    }
}