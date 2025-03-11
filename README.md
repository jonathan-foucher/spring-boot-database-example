## Introduction
This project is an example of database interactions with Spring Boot.

## Run the project
### Database
Install postgres locally or run it through docker with :
```
docker run -p 5432:5432 -e POSTGRES_DB=movie_db -e POSTGRES_USER=user -e POSTGRES_PASSWORD=user postgres
```

### Application
Once the postgres database has started, you can launch the Spring Boot project and try it out.

#### Movie controller

Get a movie by id
```
curl --request GET \
  --url http://localhost:8090/database-example/movies/1
```

Get all movies (with pagination)
```
curl --request GET \
  --url 'http://localhost:8090/database-example/movies?page=0&size=10'
```

Get all movies (with pagination and optional parameters `updated_since` and/or `released_after`)
```
curl --request GET \
  --url 'http://localhost:8090/database-example/movies?updated_since=2025-03-11%2021%3A58%3A34.358%20%2B0100&released_after=2021-02-02&page=0&size=10'
```

Get all movie director links
```
curl --request GET \
  --url http://localhost:8090/database-example/movies/directors/links
```

Get all flat movie director objects
```
curl --request GET \
  --url http://localhost:8090/database-example/movies/directors
```

Save a movie
```
curl --request POST \
  --url http://localhost:8090/database-example/movies \
  --header 'content-type: application/json' \
  --data '{
  "director_id": 1,
  "title": "some movie",
  "release_date": "2020-02-02"
}'
```

Delete a movie by id
```
curl --request DELETE \
  --url http://localhost:8090/database-example/movies/1 \
  --header 'content-type: application/json'
```

#### Director controller

Get a director by id
```
curl --request GET \
  --url http://localhost:8090/database-example/directors/1
```

Get all directors ordered by last name and first name
```
curl --request GET \
  --url http://localhost:8090/database-example/directors/ordered
```

Get all directors by last name
```
curl --request GET \
  --url 'http://localhost:8090/database-example/directors?last_name=Doe'
```

Save a director
```
curl --request POST \
  --url http://localhost:8090/database-example/directors \
  --header 'content-type: application/json' \
  --data '{
  "first_name": "John",
  "last_name": "Doe"
}'
```

Delete a director by id
```
curl --request DELETE \
  --url http://localhost:8090/database-example/directors/1
```
