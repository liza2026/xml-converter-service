# xml-json-microservices

Сервис преобразования данных из формата XML в JSON, реализованный в виде двух
независимых Spring Boot микросервисов.

## Содержание

- [Архитектура](#архитектура)
- [Хранение данных в БД](#хранение-данных-в-бд)
- [Технологии](#технологии)
- [API](#api)
- [Запуск проекта](#запуск-проекта)
- [Логирование](#логирование)

## Архитектура

- **Service 1** — единственная публичная точка входа. Принимает запросы от
  клиента, обращается к Service 2 за конвертацией и сохраняет историю
  обработанных запросов в PostgreSQL.
- **Service 2** — внутренний stateless-сервис конвертации. Клиенту напрямую
  недоступен (в смысле сценария использования — физически порт открыт, но
  предполагаемый путь обращения только через Service 1). Не хранит никакого
  состояния и ничего не пишет в БД.
- **PostgreSQL** — используется только Service 1, для хранения истории
  конвертаций. Схема БД управляется через Liquibase.

Все три компонента поднимаются в общей Docker-сети `converter-network` и
видят друг друга по именам контейнеров (`service1`, `service2`, `postgres`).

## Хранение данных в БД

Таблица `conversion_requests` (создаётся миграцией Liquibase
`001-create-conversion-requests-table.xml`):

- `id` - тип BIGSERIAL - первичный ключ, генерируется БД
- `json_result` - тип TEXT - результат конвертации, сериализованный JSON
- `request_date` - тип TIMESTAMP - дата и время запроса
- `processing_time_ms` - тип BIGINT - время обработки запроса в миллисекундах
- `xml_tags_count` - тип INTEGER - количество тегов во входном XML
- `json_keys_count` - тип INTEGER - количество ключей в результирующем JSON

По `request_date` создан индекс (`idx_conversion_requests_request_date`),
так как именно по этому полю по умолчанию сортируется выдача `/page`, и по
нему же чаще всего фильтруют.

## Технологии

- Java 17
- Spring Boot 4 (Spring MVC)
- Hibernate 6 (Core API, без Spring Data) + HikariCP
- PostgreSQL 15
- Liquibase — миграции схемы БД
- Jackson + `jackson-dataformat-xml` — конвертация XML ⇄ JSON
- Lombok, MapStruct
- Docker / Docker Compose

## API

### Servise 1

#### `POST /request`

Принимает XML, конвертирует его в JSON через Service 2 и сохраняет запись
в БД.

- **Content-Type:** `text/xml` или `application/xml`
- **Accept:** `application/json`

Пример запроса:

```bash
curl -X POST http://localhost:8080/request \
  -H "Content-Type: text/xml" \
  -d '<person><name>Иван</name><age>30</age></person>'
```

Пример ответа `200 OK`:

```json
{
  "id": 1,
  "result": {
    "name": "Иван",
    "age": "30"
  },
  "requestDate": "2026-06-21T18:42:11.123",
  "processingTimeMs": 47,
  "xmlTagsCount": 3,
  "jsonKeysCount": 2
}
```

#### `POST /page`

Возвращает постраничный список ранее обработанных запросов с опциональной
фильтрацией. Тело запроса необязательно — без него вернётся первая страница
размером 10 без фильтров.

- **Content-Type:** `application/json`

Поддерживаемые фильтры (любые поля можно опустить, все заданные условия
комбинируются через AND):

- `requestDateFrom` - тип ISO datetime - дата запроса, нижняя граница
- `requestDateTo` - тип ISO datetime - дата запроса, верхняя граница
- `processingTimeMin` - тип число (мс) - минимальное время обработки
- `processingTimeMax` - тип число (мс) - максимальное время обработки
- `xmlTagsCountMin` - тип число - минимальное количество XML-тегов
- `xmlTagsCountMax` - тип число - максимальное количество XML-тегов
- `jsonKeysCountMin` - тип число - минимальное количество JSON-ключей
- `jsonKeysCountMax` - тип число - максимальное количество JSON-ключей

Пример запроса:

```bash
curl -X POST http://localhost:8080/page \
  -H "Content-Type: application/json" \
  -d '{
        "page": 0,
        "size": 5,
        "filter": {
          "processingTimeMin": 10,
          "xmlTagsCountMax": 50
        }
      }'
```

Пример ответа `200 OK`:

```json
{
  "content": [
    {
      "id": 1,
      "jsonResult": "{\"name\":\"Иван\",\"age\":\"30\"}",
      "requestDate": "2026-06-21T18:42:11.123",
      "processingTimeMs": 47,
      "xmlTagsCount": 3,
      "jsonKeysCount": 2
    }
  ],
  "page": 0,
  "size": 5,
  "totalElements": 1,
  "totalPages": 1
}
```

### Service 2 (service2)

#### `POST /xml2format`

Конвертирует XML в JSON стандартными средствами Java (без XSLT). Вызывается
Service 1 автоматически при каждом запросе на `/request`; самостоятельный
вызов клиентом возможен, но не является предусмотренным сценарием
использования.

- **Content-Type:** `text/xml`
- **Accept:** `application/json`

Пример запроса:

```bash
curl -X POST http://localhost:8081/xml2format \
  -H "Content-Type: text/xml" \
  -d '<person><name>Иван</name><age>30</age></person>'
```

Пример ответа `200 OK`:

```json
{
  "result": {
    "name": "Иван",
    "age": "30"
  }
}
```

## Запуск проекта

Проект полностью контейнеризован: для запуска нужен только Docker и Docker
Compose, локальная установка Java/Maven/PostgreSQL не требуется.

1. Создать в корне проекта файл `.env` со следующими переменными:

   ```env
   POSTGRES_DB=converter
   POSTGRES_USER=converter_user
   POSTGRES_PASSWORD=converter_password
   POSTGRES_PORT=5432

   SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/converter

   SERVICE1_PORT=8080
   SERVICE2_PORT=8081
   SERVICE2_BASE_URL=http://service2:8081
   ```

2. Поднять всё одной командой:

   ```bash
   docker compose up --build
   ```

   Порядок старта контролируется через `depends_on`: Service 1 запускается
   только после того, как PostgreSQL пройдёт healthcheck, и после старта
   контейнера Service 2.

3. После старта доступны:
   - Service 1 — `http://localhost:8080`
   - Service 2 — `http://localhost:8081`
   - PostgreSQL — `localhost:5432` (или порт из `POSTGRES_PORT`)

Liquibase применит миграции и создаст таблицу `conversion_requests`
автоматически при первом старте Service 1.

## Логирование

Оба сервиса пишут логи в файл и в консоль (`logs/service1.log`,
`logs/service2.log`), с уровнем `DEBUG` для пакетов `com.elizaveta.*` и
`INFO` для всего остального. Service 2 логирует полный цикл
API-взаимодействия — факт получения запроса, тело запроса (на `DEBUG`),
результат конвертации и факт отправки ответа.
