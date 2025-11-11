# Примеры API и Руководство по Тестированию

Этот документ предоставляет исчерпывающие примеры для тестирования API Системы Управления Задачами.

## Базовый URL

```
http://localhost:8080
```

## Формат Ответов API

Все endpoints возвращают ответы в следующем формате:

```json
{
  "success": true/false,
  "message": "Опциональное сообщение",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00"
}
```

## 1. Создать Задачу

**Endpoint:** `POST /api/tasks`

**Тело Запроса:**
```json
{
  "title": "Реализовать аутентификацию пользователей",
  "description": "Добавить аутентификацию на основе JWT в приложение",
  "priority": "HIGH"
}
```

**Ответ:**
```json
{
  "success": true,
  "message": "Task created successfully",
  "data": {
    "id": 1,
    "title": "Реализовать аутентификацию пользователей",
    "description": "Добавить аутентификацию на основе JWT в приложение",
    "status": "TODO",
    "priority": "HIGH",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Пример cURL:**
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Реализовать аутентификацию пользователей",
    "description": "Добавить аутентификацию на основе JWT в приложение",
    "priority": "HIGH"
  }'
```

## 2. Получить Все Задачи

**Endpoint:** `GET /api/tasks`

**Ответ:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Реализовать аутентификацию пользователей",
      "description": "Добавить аутентификацию на основе JWT в приложение",
      "status": "TODO",
      "priority": "HIGH",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    },
    {
      "id": 2,
      "title": "Настроить CI/CD pipeline",
      "description": "Настроить GitHub Actions для автоматического тестирования",
      "status": "IN_PROGRESS",
      "priority": "CRITICAL",
      "createdAt": "2024-01-15T11:00:00",
      "updatedAt": "2024-01-15T12:00:00"
    }
  ],
  "timestamp": "2024-01-15T13:00:00"
}
```

**Пример cURL:**
```bash
curl http://localhost:8080/api/tasks
```

## 3. Получить Все Задачи с Пагинацией

**Endpoint:** `GET /api/tasks?page={page}&size={size}`

**Параметры:**
- `page` (опционально): Номер страницы (начиная с 0)
- `size` (опционально): Количество элементов на странице

**Ответ:** Такой же как в "Получить Все Задачи", но с пагинацией

**Пример cURL:**
```bash
curl "http://localhost:8080/api/tasks?page=0&size=10"
```

## 4. Получить Задачу по ID

**Endpoint:** `GET /api/tasks/{id}`

**Ответ:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Реализовать аутентификацию пользователей",
    "description": "Добавить аутентификацию на основе JWT в приложение",
    "status": "TODO",
    "priority": "HIGH",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T13:00:00"
}
```

**Пример cURL:**
```bash
curl http://localhost:8080/api/tasks/1
```

## 5. Обновить Задачу

**Endpoint:** `PUT /api/tasks/{id}`

**Тело Запроса (все поля опциональны):**
```json
{
  "title": "Реализовать аутентификацию OAuth2",
  "description": "Использовать OAuth2 вместо JWT",
  "status": "IN_PROGRESS",
  "priority": "CRITICAL"
}
```

**Ответ:**
```json
{
  "success": true,
  "message": "Task updated successfully",
  "data": {
    "id": 1,
    "title": "Реализовать аутентификацию OAuth2",
    "description": "Использовать OAuth2 вместо JWT",
    "status": "IN_PROGRESS",
    "priority": "CRITICAL",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T14:00:00"
  },
  "timestamp": "2024-01-15T14:00:00"
}
```

**Пример cURL:**
```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Реализовать аутентификацию OAuth2",
    "status": "IN_PROGRESS",
    "priority": "CRITICAL"
  }'
```

## 6. Удалить Задачу

**Endpoint:** `DELETE /api/tasks/{id}`

**Ответ:**
```json
{
  "success": true,
  "message": "Task deleted successfully",
  "data": null,
  "timestamp": "2024-01-15T15:00:00"
}
```

**Пример cURL:**
```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

## 7. Получить Задачи по Статусу

**Endpoint:** `GET /api/tasks/status/{status}`

**Допустимые Значения Статусов:**
- `TODO`
- `IN_PROGRESS`
- `DONE`
- `CANCELLED`

**Ответ:**
```json
{
  "success": true,
  "data": [
    {
      "id": 3,
      "title": "Написать документацию",
      "description": "Создать документацию API",
      "status": "TODO",
      "priority": "MEDIUM",
      "createdAt": "2024-01-15T12:00:00",
      "updatedAt": "2024-01-15T12:00:00"
    }
  ],
  "timestamp": "2024-01-15T15:30:00"
}
```

**Пример cURL:**
```bash
curl http://localhost:8080/api/tasks/status/TODO
```

## 8. Поиск Задач по Названию

**Endpoint:** `GET /api/tasks/search?keyword={keyword}`

**Параметры:**
- `keyword` (обязательно): Термин для поиска в названиях задач

**Ответ:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Реализовать аутентификацию пользователей",
      "description": "Добавить аутентификацию на основе JWT в приложение",
      "status": "TODO",
      "priority": "HIGH",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "timestamp": "2024-01-15T16:00:00"
}
```

**Пример cURL:**
```bash
curl "http://localhost:8080/api/tasks/search?keyword=аутентификацию"
```

## 9. Получить Общее Количество Задач

**Endpoint:** `GET /api/tasks/stats/count`

**Ответ:**
```json
{
  "success": true,
  "data": 15,
  "timestamp": "2024-01-15T16:30:00"
}
```

**Пример cURL:**
```bash
curl http://localhost:8080/api/tasks/stats/count
```

## 10. Получить Количество Задач по Статусу

**Endpoint:** `GET /api/tasks/stats/count/{status}`

**Ответ:**
```json
{
  "success": true,
  "data": 5,
  "timestamp": "2024-01-15T17:00:00"
}
```

**Пример cURL:**
```bash
curl http://localhost:8080/api/tasks/stats/count/TODO
```

## Ответы с Ошибками

### 404 Not Found

```json
{
  "success": false,
  "message": "Task not found with id: 999",
  "data": null,
  "timestamp": "2024-01-15T17:30:00"
}
```

### 400 Bad Request (Ошибка Валидации)

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "title": "Title is required",
    "priority": "Priority is required"
  },
  "timestamp": "2024-01-15T18:00:00"
}
```

### 500 Internal Server Error

```json
{
  "success": false,
  "message": "An unexpected error occurred: <детали ошибки>",
  "data": null,
  "timestamp": "2024-01-15T18:30:00"
}
```

## Тестирование с Postman

1. Импортируйте коллекцию, создав запросы для каждого endpoint
2. Установите базовый URL как переменную окружения
3. Создайте тесты для позитивных и негативных сценариев
4. Используйте Collection Runner от Postman для автоматического тестирования

## Тестирование с HTTPie

HTTPie - это удобный HTTP-клиент командной строки.

### Установка HTTPie

```bash
brew install httpie  # macOS
pip install httpie   # Python
```

### Примеры

```bash
# Создать задачу
http POST localhost:8080/api/tasks \
  title="Новая Задача" \
  description="Описание задачи" \
  priority="HIGH"

# Получить все задачи
http GET localhost:8080/api/tasks

# Обновить задачу
http PUT localhost:8080/api/tasks/1 \
  status="DONE"

# Поиск задач
http GET localhost:8080/api/tasks/search keyword==функция
```

## Нагрузочное Тестирование с Apache Bench (ab)

```bash
# Тест GET endpoint с 1000 запросами, 10 одновременных
ab -n 1000 -c 10 http://localhost:8080/api/tasks

# Тест POST endpoint
ab -n 100 -c 5 -p task.json -T application/json http://localhost:8080/api/tasks
```

Где `task.json` содержит:
```json
{"title":"Задача Нагрузочного Теста","description":"Тестирование нагрузки","priority":"MEDIUM"}
```

## Скрипт Автоматизированного Тестирования

Сохраните как `test_api.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080/api/tasks"

echo "1. Создание задачи..."
TASK_ID=$(curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{"title":"Тестовая Задача","description":"Автоматизированный тест","priority":"HIGH"}' \
  | jq -r '.data.id')

echo "Задача создана с ID: $TASK_ID"

echo "2. Получение задачи по ID..."
curl -s "$BASE_URL/$TASK_ID" | jq

echo "3. Обновление задачи..."
curl -s -X PUT "$BASE_URL/$TASK_ID" \
  -H "Content-Type: application/json" \
  -d '{"status":"IN_PROGRESS"}' | jq

echo "4. Поиск задач..."
curl -s "$BASE_URL/search?keyword=Тестовая" | jq

echo "5. Получение количества задач..."
curl -s "$BASE_URL/stats/count" | jq

echo "6. Удаление задачи..."
curl -s -X DELETE "$BASE_URL/$TASK_ID" | jq

echo "Тест завершен!"
```

Запуск: `chmod +x test_api.sh && ./test_api.sh`
