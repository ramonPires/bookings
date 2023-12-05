# Running the tests
- `mvn clean test`

# Booking
### Create a booking:

```
curl --location --request POST 'localhost:8080/bookings' \
--header 'Content-Type: application/json' \
--data '{
    "guestFirstName": "Guest",
    "guestLastName": "Fancy",
    "guestAge": 18,
    "guestSocialSecurityId": "123456",
    "checkInDate": "2024-01-28",
    "checkOutDate": "2024-01-31",
    "propertyId": 1
}'
```

### Update a booking:
 ```
 curl --location --request PUT 'localhost:8080/bookings/1' \
--header 'Content-Type: application/json' \
--data '{
    "id": 1,
    "checkInDate": "2024-01-28",
    "checkOutDate": "2024-01-31",
    "guestFirstName": "Guest",
    "guestLastName": "Fancy",
    "guestAge": 22,
    "guestSocialSecurityId": "123456",
    "canceled": false,
    "propertyId": 1
}'
 ```

### Delete a booking:
```
curl --location --request DELETE 'localhost:8080/bookings/1'
```

### Cancel a booking:
```
curl --location --request DELETE 'localhost:8080/bookings/1/cancel'
```

### Rebook a canceled booking:
```
curl --location --request PUT 'localhost:8080/bookings/1/rebook' \
--header 'Content-Type: application/json' \
--data '{
    "id": 1,
    "checkInDate": "2024-02-12",
    "checkOutDate": "2024-02-15",
    "guestFirstName": "Guest",
    "guestLastName": "Fancy",
    "guestAge": 22,
    "guestSocialSecurityId": "123456",
    "propertyId": 1
}'
```

# Block
### Create a block:
```
curl --location --request POST 'localhost:8080/blocks' \
--header 'Content-Type: application/json' \
--data '{
    "propertyId": "1",
    "startDate": "2024-05-15",
    "endDate": "2024-05-20"
}'
```
### Update a block:
```
curl --location --request PUT 'localhost:8080/blocks/1' \
--header 'Content-Type: application/json' \
--data '{
    "propertyId": "1",
    "startDate": "2024-05-12",
    "endDate": "2024-05-15"
}'
```
### Delete a block:
```
curl --location --request DELETE 'localhost:8080/blocks/1'
```

