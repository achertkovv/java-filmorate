# java-filmorate
```mermaid
erDiagram
    users ||--o{ user_friends : following_user_id
    users ||--o{ user_friends : followed_user_id
    users ||--o{ user_films : user_id
    films ||--o{ user_films : film_id
    films ||--|{ film_genres : film_id
    genres ||--|{ film_genres : genre_id
    rating ||--|| films : rating_id

    films {
        integer id PK
        integer rating_id FK
        varchar name
        varchar description
        date release_date
        integer duration
    }
    users {
        integer id PK
        varchar email
        varchar login
        varchar name
        date birthday
    }
    rating {
        integer id PK
        varchar name
    }
    user_friends {
        integer following_user_id FK
        integer followed_user_id FK
        bool confirmed_friendship
    }
    user_films {
        integer user_id FK
        integer film_id FK
    }
    film_genres {
        integer film_id FK
        integer genre_id FK
    }
    genres {
        integer id PK
        varchar name
    }
```

# Примеры запросов

```sql
-- Рейтинги фильмов
select f."name", r."name" from films f
join rating r on f.rating_id = r.id;

-- Фильмы понравившиеся пользователям
select u."name", f."name" from user_films uf
join users u on uf.user_id = u.id
join films f on uf.film_id = f.id;

-- Жанры фильмов
select f."name", g."name" from film_genres fg
join films f on fg.film_id = f.id
join genres g on fg.genre_id = g.id;

-- Подтвержденные друзья
select u1."name", u2."name" from user_friends uf
join users u1 on uf.following_user_id = u1.id
join users u2 on uf.followed_user_id = u2.id
where uf.confirmed_friendship = true;

-- Пользователи, которые отправили запрос на добавление в "Друзья"
select u1."name", u2."name" from user_friends uf
join users u1 on uf.following_user_id = u1.id
join users u2 on uf.followed_user_id = u2.id
where uf.confirmed_friendship = false;
```