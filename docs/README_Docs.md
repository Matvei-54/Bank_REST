# Документация

Все эндпоинты, кроме /auth/login и /customer/registration, требуют JWT-токен в заголовке Authorization: Bearer <token>.

Операции, изменяющие состояние (POST, PUT, DELETE), требуют заголовок Idempotency-Key для предотвращения дублирования запросов.

Формат данных: JSON

 **Для доступа к защищенным эндпоинтам необходимо получить JWT-токен через эндпоинт /auth/login.**

Authorization: Bearer <your_jwt_token>

Тело запроса (JSON):

    {
        "email": "user@example.com",
        "password": "securePassword123"
    }

Ответ:

    {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }

---

 **Создать новую карту может только админитсратор.**
POST /admin/cards/create

Учетная записи администратора создается миграцией скриптов БД.
данные для входа:

    email: admin@syscard.com
    password: admin-password

Тело запроса (JSON):

    {
    "cardNumber": "1234567890123456",
    "expiryDate": "2025-12-31",
    "customerId": 3
    }

 **Все входные данные валидируются.**

 Номер карты должен быть строкой из 16 цифр (например, 1234567890123456).

 **Используйте уникальный UUID для Idempotency-Key в каждом изменяющем запросе.**
