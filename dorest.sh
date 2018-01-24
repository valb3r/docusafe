echo "create user"
curl -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"affe", "readKeyPassword":"rkp"}'

echo "get README.txt of home dir"
curl -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: affe' -H 'password: rkp' -i 'http://localhost:8080/document/%22README.txt%22'

echo "delete user"
curl -X DELETE -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"affe", "readKeyPassword":"rkp"}'
