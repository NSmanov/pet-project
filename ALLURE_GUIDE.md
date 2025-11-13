# Руководство по Allure Reports

Этот проект использует Allure Framework для генерации красивых и информативных отчетов о тестировании.

## 🎯 Что такое Allure?

Allure Framework - это гибкий инструмент для создания отчетов о тестировании, который:
- Предоставляет наглядные графики и статистику
- Группирует тесты по различным критериям
- Показывает историю выполнения тестов
- Отображает детальные логи и скриншоты
- Интегрируется с CI/CD системами

## 📊 Запуск Тестов с Allure

### Локальный запуск

```bash
# 1. Запустить тесты
mvn clean test

# 2. Сгенерировать отчет
mvn allure:report

# 3. Открыть отчет в браузере
open target/site/allure-maven-plugin/index.html

# ИЛИ запустить локальный сервер с отчетом
mvn allure:serve
```

### Быстрая команда

```bash
# Запустить тесты и сразу открыть отчет
mvn clean test allure:serve
```

## 📁 Структура Отчета

### Overview (Обзор)
- **Statistics**: Общая статистика выполнения
- **Severity**: Распределение по критичности
- **Duration**: Время выполнения тестов
- **Categories**: Категории ошибок

### Suites (Тестовые Наборы)
Группировка тестов по тестовым классам:
- `TaskServiceImplTest` - модульные тесты сервисного слоя
- `TaskIntegrationTest` - интеграционные тесты API

### Graphs (Графики)
- **Status Chart**: Круговая диаграмма статусов
- **Severity Chart**: Распределение по критичности
- **Duration Chart**: График времени выполнения
- **Categories Trend**: Тренд категорий во времени
- **Retry Trend**: Статистика повторных запусков

### Timeline (Временная Шкала)
Визуализация последовательности выполнения тестов с временными метками.

### Behaviors (Поведение)
Группировка тестов по функциональности (Stories/Features).

### Packages (Пакеты)
Иерархическая группировка по Java пакетам.

## 🔧 Конфигурация

### Maven Configuration

```xml
<properties>
    <allure.version>2.25.0</allure.version>
</properties>

<dependencies>
    <dependency>
        <groupId>io.qameta.allure</groupId>
        <artifactId>allure-junit5</artifactId>
        <version>${allure.version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>io.qameta.allure</groupId>
            <artifactId>allure-maven</artifactId>
            <version>2.12.0</version>
        </plugin>
    </plugins>
</build>
```

### Системные Свойства

```bash
# Указать директорию для результатов
-Dallure.results.directory=target/allure-results
```

## 🎨 Аннотации Allure

Вы можете использовать аннотации Allure для обогащения отчетов:

```java
import io.qameta.allure.*;

@Epic("Task Management")
@Feature("Task CRUD Operations")
@Story("Create Task")
@Severity(SeverityLevel.CRITICAL)
@DisplayName("Should create task successfully")
@Description("Проверяет создание новой задачи с валидными данными")
@Test
void shouldCreateTaskSuccessfully() {
    // Arrange
    Step.step("Подготовка тестовых данных", () -> {
        // ...
    });

    // Act
    Step.step("Создание задачи", () -> {
        // ...
    });

    // Assert
    Step.step("Проверка результата", () -> {
        // ...
    });
}
```

## 🚀 GitHub Actions Интеграция

Allure отчеты автоматически генерируются в CI/CD:

### В Main CI Pipeline (`ci.yml`)
```yaml
- name: Generate Allure Report
  if: always()
  run: mvn allure:report

- name: Upload Allure Report
  if: always()
  uses: actions/upload-artifact@v4
  with:
    name: allure-report
    path: target/site/allure-maven-plugin/
```

### В PR Checks (`pr-checks.yml`)
```yaml
- name: Generate Allure Report
  if: always()
  run: mvn allure:report

- name: Upload Allure Report
  if: always()
  uses: actions/upload-artifact@v4
  with:
    name: pr-allure-report
    path: target/site/allure-maven-plugin/
```

### Просмотр Отчетов в GitHub Actions

1. Перейдите в раздел **Actions** вашего репозитория
2. Выберите нужный workflow run
3. Прокрутите вниз до секции **Artifacts**
4. Скачайте артефакт `allure-report` или `pr-allure-report`
5. Распакуйте архив и откройте `index.html` в браузере

## 📈 Интерпретация Результатов

### Статусы Тестов

- ✅ **Passed**: Тест выполнен успешно
- ❌ **Failed**: Тест провалился
- ⚠️ **Broken**: Тест сломан (ошибка в коде теста)
- ⏭️ **Skipped**: Тест пропущен
- ❓ **Unknown**: Неизвестный статус

### Уровни Критичности

- 🔴 **Blocker**: Блокирующий
- 🟠 **Critical**: Критический
- 🟡 **Normal**: Обычный
- 🔵 **Minor**: Незначительный
- ⚪ **Trivial**: Тривиальный

### Категории Ошибок

Allure автоматически категоризирует ошибки:
- **Product defects**: Ошибки в коде приложения
- **Test defects**: Ошибки в коде тестов
- **System defects**: Ошибки инфраструктуры

## 🔍 Детальная Информация о Тестах

Каждый тест в отчете содержит:
- ⏱️ Время выполнения
- 📊 Параметры теста
- 📝 Шаги выполнения
- 📎 Вложения (логи, скриншоты)
- 🏷️ Метки и категории
- 🔗 Ссылки на issues/TMS
- 📜 История выполнения

## 🎯 Best Practices

### 1. Используйте Описательные Имена
```java
@DisplayName("Should return 404 when task not found")
@Test
void shouldReturn404WhenTaskNotFound() { }
```

### 2. Группируйте по Функциональности
```java
@Epic("Task Management")
@Feature("Task CRUD")
@Story("Update Task")
```

### 3. Добавляйте Шаги
```java
Step.step("Open task details page", () -> { });
Step.step("Update task title", () -> { });
Step.step("Save changes", () -> { });
Step.step("Verify task updated", () -> { });
```

### 4. Прикрепляйте Артефакты
```java
Allure.addAttachment("Request", "application/json", requestJson);
Allure.addAttachment("Response", "application/json", responseJson);
```

### 5. Используйте Severity
```java
@Severity(SeverityLevel.CRITICAL)  // Для важных тестов
@Severity(SeverityLevel.MINOR)     // Для некритичных
```

## 🛠️ Troubleshooting

### Проблема: Отчет не генерируется

**Решение:**
```bash
# Убедитесь, что тесты выполнились
mvn clean test

# Проверьте наличие результатов
ls -la target/allure-results/

# Попробуйте очистить кеш
rm -rf target/allure-results/
rm -rf target/site/allure-maven-plugin/
mvn clean test allure:report
```

### Проблема: Пустой отчет

**Причина:** Тесты не были запущены перед генерацией отчета.

**Решение:**
```bash
# Всегда запускайте тесты перед генерацией
mvn clean test allure:report
```

### Проблема: Отчет не открывается в браузере

**Решение:**
```bash
# Используйте allure:serve вместо прямого открытия
mvn allure:serve

# ИЛИ запустите локальный HTTP сервер
cd target/site/allure-maven-plugin/
python3 -m http.server 8000
# Откройте http://localhost:8000 в браузере
```

## 📚 Дополнительные Ресурсы

- [Официальная документация Allure](https://docs.qameta.io/allure/)
- [Allure Maven Plugin](https://github.com/allure-framework/allure-maven)
- [Примеры использования](https://github.com/allure-examples)

## 🎉 Итог

Allure Reports предоставляет:
- ✅ Красивые и информативные отчеты
- ✅ Детальную статистику выполнения
- ✅ Историю изменений
- ✅ Простую интеграцию с CI/CD
- ✅ Понятную визуализацию результатов

**Happy Testing!** 🚀
