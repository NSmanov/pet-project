# Playwright E2E Tests for Task Management System

## Описание

Этот каталог содержит end-to-end (E2E) тесты для Task Management System, написанные с использованием Playwright.

## Структура тестов

```
tests/
└── e2e/
    ├── tasks.spec.js        # Основные функциональные тесты
    └── performance.spec.js  # Тесты производительности и доступности
```

## Покрытие тестами

### tasks.spec.js - Основные функциональные тесты (13 тестов)

1. ✅ Загрузка приложения и отображение заголовка
2. ✅ Отображение панели статистики
3. ✅ Отображение существующих задач
4. ✅ Создание новой задачи
5. ✅ Редактирование существующей задачи
6. ✅ Удаление задачи
7. ✅ Фильтрация задач по статусу
8. ✅ Фильтрация задач по приоритету
9. ✅ Поиск задач по ключевому слову
10. ✅ Очистка всех фильтров
11. ✅ Отмена создания задачи
12. ✅ Валидация обязательных полей

### performance.spec.js - Производительность и доступность (10 тестов)

1. ✅ Производительность загрузки страницы
2. ✅ Отзывчивость на мобильных устройствах
3. ✅ Обработка ошибок API
4. ✅ Отображение пустого состояния
5. ✅ Правильная иерархия заголовков
6. ✅ Доступность форм (labels)
7. ✅ Отображение состояния загрузки
8. ✅ Обработка длинных заголовков задач
9. ✅ Навигация с клавиатуры
10. ✅ Сохранение фильтров во время сессии

**Всего: 23 E2E теста**

## Установка

```bash
# Установить зависимости
npm install

# Установить браузеры для Playwright
npx playwright install
```

## Запуск тестов

### Все тесты (headless mode)
```bash
npm run test:e2e
```

### Интерактивный UI режим
```bash
npm run test:e2e:ui
```

### С визуализацией браузера (headed mode)
```bash
npm run test:e2e:headed
```

### Режим отладки
```bash
npm run test:e2e:debug
```

### Запуск в конкретном браузере
```bash
npm run test:e2e:chromium  # Только Chrome
npm run test:e2e:firefox   # Только Firefox
npm run test:e2e:webkit    # Только Safari
```

### Просмотр отчета о последнем запуске
```bash
npm run test:e2e:report
```

## Конфигурация

Конфигурация тестов находится в `playwright.config.js`.

### Основные настройки:

- **baseURL**: `http://localhost:8080`
- **Timeout**: 30 секунд на тест
- **Retries**: 2 попытки в CI, 0 локально
- **Screenshots**: Только при ошибках
- **Video**: Сохраняется при ошибках
- **Trace**: Включается при первой повторной попытке

### Поддерживаемые браузеры:

- Chromium (Desktop)
- Firefox (Desktop)
- WebKit (Desktop Safari)
- Mobile Chrome (Pixel 5)
- Mobile Safari (iPhone 12)

## Требования к окружению

### Для запуска тестов необходимо:

1. **Запущенное приложение** на `http://localhost:8080`
   - Backend должен быть доступен
   - Frontend должен отвечать на запросы

2. **Playwright автоматически запустит dev server** если он еще не запущен
   - Настроено в `playwright.config.js` → `webServer`

### Для CI/CD:

```yaml
# Пример GitHub Actions workflow
- name: Install dependencies
  run: npm ci

- name: Install Playwright Browsers
  run: npx playwright install --with-deps

- name: Run E2E tests
  run: npm run test:e2e

- name: Upload test results
  if: always()
  uses: actions/upload-artifact@v3
  with:
    name: playwright-report
    path: playwright-report/
```

## Отчеты

После запуска тестов генерируются следующие отчеты:

- **HTML отчет**: `playwright-report/index.html`
- **JSON результаты**: `test-results/results.json`
- **Screenshots**: `test-results/` (при ошибках)
- **Videos**: `test-results/` (при ошибках)
- **Traces**: `test-results/` (при повторных попытках)

## Лучшие практики

### При написании новых тестов:

1. **Используйте data-testid** для надежных селекторов
2. **Группируйте тесты** с помощью `test.describe()`
3. **Используйте beforeEach** для общей настройки
4. **Обрабатывайте асинхронность** с помощью `await`
5. **Проверяйте видимость** элементов перед взаимодействием
6. **Используйте уникальные данные** (например, timestamp) для избежания конфликтов

### Пример:

```javascript
test('should create a task', async ({ page }) => {
  const uniqueTitle = `Task ${Date.now()}`;

  await page.click('[data-testid="create-button"]');
  await page.fill('[data-testid="task-title"]', uniqueTitle);
  await page.click('[data-testid="submit-button"]');

  await expect(page.locator(`text=${uniqueTitle}`)).toBeVisible();
});
```

## Устранение проблем

### Тесты падают с timeout

- Увеличьте timeout в `playwright.config.js`
- Проверьте, что приложение запущено и доступно
- Используйте `await page.waitForLoadState('networkidle')`

### Тесты проходят локально, но падают в CI

- Убедитесь, что браузеры установлены: `npx playwright install --with-deps`
- Проверьте переменные окружения
- Используйте `--headed` для отладки в CI

### Элементы не находятся

- Используйте Playwright Inspector: `npm run test:e2e:debug`
- Проверьте селекторы в DevTools
- Добавьте явные ожидания: `await expect(locator).toBeVisible()`

## Дополнительные ресурсы

- [Playwright Documentation](https://playwright.dev/)
- [Best Practices](https://playwright.dev/docs/best-practices)
- [Debugging Guide](https://playwright.dev/docs/debug)
- [CI Configuration](https://playwright.dev/docs/ci)
