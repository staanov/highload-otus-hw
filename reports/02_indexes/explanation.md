# Общие пояснения
- Графики latency и throughput до создания индекса находятся в папке `reports/02_indexes/before_index`
- Графики latency и throughput после создания индекса находятся в папке `reports/02_indexes/after_index`
- Нагрузочные тесты проводились в Apache Jmeter с использованием InfluxDB и Grafana
- В тестах проводились одновременные (concurrent) запросы от 1, 10, 100 и 1000 пользователей, соответственно, в течение 5 минут на каждый тест
- В тестах в каждом запросе в качестве префикса имени и префикса фамилии выбиралось по 1 случайной заглавной букве латинского алфавита (например, запрос SELECT * FROM user WHERE last_name LIKE 'L%' AND first_name LIKE 'S%' ORDER BY user_id;)

# Запрос добавления индекса
`CREATE INDEX prefix_reverse_idx ON user(last_name, first_name);`

где 
- user - таблица с данными о пользователе
- first_name - имя пользователя
- last_name - фамилия пользователя

# Explain запросов после индекса
mysql> `EXPLAIN SELECT * FROM user WHERE last_name LIKE 'I%' AND first_name LIKE 'A%' ORDER BY user_id;`

|**id**|**select_type**|**table**|**partitions**|**type**|**possible_keys**|**key**|**key_len**|**ref**|**rows**|**filtered**|**Extra**|
|---|---|---|---|---|---|---|---|---|---|---|---|
|  1 | SIMPLE      | user  | NULL       | range | prefix_reverse_idx,first_name_idx,last_name_idx,prefix_idx | prefix_reverse_idx | 804     | NULL | 28030 |    35.46 | Using index condition; Using filesort |

1 row in set, 1 warning (0.01 sec)

# Объяснение почему индекс именно такой
Рассчитаем селективность колонок first_name и last_name

mysql> `SELECT COUNT(DISTINCT first_name) FROM user;`

|COUNT(DISTINCT first_name)|
|---|
|101|

1 row in set (0.02 sec)

mysql> `SELECT COUNT(DISTINCT last_name) FROM user;`

|COUNT(DISTINCT last_name)|
|-------------------------|
| 166294                  |

1 row in set (0.35 sec)

mysql> `SELECT COUNT(*) FROM user;`

|COUNT(*)|
|-------|
|1000001|

1 row in set (0.08 sec)

Таким образом,

selectivity(first_name) = 101 / 1000001 = 0,000101

selectivity(last_name) = 166294 / 1000001 = 0,166293834

**можно считать, что селективность колонки last_name выше**, поэтому ставим ее раньше при создании индекса:
`CREATE INDEX prefix_reverse_idx ON user(last_name, first_name);`.

Еще можно создать 4 индекса:
- `CREATE INDEX first_name_idx ON user(first_name);`
- `CREATE INDEX last_name_idx ON user(last_name);`
- `CREATE INDEX prefix_idx ON user(first_name, last_name);`
- `CREATE INDEX prefix_reverse_idx ON user(last_name, first_name);`

и проверить explain'ом какой индекс будет использоваться на разных запросах:

mysql> `EXPLAIN SELECT * FROM user WHERE first_name LIKE 'A%' AND last_name LIKE 'I%' ORDER BY user_id;`

| id | select_type | table | partitions | type  | possible_keys                                              | key                | key_len | ref  | rows  | filtered | Extra                                 |
|---|---|---|------------|-------|------------------------------------------------------------|--------------------|---------|------|-------|----------|---------------------------------------|
|  1 | SIMPLE      | user  | NULL       | range | prefix_reverse_idx,first_name_idx,last_name_idx,prefix_idx | prefix_reverse_idx | 804     | NULL | 28030 |    35.46 | Using index condition; Using filesort |

1 row in set, 1 warning (0.00 sec)

mysql> `EXPLAIN SELECT * FROM user WHERE last_name LIKE 'I%' AND first_name LIKE 'A%' ORDER BY user_id;`

| id | select_type | table | partitions | type  | possible_keys                                              | key                | key_len | ref  | rows  | filtered | Extra                                 |
|----|-------------|-------|------------|-------|------------------------------------------------------------|--------------------|---------|------|-------|----------|---------------------------------------|
|  1 | SIMPLE      | user  | NULL       | range | prefix_reverse_idx,first_name_idx,last_name_idx,prefix_idx | prefix_reverse_idx | 804     | NULL | 28030 |    35.46 | Using index condition; Using filesort |

1 row in set, 1 warning (0.00 sec)

mysql> `EXPLAIN SELECT * FROM user WHERE first_name LIKE 'P%' AND last_name LIKE 'O%' ORDER BY user_id;`

| id | select_type | table | partitions | type  | possible_keys                                              | key                | key_len | ref  | rows  | filtered | Extra                                 |
|----|-------------|-------|------------|-------|------------------------------------------------------------|--------------------|---------|------|-------|----------|---------------------------------------|
|  1 | SIMPLE      | user  | NULL       | range | prefix_reverse_idx,first_name_idx,last_name_idx,prefix_idx | prefix_reverse_idx | 804     | NULL | 31668 |     5.70 | Using index condition; Using filesort |

1 row in set, 1 warning (0.01 sec)

mysql> `EXPLAIN SELECT * FROM user WHERE last_name LIKE 'O%' AND first_name LIKE 'P%' ORDER BY user_id;`

| id | select_type | table | partitions | type  | possible_keys                                              | key                | key_len | ref  | rows  | filtered | Extra                                 |
|----|-------------|-------|------------|-------|------------------------------------------------------------|--------------------|---------|------|-------|----------|---------------------------------------|
|  1 | SIMPLE      | user  | NULL       | range | prefix_reverse_idx,first_name_idx,last_name_idx,prefix_idx | prefix_reverse_idx | 804     | NULL | 31668 |     5.70 | Using index condition; Using filesort |

1 row in set, 1 warning (0.00 sec)

mysql> `EXPLAIN SELECT * FROM user WHERE first_name LIKE 'E%' AND last_name LIKE 'L%' ORDER BY user_id;`

| id | select_type | table | partitions | type  | possible_keys                                              | key                | key_len | ref  | rows  | filtered | Extra                                 |
|----|-------------|-------|------------|-------|------------------------------------------------------------|--------------------|---------|------|-------|----------|---------------------------------------|
|  1 | SIMPLE      | user  | NULL       | range | prefix_reverse_idx,first_name_idx,last_name_idx,prefix_idx | prefix_reverse_idx | 804     | NULL | 83272 |    13.21 | Using index condition; Using filesort |

1 row in set, 1 warning (0.01 sec)

mysql> `EXPLAIN SELECT * FROM user WHERE last_name LIKE 'L%' AND first_name LIKE 'E%' ORDER BY user_id;`

| id | select_type | table | partitions | type  | possible_keys                                              | key                | key_len | ref  | rows  | filtered | Extra                                 |
|----|-------------|-------|------------|------|------------------------------------------------------------|--------------------|---------|------|-------|----------|---------------------------------------|
|  1 | SIMPLE      | user  | NULL       | range | prefix_reverse_idx,first_name_idx,last_name_idx,prefix_idx | prefix_reverse_idx | 804     | NULL | 83272 |    13.21 | Using index condition; Using filesort |

1 row in set, 1 warning (0.00 sec)

Естественно, при нагрузочном тестировании лишние индексы удалялись и проверялся только индекс prefix_reverse_idx.