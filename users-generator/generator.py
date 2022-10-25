import pandas as pd
import requests
import random
import string
from russian_names import RussianNames
from transliterate import translit


def generate_names(is_male):
    if is_male:
        person = RussianNames(count=1, patronymic=False,
                              transliterate=True, gender=1.0, output_type='dict').get_batch()[0]
        return person
    else:
        person = RussianNames(count=1, patronymic=False,
                              transliterate=True, gender=0.0, output_type='dict').get_batch()[0]
        return person


def generate_interests():
    interests_data = pd.read_csv('hobbies.csv', header=0)
    all_available_interests = list(interests_data.Hobbies)
    generated_interests = [all_available_interests[random.randrange(0, len(all_available_interests))],
                           all_available_interests[random.randrange(0, len(all_available_interests))]]
    return generated_interests


def generate_city():
    cities_data = pd.read_csv('cities.csv', header=0)
    all_available_cities = list(cities_data.city)
    city_ru = all_available_cities[random.randrange(0, len(all_available_cities))]
    city_en = translit(city_ru, "ru", reversed=True)
    return city_en


def generate_male_users(url):
    for i in range(500000):
        names = generate_names(True)
        first_name = names.get("name")
        last_name = names.get("surname")
        age = random.randrange(14, 71)
        login = first_name + last_name + str(age)
        password = ''.join(random.choices(string.ascii_letters + string.digits, k=20))
        gender = "MALE"
        interests = generate_interests()
        city = generate_city()

        payload = {
            'login': login,
            'password': password,
            'firstName': first_name,
            'lastName': last_name,
            'age': age,
            'gender': gender,
            'interests': interests,
            'city': city
        }

        requests.post(url, json=payload)


def generate_female_users(url):
    for i in range(500000):
        names = generate_names(False)
        first_name = names.get("name")
        last_name = names.get("surname")
        age = random.randrange(14, 71)
        login = first_name + last_name + str(age)
        password = ''.join(random.choices(string.ascii_letters + string.digits, k=20))
        gender = "FEMALE"
        interests = generate_interests()
        city = generate_city()

        payload = {
            'login': login,
            'password': password,
            'firstName': first_name,
            'lastName': last_name,
            'age': age,
            'gender': gender,
            'interests': interests,
            'city': city
        }

        requests.post(url, json=payload)


if __name__ == '__main__':
    # How to know the host?
    # Linux: sudo ip addr show docker0 (by default: http://172.17.0.1)
    # Windows/MacOS: Use http://host.docker.internal instead of http://localhost
    register_url = "http://172.17.0.1:8080/api/v1/auth/register"

    generate_male_users(register_url)
    print("Male users generation is over successfully")
    generate_female_users(register_url)
    print("Female users generation is over successfully")
