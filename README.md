# javamid-vitrina

```text
vitrina-main

order_items            orders                 users              baskets                 basket_items         products
+------------+         +---------------+      +-----------+      +----------------+      +-------------+      +-------------+
| id         |         | id            |      | id        |      | id             |      | id          |      | id          |
| user_id    |    1<-N | user_id       | 1<-N | name      | 1->1 | user_id        | 1->N | basket_id   | 1->N | image       |
| name       |         | order_item_id |      | basket_id | 1<-1 | basket_item_id | 1<-N | product_id  |      | name        |
| image      |         |               |      | login     |      |                |      | quantity    |      | description |
| price      |         +---------------+      | password  |      +----------------+      +-------------+      | price       |
| user_id    |                                +-----------+                                                                |             |
| quantity   |                                                                                                +-------------+
| product_id |                                                                                             
+------------+                                                                                             


rest-service

balance                           payments                             
+-----------------+               +-----------------+                  
| id              |               | id              | 
| user_id         |               | amount          | 
| amount          |               | user_id         | 
|                 |               | created_at      | 
+-----------------+               | order_signature | 
                                  |                 | 
                                  +-----------------+ 


```



запуск приложения
для работы приложения необходимо, чтобы были запущены локально, в докере или где-либо в сети 
(в этом случае потребутся настройки в application.properties) следующие приложения
- redis
- keycloak

тестировалось всё на докер контейнерах
docker run --name redis-test -it --rm -p 6379:6379 redis:7.4.2-bookworm sh -c "redis-server --daemonize yes && redis-cli"
docker run -d -p 8082:8080 --name keycloak -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.1.3 start-dev


настройка основного приложения как клиента в keycloak:
основное приложение vitrina-main является клиентом в keycloak, пользователей нет, ролей нет, скоуп не используется,
только authenticated or not
в кейклоаке в веб консоли http://127.0.0.1:8082/ ( в виндовсе тут не работает localhost )
создать клиента:
Client ID Vitrina
Client authentication	On
Standard flow
Direct access greants
Service accounts roles
Credentials -> Client secret  - отсюда взять секрет
генерацию токенов можно проверить курлом, подставив свой client_secret
curl -X POST "http://localhost:8082/realms/master/protocol/openid-connect/token" -H "Content-Type: application/x-www-form-urlencoded" -d "client_id=vitrina&client_secret=78Zi4hnhNrcSFGwWMlvy9468SHUcoWjy&grant_type=client_credentials"
в основном приложении в application.properties правильно прописать
spring.security.oauth2.client.provider.keycloak.issuer-uri=http://localhost:8082/realms/master
spring.security.oauth2.client.registration.keycloak.client-id=vitrina
spring.security.oauth2.client.registration.keycloak.client-secret=78Zi4hnhNrcSFGwWMlvy9468SHUcoWjy
spring.security.oauth2.client.registration.keycloak.scope=openid,profile,email
spring.security.oauth2.client.registration.keycloak.authorization-grant-type=client_credentials

рест сервис является сервером ресурсов, ему нужен коннект к кейклоаку только для получения публичных ключей для декодирования токенов,
таким образом, для него нет настроек в кейклоаке
в application.properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8082/realms/master



раслределение портов:
8080 - приложение Витрина
8081 - рест сервис платежей
8082 - keycloak
6379 - db redis


тестовые пользователи:
в основном приложении: 
создаются тестовые пользователи в классе DataInitializer (можно там еще добавить)
admin admin
user1 aaa
user2 bbb
user3 ccc
пользователи создаются вместе с корзиной.
у пользователя admin роль ROLE_ADMIN, у всех остальных ROLE_USER .
это закардкожено в классе User в методе getAuthorities()
в рестсервисе: 
в schema.sql при первом старте создаются десять счетов для user_id 1..10 с 100000 на балансе



защита - security
- в классе SecurityConfig защищены эндпоинты
- в каждом методе контроллера, который должен обслуживать только аутентифицированных пользователей,
стоит аннотация @PreAuthorize("isAuthenticated()")
- в методе @GetMapping("/orders/{id}") добавлена проверка в код ( currentUserId() == findOrderById.userId ) 
чтобы пользователи не могли видеть заказы друг друга.
- вызов рестсервиса для проверки баланса и совершения платежа защищен
OAuth in keycloak, PaymentService в vithina-main получает в кейклоак токен и посылает его в заголовке запроса
рестсервис проверяет токен
- 
csrf - отключена во всем проекте
никак не удалось заставить работать с POST multupart data type
не удалось частично отключить для конкретных ендпоинтов в реактивном вебфлюксе




проект был разделен на 2 подпроекта
rest-service
vitrina-main
в каждом своя БД H2

подпроект rest-service

для того, чтобы избежать проблем с повторной оплатой заказа в случае сбоев, я придумал писать некий хеш заказа - order_signature
но так и не реализовал.

для создания сервиса была создана спека openApi
лежит в ресурсах подпроекта с сервисом.
по ней был сгенерирован код для серверной части ( рест сервис )
важно, нельзя запускать генератор как maven goal, он тогда не читает конфигурацию из пом,
надо mvn clean compile

сгенерированный код был скопирован из target/generated-sources в src
домучен до работающего состояния.

после этого был сгенерирован код клиентской части ( для основного приложения )
в качестве источника был урл http://localhost:8081/v3/api-docs

в клиентском коде не удалось отключить аутентификацию. вот какие файлы взял из сгенеренных + auth :
org/openapitools/client/
├── ApiClient.java                 ← Основной клиент (обязательно)
├── JavaTimeFormatter.java         ← Форматирование дат (теперь знаем, что он нужен!)
├── api/
│   ├── BalanceApi.java            ← API для работы с балансом  
│   └── PaymentApi.java            ← API для платежей  
└── model/
    ├── Balance.java               ← Модель баланса  
    ├── PaymentRequest.java        ← Модель запроса платежа  
    └── PaymentResponse.java       ← Модель ответа платежа

в итоге у rest service получились следующие endpoints
http://localhost:8081/payment/balance/{id}
http://localhost:8081/payment
http://localhost:8081/v3/api-docs



подпроект vitrina-main

редис запускается на локальном хосте в докер контейнере с удобным доступом в cli:
docker run --name redis-test -it --rm -p 6379:6379 redis:7.4.2-bookworm sh -c "redis-server --daemonize yes && redis-cli"

реализовано кеширование продуктов на основной странице /main/images
сохраняется вся страница, ключ составляется из ключевого слова для поиска, 
метода сортировки, номера страницы, размера страницы. например  "products::NO:0:10"

картинки товаров кешируются отдельно как byte[] ключем является product id/




для загрузки товаров в базу написана админка, 
которую надо вызвать по адресу 8080:/admin/upload
ей надо скормить zip архив содержащий файлы с картинками и csv файл с записями. возможно отсутствие картинок.
формат файла вот такой:
name, image, description, price
"Ноутбук Lenovo", , "нетипичный товар", 55000
"Книга Spring Boot",, "очень интересная книжка", 1200
"кепка", "cap1.jfif", "кепка типа бейсболка", 500
"eщё кепка", "cap2.jfif", "кепка типа бейсболка", 510
пример для загрузки товаров лежит в папке src/main/resources/static/example

при разработке использовал БД H2 в файле, в папке проекта

использовал готовые шаблоны Thymeleaf

по сути все реактивные методы написаны дипсиком ((

не удалось передать флеш-параметр newOrder при редиректе по кнопке post buy

тестов наверное, маловато, но нигде не сказано, на сколько % надо покрыть.