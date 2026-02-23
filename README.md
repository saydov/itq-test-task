<div align="center">
  <h1>Document Service</h1>
  <img src="https://img.shields.io/badge/language-Java_21-green?style=flat" />
  <img src="https://img.shields.io/badge/framework-Spring_Boot_3.3-green?style=flat" />
  <img src="https://img.shields.io/badge/dev_branch-development-green?style=flat" />
  <br><br>
  Сервис управления документами с пакетной обработкой, фоновыми воркерами и реестром утверждений.
</div>

## Требования

- Java 21
- Docker / Docker Compose

## Запуск

### 1. База данных

```bash
docker compose up -d
```

PostgreSQL поднимется на `localhost:5432`, база `document_service`.

### 2. Сервис

```bash
./gradlew service:bootRun
```

Сервис стартует на `http://localhost:8080`. Liquibase автоматически накатит миграции.

### 3. Утилита генерации документов

```bash
./gradlew generator:run
```

Параметры в `generator/src/main/resources/generator-config.properties`:

| Параметр | Значение по умолчанию | Описание |
|----------|----------------------|----------|
| `count` | 100 | Количество документов |
| `api.url` | http://localhost:8080/api/documents | URL сервиса |
| `author` | Generator | Автор |
| `title.prefix` | Generated Document | Префикс названия |

Можно передать путь к другому файлу конфигурации:

```bash
./gradlew generator:run --args="/path/to/config.properties"
```

### 4. Тесты

```bash
./gradlew service:test
```

Тесты используют Testcontainers, Docker должен быть запущен.

## API

Готовая коллекция запросов для Postman: [`postman-collection.json`](postman-collection.json). Импорт: File > Import > выбрать файл. Базовый URL настраивается через переменную `{{baseUrl}}`.

### Документы (`/api/documents`)

| Метод | URL | Описание |
|-------|-----|----------|
| POST | `/api/documents` | Создать документ |
| GET | `/api/documents/{id}` | Получить документ с историей |
| GET | `/api/documents/batch?ids=1,2,3` | Пакетное получение |
| POST | `/api/documents/{action}` | Перевести статус (`submit` / `approve`) |
| GET | `/api/documents/search` | Поиск с фильтрами |

**Создание документа:**

```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{"author": "Богдан", "title": "Акт приема-передачи за февраль"}'
```

**Пакетная отправка на согласование:**

```bash
curl -X POST http://localhost:8080/api/documents/submit \
  -H "Content-Type: application/json" \
  -d '{"ids": [1, 2, 3], "initiator": "Петров", "comment": "На проверку"}'
```

**Пакетное утверждение:**

```bash
curl -X POST http://localhost:8080/api/documents/approve \
  -H "Content-Type: application/json" \
  -d '{"ids": [1, 2, 3], "initiator": "Сидоров"}'
```

Результат по каждому ID: `SUCCESS`, `CONFLICT`, `NOT_FOUND`, `REGISTRY_ERROR`.

**Поиск документов:**

```bash
curl "http://localhost:8080/api/documents/search?author=Богдан&status=DRAFT&from=2025-02-01T00:00:00&to=2025-02-28T23:59:59&page=0&size=20"
```

Фильтр по дате работает по `created_at` (дата создания). Все параметры опциональны.

### Реестр утверждений (`/api/approvals`)

| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/approvals/document/{documentId}` | Запись утверждения |
| GET | `/api/approvals/document/{documentId}/status` | Статус утверждения |
| GET | `/api/approvals/check?ids=1,2,3` | Пакетная проверка статусов |
| GET | `/api/approvals/search` | Поиск утверждений |
| DELETE | `/api/approvals/document/{documentId}` | Отозвать утверждение |

### Тест конкурентности (`/api/documents/concurrency-test`)

Доступен только в профиле `test`. Запускает параллельные попытки утвердить один документ.

```bash
curl -X POST http://localhost:8080/api/documents/concurrency-test \
  -H "Content-Type: application/json" \
  -d '{"id": 1, "threads": 5, "attempts": 10, "initiator": "Михаил"}'
```

Ожидание: ровно одна попытка успешна, остальные получают конфликт.

## Конфигурация

Основные параметры в `service/src/main/resources/application.yml`:

| Параметр | Значение | Описание |
|----------|---------|----------|
| `app.transition.batch-size` | 50 | Размер пачки при обработке |
| `app.transition.max-concurrency` | 4 | Макс. параллельных пачек |
| `app.workers.submit.enabled` | true | Вкл/выкл SUBMIT-воркера |
| `app.workers.submit.interval` | 10000 | Интервал SUBMIT-воркера (мс) |
| `app.workers.approve.enabled` | true | Вкл/выкл APPROVE-воркера |
| `app.workers.approve.interval` | 15000 | Интервал APPROVE-воркера (мс) |

## Логи

### Утилита генерации

```
[1/100] Документ создан за 45ms
[2/100] Документ создан за 32ms
...
Генерация завершена: успешно=100, ошибок=0, время=3200ms
```

### Пакетная обработка

```
SUBMIT: 50 documents, initiator=Петров
SUBMIT completed: total=50, success=48, failed=2
```

### Фоновые воркеры

```
SubmitWorker: processing 50 DRAFT documents
SubmitWorker: batch completed in 120ms, success=50, failed=0
ApproveWorker: processing 50 SUBMITTED documents
ApproveWorker: batch completed in 200ms, success=49, failed=1
```

## Масштабирование на 5000+ ID

Пакетная обработка уже поддерживает произвольное количество ID. Входной список разбивается на пачки по `batch-size`, которые обрабатываются параллельно (до `max-concurrency` одновременно) на виртуальных потоках. При ошибке пачки происходит fallback на поштучную обработку, что не останавливает остальные пачки.

## Выделение реестра утверждений в отдельную систему

Реестр утверждений уже выделен в отдельный HTTP-сервис (`/api/approvals`) с собственным контроллером, сервисом и репозиторием. Для выноса в физически отдельный сервис достаточно:

1. Извлечь `ApprovalRegistryController`, `ApprovalRegistryService`, `ApprovalRegistryRepository` в отдельный Spring Boot модуль
2. Заменить прямые вызовы репозитория в `TransitionPersistenceService` на HTTP-клиент (WebClient/RestClient)
3. Реализовать Saga или паттерн Try-Confirm/Cancel для обеспечения консистентности между статусом документа и записью в реестре
