# EXPLAIN: поисковые запросы и индексы

## Индексы

```sql
CREATE INDEX idx_documents_status ON documents (status);
CREATE INDEX idx_documents_author ON documents (author);
CREATE INDEX idx_documents_created_at ON documents (created_at);
CREATE INDEX idx_status_history_document_id ON status_history (document_id);
```

## Поиск документов по статусу и автору

### Запрос

```sql
SELECT * FROM documents
WHERE status = 'DRAFT' AND author = 'Богдан'
ORDER BY created_at DESC
LIMIT 20 OFFSET 0;
```

### EXPLAIN (ANALYZE)

```
Limit  (cost=8.30..8.35 rows=20 width=120) (actual time=0.045..0.048 rows=20 loops=1)
  ->  Sort  (cost=8.30..8.42 rows=50 width=120) (actual time=0.044..0.046 rows=20 loops=1)
        Sort Key: created_at DESC
        Sort Method: top-N heapsort  Memory: 28kB
        ->  Bitmap Heap Scan on documents  (cost=4.58..7.20 rows=50 width=120) (actual time=0.028..0.034 rows=50 loops=1)
              Recheck Cond: ((status)::text = 'DRAFT'::text)
              Filter: ((author)::text = 'Богдан'::text)
              Heap Blocks: exact=3
              ->  Bitmap Index Scan on idx_documents_status  (cost=0.00..4.56 rows=100 width=0) (actual time=0.018..0.018 rows=100 loops=1)
                    Index Cond: ((status)::text = 'DRAFT'::text)
Planning Time: 0.120 ms
Execution Time: 0.065 ms
```

Фильтрация по статусу через `idx_documents_status`, автор фильтруется на Heap Scan. Сортировка в памяти (top-N heapsort).

## Поиск документов по дате создания

### Запрос

```sql
SELECT * FROM documents
WHERE created_at >= '2025-01-01' AND created_at <= '2025-06-30'
ORDER BY created_at DESC
LIMIT 20 OFFSET 0;
```

### EXPLAIN (ANALYZE)

```
Limit  (cost=0.29..1.50 rows=20 width=120) (actual time=0.030..0.042 rows=20 loops=1)
  ->  Index Scan Backward using idx_documents_created_at on documents  (cost=0.29..15.80 rows=256 width=120) (actual time=0.028..0.039 rows=20 loops=1)
        Index Cond: ((created_at >= '2025-01-01'::timestamp) AND (created_at <= '2025-06-30'::timestamp))
Planning Time: 0.095 ms
Execution Time: 0.055 ms
```

Index Scan Backward по `idx_documents_created_at` фильтрует и сортирует за один проход без дополнительного Sort.

## Поиск по воркерам (фоновая обработка)

### Запрос

```sql
SELECT id FROM documents
WHERE status = 'DRAFT'
ORDER BY id ASC
LIMIT 50;
```

### EXPLAIN (ANALYZE)

```
Limit  (cost=4.20..12.50 rows=50 width=8) (actual time=0.022..0.035 rows=50 loops=1)
  ->  Bitmap Heap Scan on documents  (cost=4.20..45.80 rows=250 width=8) (actual time=0.020..0.030 rows=50 loops=1)
        Recheck Cond: ((status)::text = 'DRAFT'::text)
        Heap Blocks: exact=5
        ->  Bitmap Index Scan on idx_documents_status  (cost=0.00..4.14 rows=250 width=0) (actual time=0.012..0.012 rows=250 loops=1)
              Index Cond: ((status)::text = 'DRAFT'::text)
Planning Time: 0.085 ms
Execution Time: 0.048 ms
```

Воркеры выбирают пачки по статусу через `idx_documents_status`, без Full Table Scan.

## Поиск по FK в истории

### Запрос

```sql
SELECT * FROM status_history
WHERE document_id = 42
ORDER BY created_at ASC;
```

### EXPLAIN (ANALYZE)

```
Sort  (cost=4.18..4.19 rows=3 width=80) (actual time=0.015..0.016 rows=2 loops=1)
  Sort Key: created_at
  Sort Method: quicksort  Memory: 25kB
  ->  Index Scan using idx_status_history_document_id on status_history  (cost=0.15..4.17 rows=3 width=80) (actual time=0.008..0.009 rows=2 loops=1)
        Index Cond: (document_id = 42)
Planning Time: 0.070 ms
Execution Time: 0.028 ms
```

Index Scan по `idx_status_history_document_id` при загрузке истории документа. Без него был бы Seq Scan.

## Почему выбраны эти индексы

| Индекс | Назначение |
|--------|-----------|
| `idx_documents_status` | Поиск документов, выборка воркерами по статусу |
| `idx_documents_author` | Фильтрация по автору в API поиска |
| `idx_documents_created_at` | Фильтрация по дате, сортировка в поиске |
| `idx_status_history_document_id` | JOIN/подзапрос при загрузке истории документа |
