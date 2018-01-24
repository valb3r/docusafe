echo "create user"
curl -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"affe", "readKeyPassword":"rkp"}'

echo "get README.txt of home dir"
curl -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: affe' -H 'password: rkp' -i 'http://localhost:8080/document/%22README.txt%22'

echo "speichere ein weiteres dokument"
curl -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: affe' -H 'password: rkp' -i http://localhost:8080/document --data '{
  "documentFQN": "deeper/and/deeper/README.txt",
  "documentContent": {
    "value": [
    1,2,3,4,5,6,7,8
    ]
  }
}'

echo "get README.txt of home dir"
curl -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: affe' -H 'password: rkp' -i 'http://localhost:8080/document/%22deeper/and/deeper/README.txt%22'

echo "delete user"
curl -X DELETE -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"affe", "readKeyPassword":"rkp"}'
