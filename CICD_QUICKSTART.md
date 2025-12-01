# 🚀 CI/CD Quick Start Guide

## Быстрый старт за 5 минут

### Шаг 1: Настройте GitHub Secrets

```bash
# Repository → Settings → Secrets and Variables → Actions → New repository secret

COOLIFY_TOKEN=your_coolify_api_token_here
```

### Шаг 2: Настройте GitHub Variables

```bash
# Repository → Settings → Secrets and Variables → Actions → Variables

COOLIFY_URL=https://coolify.yourserver.com
STAGING_URL=https://staging.yourapp.com
PRODUCTION_URL=https://yourapp.com
PROJECT_NAME=task-management
```

### Шаг 3: Настройте GitHub Environments

```bash
# Repository → Settings → Environments

1. Create "staging" environment
   - No protection rules (auto-deploy)

2. Create "production" environment
   - Required reviewers: [ваш username]
   - Deployment branches: Only "main"
```

### Шаг 4: Push и наблюдайте магию!

```bash
# Деплой на staging
git checkout develop
git add .
git commit -m "feat: My awesome feature"
git push origin develop

# Pipeline автоматически:
# ✅ Валидирует конфигурации (AI)
# ✅ Собирает приложение
# ✅ Деплоит на staging
# ✅ Мониторит с Predictive Rollback

# Деплой на production
git checkout main
git merge develop
git push origin main
# → Ждите approval в GitHub Actions
# → Approve → Zero-downtime deployment!
```

---

## 📋 Проверочный список перед первым деплоем

### Обязательно:

- [ ] Добавлен `COOLIFY_TOKEN` в GitHub Secrets
- [ ] Созданы `staging` и `production` environments
- [ ] Настроен Coolify проект с именем из `PROJECT_NAME`
- [ ] Spring Boot Actuator включен в приложении

### Рекомендуется:

- [ ] Добавлен URL staging/production в Variables
- [ ] Протестирован AI validator локально: `python3 .github/scripts/ai-config-validator.py`
- [ ] Протестирован health monitor: `APP_URL=http://localhost:8080 .github/scripts/health-monitor.sh`

---

## 🎯 Основные команды

### Тестирование локально

```bash
# AI Validator
python3 .github/scripts/ai-config-validator.py

# Health Monitor (требует запущенное приложение)
docker-compose up -d
APP_URL="http://localhost:8080" .github/scripts/health-monitor.sh

# Predictive Rollback (симуляция)
python3 .github/scripts/predictive-rollback.py "http://localhost:8080" "test"
```

### Просмотр метрик деплоя

```bash
# GitHub → Actions → Latest workflow run → Summary
# Там будут все метрики:
# - Deployment Time
# - Health Checks results
# - Rollback status
```

---

## 🔥 Hot Tips

1. **Первый деплой может быть медленнее** (загрузка base images)
2. **Staging деплоится автоматически** при push в `develop`
3. **Production требует approval** в GitHub UI
4. **Rollback автоматический** если AI обнаружит проблемы
5. **Все метрики в GitHub Actions** → Summary

---

## 🆘 Нужна помощь?

Смотрите полную документацию: [CICD_DOCUMENTATION.md](./CICD_DOCUMENTATION.md)

Раздел Troubleshooting: [CICD_DOCUMENTATION.md#troubleshooting](./CICD_DOCUMENTATION.md#-troubleshooting)

---

## ✅ Проверка что всё работает

После первого деплоя проверьте:

```bash
# 1. Health endpoint
curl https://staging.yourapp.com/actuator/health

# Ожидаемый ответ:
# {"status":"UP"}

# 2. API работает
curl https://staging.yourapp.com/api/tasks

# 3. GitHub Actions успешно
# Зелёные галочки во всех jobs
```

---

**Готово! Ваш AI-powered CI/CD pipeline настроен!** 🎉
