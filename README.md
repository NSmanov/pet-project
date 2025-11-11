# Система Управления Задачами

Простое, но комплексное Spring Boot приложение для управления задачами, разработанное как демонстрационный проект для интеграции CI/CD с GitHub Actions.

## Возможности

- RESTful API для управления задачами (CRUD операции)
- База данных PostgreSQL с миграциями Flyway
- Функционал мягкого удаления (soft delete)
- Фильтрация и поиск задач
- Полное покрытие тестами (модульные + интеграционные тесты)
- CI/CD pipeline с GitHub Actions
- TestContainers для интеграционного тестирования
- Поддержка Docker

## Технологический Стек

- **Java 21**
- **Spring Boot 3.2.2**
  - Spring Web
  - Spring Data JPA
  - Spring Validation
- **PostgreSQL**
- **Flyway** для миграций базы данных
- **Lombok** для сокращения шаблонного кода
- **TestContainers** для интеграционного тестирования
- **Maven** для управления сборкой
- **Docker** для контейнеризации

## Структура Проекта

```
task-management/
├── src/
│   ├── main/
│   │   ├── java/com/example/taskmanagement/
│   │   │   ├── controller/         # REST контроллеры
│   │   │   ├── service/            # Бизнес-логика
│   │   │   ├── repository/         # Слой доступа к данным
│   │   │   ├── entity/             # JPA сущности
│   │   │   ├── dto/                # Объекты передачи данных
│   │   │   ├── exception/          # Пользовательские исключения
│   │   │   └── config/             # Классы конфигурации
│   │   └── resources/
│   │       ├── application.yml     # Конфигурация приложения
│   │       └── db/migration/       # Скрипты миграций Flyway
│   └── test/
│       └── java/com/example/taskmanagement/
│           ├── service/            # Модульные тесты
│           └── integration/        # Интеграционные тесты
├── .github/
│   └── workflows/
│       ├── ci.yml                  # Основной CI pipeline
│       └── pr-checks.yml           # Workflow проверки PR
├── pom.xml                         # Конфигурация Maven
└── README.md
```

## Требования

- Java 21 или выше
- Maven 3.8+
- PostgreSQL 13+ (для локальной разработки)
- Docker (опционально, для TestContainers и контейнерного развертывания)

## Начало Работы

### 1. Клонируйте репозиторий

```bash
git clone <repository-url>
cd pet-project
```

### 2. Настройте базу данных

Создайте базу данных PostgreSQL:

```sql
CREATE DATABASE taskdb;
```

При необходимости обновите `src/main/resources/application.yml` с учетными данными базы данных или используйте переменные окружения:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=taskdb
export DB_USER=postgres
export DB_PASSWORD=your_password
```

### 3. Соберите проект

```bash
mvn clean install
```

### 4. Запустите приложение

```bash
mvn spring-boot:run
```

Приложение запустится на `http://localhost:8080`

## API Endpoints

### Управление Задачами

| Метод | Endpoint | Описание |
|--------|----------|-------------|
| POST | `/api/tasks` | Создать новую задачу |
| GET | `/api/tasks` | Получить все задачи (поддержка пагинации) |
| GET | `/api/tasks/{id}` | Получить задачу по ID |
| PUT | `/api/tasks/{id}` | Обновить задачу |
| DELETE | `/api/tasks/{id}` | Удалить задачу (мягкое удаление) |
| GET | `/api/tasks/status/{status}` | Получить задачи по статусу |
| GET | `/api/tasks/search?keyword={keyword}` | Поиск задач по названию |
| GET | `/api/tasks/stats/count` | Получить общее количество задач |
| GET | `/api/tasks/stats/count/{status}` | Получить количество задач по статусу |

### Примеры Запросов

#### Создать Задачу

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Реализовать новую функцию",
    "description": "Добавить аутентификацию пользователей",
    "priority": "HIGH"
  }'
```

#### Получить Все Задачи

```bash
curl http://localhost:8080/api/tasks
```

#### Обновить Задачу

```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Обновленное название задачи",
    "status": "IN_PROGRESS",
    "priority": "CRITICAL"
  }'
```

#### Поиск Задач

```bash
curl http://localhost:8080/api/tasks/search?keyword=функцию
```

## Статусы и Приоритеты Задач

### Значения Статусов
- `TODO` - Задача еще не начата
- `IN_PROGRESS` - Задача в работе
- `DONE` - Задача выполнена
- `CANCELLED` - Задача отменена

### Значения Приоритетов
- `LOW` - Низкий приоритет
- `MEDIUM` - Средний приоритет
- `HIGH` - Высокий приоритет
- `CRITICAL` - Критический приоритет

## Запуск Тестов

### Запустить все тесты

```bash
mvn test
```

### Запустить только модульные тесты

```bash
mvn test -Dtest=*Test
```

### Запустить только интеграционные тесты

```bash
mvn test -Dtest=*IntegrationTest
```

## GitHub Actions CI/CD

Проект включает два workflow GitHub Actions:

### 1. Основной CI Pipeline (`ci.yml`)

Запускается при push и pull request в ветки `main` и `develop`.

**Задачи:**
- **build-and-test**: Компилирует код, запускает модульные и интеграционные тесты, пакует приложение
- **code-quality**: Выполняет проверки качества кода и сканирование уязвимостей безопасности
- **build-status**: Финальная проверка статуса всех задач

### 2. Проверки PR (`pr-checks.yml`)

Запускается при событиях pull request (opened, synchronize, reopened).

**Задачи:**
- **pr-validation**: Валидирует заголовок PR и запускает все тесты
- **integration-test**: Запускает интеграционные тесты с TestContainers
- **auto-merge-dependabot**: Подготавливает PR от Dependabot для автоматического слияния (когда все проверки пройдены)

### Настройка GitHub Actions

1. Отправьте код на GitHub
2. Workflow автоматически запустятся при push/PR событиях
3. Проверьте вкладку "Actions" в репозитории для просмотра запусков workflow

## Миграции Базы Данных

Проект использует Flyway для контроля версий базы данных.

Файлы миграций находятся в `src/main/resources/db/migration/`

- `V001__Initial_Schema.sql` - Создает таблицу tasks с индексами
- `V002__Insert_Test_Data.sql` - Вставляет тестовые данные

Для ручного запуска миграций:

```bash
mvn flyway:migrate
```

## Поддержка Docker

### Сборка Docker образа

```bash
docker build -t task-management:latest .
```

### Запуск с Docker Compose

Создайте `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:17
    environment:
      POSTGRES_DB: taskdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    image: task-management:latest
    environment:
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: taskdb
      DB_USER: postgres
      DB_PASSWORD: postgres
    ports:
      - "8080:8080"
    depends_on:
      - postgres

volumes:
  postgres_data:
```

Запуск:

```bash
docker-compose up
```

## Советы по Разработке

### Использование IntelliJ IDEA

1. Импортируйте проект как Maven проект
2. Включите обработку аннотаций для Lombok:
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Отметьте "Enable annotation processing"
3. Установите плагин Lombok

### Стиль Кода

Проект следует стандартным соглашениям Java/Spring Boot:
- Используйте описательные имена переменных и методов
- Держите методы небольшими и сфокусированными
- Пишите тесты для новых функций
- Документируйте сложную логику комментариями

## Устранение Проблем

### Распространенные Проблемы

**Проблема: Тесты падают с ошибкой "Could not start PostgreSQL container"**
- Решение: Убедитесь, что Docker запущен на вашей машине

**Проблема: Приложение не запускается с ошибкой "Connection refused"**
- Решение: Проверьте, что PostgreSQL запущен и учетные данные корректны

**Проблема: Миграция Flyway не выполняется**
- Решение: Проверьте скрипты миграций на наличие синтаксических ошибок или удалите базу данных и создайте заново

## Участие в Разработке

1. Сделайте Fork репозитория
2. Создайте ветку для новой функции (`git checkout -b feature/amazing-feature`)
3. Зафиксируйте изменения (`git commit -m 'Add some amazing feature'`)
4. Отправьте в ветку (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## Лицензия

Это демонстрационный проект в образовательных целях.

## Контакты

По вопросам или предложениям, пожалуйста, создайте issue в GitHub репозитории.

---

**Приятного Кодирования!**
