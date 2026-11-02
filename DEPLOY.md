# Как запустить проект не на localhost

Схема: **приложение Android → backend (Docker-контейнер на хостинге) → PostgreSQL (Neon, данные не пропадают)**.

## 1. База данных (Neon, бесплатно, без срока действия)

1. Зарегистрируйтесь на <https://neon.com>, создайте проект (регион выберите ближе к вам).
2. Откройте **Connection details** и возьмите отдельные значения: **Host**, **Database**, **User**, **Password**.
   Берите обычный (direct) хост, не `...-pooler...`: пул соединений уже есть в приложении.
3. Таблицы создавать вручную не нужно: при первом запуске backend сам применит миграцию
   `backend/src/main/resources/db/migration/V1__init.sql`.

> На Render бесплатный Postgres удаляется через 30 дней (+14 дней отсрочки), поэтому для данных, которые должны жить, лучше Neon.

## 2. Backend (пример: Render)

1. На <https://render.com> выберите **New → Blueprint** и укажите репозиторий. Render прочитает `render.yaml`.
2. Заполните переменные, которые он попросит:

   | Переменная    | Значение                                      |
   |---------------|-----------------------------------------------|
   | `DB_HOST`     | Host из Neon (`ep-xxxx.<регион>.aws.neon.tech`) |
   | `DB_NAME`     | Database из Neon                              |
   | `DB_USER`     | User из Neon                                  |
   | `DB_PASSWORD` | Password из Neon                              |
   | `AUTH_SECRET` | создаётся автоматически (не меняйте без нужды, иначе все выйдут из аккаунта) |

3. После деплоя проверьте `https://<ваш-сервис>.onrender.com/actuator/health`: должно быть `{"status":"UP"}`.
   Документация API: `/swagger-ui.html`.

Ограничение бесплатного плана Render: сервис засыпает после 15 минут без запросов и просыпается около минуты,
а сам Render не рекомендует free-инстансы для production. Для постоянной работы возьмите платный план
или любой VPS (см. п. 4): контейнер собирается одинаково везде.

## 3. Android

В `android/gradle.properties` укажите адрес сервера (https, со слэшем в конце):

```
API_BASE_URL=https://<ваш-сервис>.onrender.com/
```

Нажмите **Sync Gradle** и соберите приложение. Пока там стоит `YOUR-SERVER`, приложение подскажет, что адрес не задан.
Обычный `http://` запрещён network security config; исключение только для эмулятора (`10.0.2.2`).
Для эмулятора + локальный backend раскомментируйте `API_BASE_URL_DEBUG=http://10.0.2.2:8080/`.

## 4. Свой сервер / локальный запуск (Docker)

```
cp .env.example .env      # заполните DB_PASSWORD и AUTH_SECRET (openssl rand -base64 48)
docker compose up -d --build
```

Данные Postgres лежат в томе `pgdata` и переживают перезапуски и пересборки. Бэкап:
`docker compose exec db pg_dump -U postgres buh3 > backup.sql`.

## 5. Перенос старых данных из локальной БД

```
pg_dump --no-owner --no-acl -h localhost -U postgres buh3 > old.sql
psql "postgresql://<user>:<password>@<host>/<db>?sslmode=require" < old.sql
```

Миграции Flyway поверх уже существующих таблиц безопасны. Пароли старых пользователей лежали открытым текстом:
при их первом входе сервер автоматически заменит их на BCrypt-хеш.

## Переменные окружения backend

| Переменная | По умолчанию | Назначение |
|---|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `5432` / `buh3` | подключение к PostgreSQL |
| `DB_USER` / `DB_PASSWORD` | `postgres` / пусто | учётные данные БД |
| `DB_PARAMS` | пусто | для Neon: `?sslmode=require` |
| `AUTH_SECRET` | случайный при каждом старте | ключ подписи токенов, **минимум 32 символа** |
| `AUTH_TOKEN_TTL_HOURS` | `720` | срок жизни токена |
| `SWAGGER_ENABLED` | `true` | `false` отключает Swagger на проде |
| `PORT` | `8080` | порт (хостинги задают сами) |
