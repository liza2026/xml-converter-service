package com.elizaveta.service1.dao;

import com.elizaveta.service1.dto.ConversionFilterDTO;
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

/**
 * Слой прямого доступа к таблице {@code conversion_requests} в PostgreSQL.
 * <p>
 * Работа с БД выполняется через Hibernate ({@link SessionFactory}/{@link Session}
 * и Criteria API), без репозиториев из коробки. Сессия берётся через
 * {@code sessionFactory.getCurrentSession()}, то есть этот класс сам не
 * управляет транзакциями и опирается на то, что транзакция (и привязанная к ней Hibernate-сессия)
 * уже открыта на момент вызова.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ConversionRequestDAO {

    private final SessionFactory sessionFactory;

    /**
     * Сохраняет в БД одну запись об обработанном запросе.
     *
     * @param entity новая сущность без id — поля {@code jsonResult},
     *               {@code requestDate}, {@code processingTimeMs},
     *               {@code xmlTagsCount}, {@code jsonKeysCount} уже заполнены
     * @return та же сущность, но с присвоенным {@code id} из БД
     */
    public ConversionRequest save(ConversionRequest entity) {

        Session session = sessionFactory.getCurrentSession();

        session.persist(entity);

        log.debug("Сохранена запись в БД с id={}", entity.getId());

        return entity;
    }

    /**
     * Выбирает одну страницу записей истории конвертаций, отсортированную
     * по дате запроса (новые сверху), с применением фильтра.
     *
     * @param filter критерии фильтрации; любое незаполненное поле фильтра
     *               просто не участвует в {@code WHERE}
     * @param page   номер страницы, начиная с 0 (смещение = {@code page * size})
     * @param size   максимальное количество записей на странице
     * @return список записей текущей страницы, без подсчёта общего количества
     */
    public List<ConversionRequest> findContent(ConversionFilterDTO filter, int page, int size) {

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

    /**
     * Считает общее количество записей, удовлетворяющих фильтру, без
     * выборки самих данных.
     *
     * @param filter критерии фильтрации
     * @return количество записей в таблице, подходящих под фильтр
     */
    public long countTotal(ConversionFilterDTO filter) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ConversionRequest> countRoot = countQuery.from(ConversionRequest.class);

        countQuery.select(cb.count(countRoot))
                .where(buildPredicates(cb, countRoot, filter));

        return session.createQuery(countQuery).getSingleResult();
    }

    /**
     * Строит набор условий {@code WHERE} из непустых полей фильтра.
     * <p>
     * Каждое поле {@link ConversionFilterDTO} (диапазоны даты запроса,
     * времени обработки, количества XML-тегов, количества JSON-ключей)
     * необязательно и проверяется на {@code null} отдельно: если поле не
     * задано клиентом, соответствующее условие в выборку не попадает.
     * Все добавленные условия комбинируются неявно через AND — именно так
     * реализована поддержка одновременной фильтрации сразу по нескольким
     * параметрам. Общий для {@link #findContent} и
     * {@link #countTotal}, чтобы логика фильтрации не дублировалась и не
     * могла разойтись между подсчётом и выборкой.
     *
     * @param cb     построитель критериев текущей сессии
     * @param root   корень запроса (сущность {@link ConversionRequest})
     * @param filter фильтр с диапазонами; любые поля могут быть {@code null}
     * @return массив предикатов;
     * пустой массив, если фильтр полностью пуст
     */
    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<ConversionRequest> root, ConversionFilterDTO filter) {

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