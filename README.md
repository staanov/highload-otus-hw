# API для социальной сети в качестве домашнего задания по курсу ["Архитектор высоких нагрузок"](https://otus.ru/lessons/highloadarchitect/)

## Инструкция по установке и использованию:
1. Локально склонировать себе репозиторий
2. Убедиться, что установлены docker, docker-compose и Postman
3. Выполнить `docker-compose up -d` в корне репозитория
4. Убедиться, что контейнеры с приложением (`highload-otus-hw`) и базой данных (`mysqldb`) успешно поднялись
5. В случае ошибки `docker.credentials.errors.InitializationError: docker-credential-gcloud not installed or not available in PATH` удалить файл `~/.docker/config.json`
6. Открыть Postman и импортировать коллекцию, которая лежит в папке `postman` в корне репозитория
7. Можно "дёргать" API

## Доступные возможности:
- Неавторизованному пользователю доступны только два запроса: 
  - POST-запрос на регистрацию (`Register a new user`) 
  - POST-запрос на авторизацию по паролю (`Login`)

- Авторизованный пользователь может: 
  - посмотреть список всех пользователей (GET-запрос `Get all users`)
  - найти информацию о другом пользователе по его логину (GET-запрос `Get a user by login`)
  - добавить пользователя в друзья (POST-запрос `Add a friend`)
  - удалить пользователя из друзей (DELETE-запрос `Remove a friend`)
  - получить список друзей другого пользователя (GET-запрос `Get a user friends`)
  - посмотреть список своих друзей (GET-запрос `Get my friends (friends of authorized user)`)
  - выйти из системы (GET-запрос `Logout`)