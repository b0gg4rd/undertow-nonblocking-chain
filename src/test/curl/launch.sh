#!/bin/sh
for i in {1..30}
do
  curl -X GET "http://localhost:8080/?timezone-id=10" -H "accept: application/json" &
  curl -X GET "http://localhost:8080/?timezone-id=10" -H "accept: application/json" &
  curl -X GET "http://localhost:8080/?timezone-id=10" -H "accept: application/json" &
  curl -X GET "http://localhost:8080/?timezone-id=10" -H "accept: application/json" &
  curl -X GET "http://localhost:8080/?timezone-id=10" -H "accept: application/json" &
done
