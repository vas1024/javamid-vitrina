# javamid-vitrina

```text

order_items       orders                users              baskets                 basket_items         products
+---------+      +---------------+      +-----------+      +----------------+      +-------------+      +-------------+
| id      |      | id            |      | id        |      | id             |      | id          |      | id          |
| user_id | 1<-N | user_id       | 1<-N | name      | 1->1 | user_id        | 1->N | basket_id   | 1->N | image       |
| name    |      | order_item_id |      | basket_id | 1<-1 | basket_item_id | 1<-N | product_id  |      | name        |
| image   |      |               |      |           |      |                |      | quantity    |      | description |
| price   |      +---------------+      +-----------+      +----------------+      +-------------+      | price       |
| user_id |                                                                                             |             |
+---------+                                                                                             +-------------+


```



я решил, что в заказах надо хранить исторические данные, ведь у продукта может поменяться, например, цена, или продукт может вообще закончиться. а в заказе должны быть сохранены данные на момент заказа.


на вторую часть ( контроллеры ) у меня ушло 4 дня((
чаще всего приходилось бороться с ленивой загрузкой типа такой ошибки org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role:
javamid.vitrina.dao.User.orders: could not initialize proxy - no Session
иногда связанный список объектов просто оказывался пустым безо всяких ошибок.
