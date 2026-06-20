package com.elizaveta.service1.dao;

import com.elizaveta.service1.dto.ConversionFilter;
import com.elizaveta.service1.entity.ConversionRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ConversionRequestDao {

    private final SessionFactory sessionFactory;

    public ConversionRequest save(ConversionRequest entity) {

        Session session = sessionFactory.getCurrentSession();

        session.persist(entity);

        log.debug("Сохранена запись в БД с id={}", entity.getId());

        return entity;
    }

    public List<ConversionRequest> findContent(ConversionFilter filter, int page, int size) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<ConversionRequest> dataQuery = cb.createQuery(ConversionRequest.class);
        Root<ConversionRequest> root = dataQuery.from(ConversionRequest.class);

        dataQuery.select(root)
                .where(buildPredicates(cb, root, filter))
                .orderBy(cb.desc(root.get("requestDate")));

        return session.createQuery(dataQuery)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public long countTotal(ConversionFilter filter) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ConversionRequest> countRoot = countQuery.from(ConversionRequest.class);

        countQuery.select(cb.count(countRoot))
                .where(buildPredicates(cb, countRoot, filter));

        return session.createQuery(countQuery).getSingleResult();
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<ConversionRequest> root, ConversionFilter filter) {

        List<Predicate> predicates = new ArrayList<>();

        if (filter.getRequestDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.<LocalDateTime>get("requestDate"), filter.getRequestDateFrom()));
        }

        if (filter.getRequestDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    root.<LocalDateTime>get("requestDate"), filter.getRequestDateTo()));
        }

        if (filter.getProcessingTimeMin() != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.<Long>get("processingTimeMs"), filter.getProcessingTimeMin()));
        }

        if (filter.getProcessingTimeMax() != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    root.<Long>get("processingTimeMs"), filter.getProcessingTimeMax()));
        }

        if (filter.getXmlTagsCountMin() != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.<Integer>get("xmlTagsCount"), filter.getXmlTagsCountMin()));
        }

        if (filter.getXmlTagsCountMax() != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    root.<Integer>get("xmlTagsCount"), filter.getXmlTagsCountMax()));
        }

        if (filter.getJsonKeysCountMin() != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.<Integer>get("jsonKeysCount"), filter.getJsonKeysCountMin()));
        }

        if (filter.getJsonKeysCountMax() != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    root.<Integer>get("jsonKeysCount"), filter.getJsonKeysCountMax()));
        }
        return predicates.toArray(new Predicate[0]);
    }
}