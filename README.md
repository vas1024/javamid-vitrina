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