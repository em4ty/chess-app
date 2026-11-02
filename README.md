# Chess App — обучающее шахматное приложение

Android-приложение для изучения шахмат с сервером на Java и базой данных PostgreSQL.

- **Обучение:** фигуры и их ходы, правила.
- **Тест:** 15 вопросов по основам шахмат.
- **Упражнения:** 3 раздела по 5 уровней на доске: защита, атака, мат в 1 ход (играют чёрные).
- **Прогресс:** результаты хранятся на сервере и привязаны к аккаунту.

## Как это устроено

```
Android-приложение (Kotlin)  →  Backend (Java, Spring Boot)  →  PostgreSQL (Neon)
        телефон                        Render (Docker)               облако
```

| Часть | Технологии |
|---|---|
| `android/` | Kotlin, Retrofit, OkHttp, ViewBinding |
| `backend/` | Java 17, Spring Boot 4, Spring Data JPA, Flyway, BCrypt, Swagger (springdoc) |
| База данных | PostgreSQL (Neon), схема создаётся миграциями Flyway |
| Хостинг | Docker-контейнер (Render или любой VPS) |

## Структура репозитория

```
android/                  Android-приложение
backend/                  Spring Boot сервер
  src/main/resources/db/migration/   SQL-миграции базы данных
  Dockerfile              сборка и запуск контейнера
docker-compose.yml        локальный запуск: backend + PostgreSQL
render.yaml               настройки деплоя на Render
.env.example              шаблон переменных окружения
DEPLOY.md                 подробная инструкция по запуску не на localhost
```

## API

Документация (Swagger) открывается на работающем сервере: `/swagger-ui.html`.
Проверка состояния: `/actuator/health`.

| Метод | Адрес | Описание | Токен |
|---|---|---|---|
| POST | `/api/auth/register` | регистрация (логин от 3 символов, пароль от 6, email необязателен) | нет |
| POST | `/api/auth/login` | вход, возвращает токен и статистику | нет |
| GET | `/api/stats/{userId}` | статистика пользователя | да |
| POST | `/api/stats/{userId}/quiz` | сохранить результат теста (0–15), хранится лучший | да |
| POST | `/api/stats/{userId}/exercise` | отметить упражнение пройденным (тип и уровень) | да |

Токен передаётся в заголовке `Authorization: Bearer <token>`. Пользователь видит только свою статистику.
Ошибки приходят в формате `{"error": "..."}` с понятным HTTP-кодом (400, 401, 403, 404, 409).

## Быстрый старт

### 1. Запуск backend локально (Docker)

```
cp .env.example .env     # заполните DB_PASSWORD и AUTH_SECRET
docker compose up -d --build
```

Сервер: <http://localhost:8080>. Данные PostgreSQL хранятся в томе `pgdata` и не пропадают при перезапуске.

### 2. Запуск backend без Docker

Нужны Java 17+, Maven и доступная PostgreSQL. Задайте переменные окружения (см. таблицу ниже) и выполните:

```
cd backend
mvn spring-boot:run
```

### 3. Запуск приложения

1. Откройте папку `android` в Android Studio.
2. В `android/gradle.properties` укажите адрес сервера (https, со слэшем в конце):
   ```
   API_BASE_URL=https://<ваш-сервис>.onrender.com/
   ```
   Для эмулятора и локального backend: `API_BASE_URL_DEBUG=http://10.0.2.2:8080/`.
3. Sync Gradle и запуск.

### 4. Публикация на хостинг

Полная инструкция (Neon + Render) — в [DEPLOY.md](DEPLOY.md).

## Переменные окружения backend

| Переменная | По умолчанию | Назначение |
|---|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `5432` / `buh3` | подключение к PostgreSQL |
| `DB_USER` / `DB_PASSWORD` | `postgres` / пусто | учётные данные БД |
| `DB_PARAMS` | пусто | для облачных БД: `?sslmode=require` |
| `AUTH_SECRET` | случайный при каждом старте | ключ подписи токенов, минимум 32 символа |
| `AUTH_TOKEN_TTL_HOURS` | `720` | срок жизни токена |
| `SWAGGER_ENABLED` | `true` | `false` отключает Swagger |
| `PORT` | `8080` | порт сервера |

Секреты (пароль БД, `AUTH_SECRET`) никогда не хранятся в репозитории: файл `.env` добавлен в `.gitignore`.

## Сборка APK

В Android Studio: **Build → Generate App Bundles or APKs → Generate APKs**, вариант **release**.
Готовый файл публикуется в разделе **Releases** на GitHub. Ключ подписи (`.jks`) в репозиторий не добавляется.

## Известные ограничения

- На бесплатном тарифе Render сервис засыпает после 15 минут без запросов; первый запрос после простоя занимает около минуты.
- Шахматная доска в упражнениях не проверяет шах, рокировку, взятие на проходе и превращение пешки.
- Нет ограничения частоты попыток входа.
