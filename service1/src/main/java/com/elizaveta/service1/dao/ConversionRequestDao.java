package com.elizaveta.service1.dao;

import com.elizaveta.service1.entity.ConversionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ConversionRequestDao {

    private final SessionFactory sessionFactory;

    public ConversionRequest save(ConversionRequest entity) {

        try (Session session = sessionFactory.openSession()) {

            Transaction transaction = session.beginTransaction();

            try {
                session.persist(entity);

                transaction.commit();

                log.debug("Сохранена запись в БД с id={}", entity.getId());

                return entity;

            } catch (Exception e) {
                transaction.rollback();
                log.error("Ошибка сохранения в БД: {}", e.getMessage());
                throw e;
            }
        }
    }
}