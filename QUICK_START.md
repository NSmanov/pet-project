# Руководство по Быстрому Старту

Запустите Систему Управления Задачами за несколько минут!

## Вариант 1: Локальная Разработка (Рекомендуется для разработки)

### Требования
- Java 21
- Maven 3.8+
- PostgreSQL 13+

### Шаги

1. **Клонируйте репозиторий**
   ```bash
   git clone <repository-url>
   cd pet-project
   ```

2. **Запустите PostgreSQL**
   ```bash
   # Используя Docker
   docker run --name postgres-dev \
     -e POSTGRES_DB=taskdb \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     -d postgres:17
   ```

3. **Запустите приложение**
   ```bash
   mvn spring-boot:run
   ```

4. **Проверьте API**
   ```bash
   curl http://localhost:8080/api/tasks
   ```

## Вариант 2: Docker Compose (Рекомендуется для быстрого тестирования)

### Требования
- Docker
- Docker Compose

### Шаги

1. **Клонируйте репозиторий**
   ```bash
   git clone <repository-url>
   cd pet-project
   ```

2. **Запустите все сервисы**
   ```bash
   docker-compose up -d
   ```

3. **Проверьте логи**
   ```bash
   docker-compose logs -f app
   ```

4. **Проверьте API**
   ```bash
   curl http://localhost:8080/api/tasks
   ```

5. **Остановите все сервисы**
   ```bash
   docker-compose down
   ```

## Вариант 3: Только Запуск Тестов

### Требования
- Java 21
- Maven 3.8+
- Docker (для TestContainers)

### Шаги

```bash
# Клонируйте репозиторий
git clone <repository-url>
cd pet-project

# Запустите все тесты (база данных запустится автоматически через TestContainers)
mvn test
```

## Первые API Вызовы

### 1. Получить все задачи (включает тестовые данные)
```bash
curl http://localhost:8080/api/tasks | jq
```

### 2. Создать вашу первую задачу
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Моя первая задача",
    "description": "Изучаю API",
    "priority": "HIGH"
  }' | jq
```

### 3. Обновить задачу
```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS"
  }' | jq
```

### 4. Поиск задач
```bash
curl "http://localhost:8080/api/tasks/search?keyword=первая" | jq
```

## Проверка Работоспособности

```bash
# Проверьте, что приложение запущено
curl http://localhost:8080/api/tasks/stats/count

# Должно вернуть что-то вроде:
# {
#   "success": true,
#   "data": 9,
#   "timestamp": "2024-01-15T10:30:00"
# }
```

## Устранение Проблем

### Проблема: Порт 8080 уже используется
```bash
# Измените порт в application.yml или используйте переменную окружения
export SERVER_PORT=8081
mvn spring-boot:run
```

### Проблема: Не удается подключиться к PostgreSQL
```bash
# Проверьте, что PostgreSQL запущен
docker ps

# Если нет, запустите его
docker start postgres-dev

# Или проверьте настройки подключения в application.yml
```

### Проблема: Тесты не проходят
```bash
# Убедитесь, что Docker запущен (для TestContainers)
docker --version

# Очистите и пересоберите
mvn clean install
```

## Следующие Шаги

1. **Изучите API** - Смотрите [API_EXAMPLES.md](API_EXAMPLES.md) для подробных примеров
2. **Прочитайте документацию** - См. [README.md](README.md) для полной документации
3. **Участвуйте в разработке** - Проверьте [CONTRIBUTING.md](CONTRIBUTING.md) для руководств
4. **Настройте GitHub Actions** - Отправьте на GitHub и увидьте CI/CD в действии

## Тестовые Данные по Умолчанию

Приложение поставляется с тестовыми задачами (см. V002__Insert_Test_Data.sql):
- 9 примеров задач с различными статусами и приоритетами
- Демонстрирует все возможности системы
- Может использоваться для немедленного тестирования

## Переменные Окружения

| Переменная | По умолчанию | Описание |
|----------|---------|-------------|
| DB_HOST | localhost | Хост PostgreSQL |
| DB_PORT | 5432 | Порт PostgreSQL |
| DB_NAME | taskdb | Имя базы данных |
| DB_USER | postgres | Пользователь БД |
| DB_PASSWORD | postgres | Пароль БД |
| SERVER_PORT | 8080 | Порт приложения |

## Полезные Команды

```bash
# Сборка без тестов
mvn clean package -DskipTests

# Запуск с пользовательской базой данных
DB_HOST=remote-host DB_NAME=prod_taskdb mvn spring-boot:run

# Генерация отчетов проекта
mvn site

# Проверка обновлений зависимостей
mvn versions:display-dependency-updates

# Очистка всего
mvn clean && docker-compose down -v
```

## Проверка Работоспособности

```bash
# Проверьте, что приложение отвечает
curl -f http://localhost:8080/api/tasks/stats/count || echo "Приложение не запущено"
```

---

**Все готово! Приятного кодирования!**
