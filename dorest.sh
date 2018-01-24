trap error ERR

function error () {
	echo "an error occured"
	exit 1
}

echo "delete user, if exists, ignore error"
curl -X DELETE -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"affe", "readKeyPassword":"rkp"}'

echo "create user"
curl -f -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"affe", "readKeyPassword":"rkp"}'

echo "get README.txt of home dir"
curl -f -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: affe' -H 'password: rkp' -i 'http://localhost:8080/document/%22README.txt%22'

echo "save deep document"
curl -f -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: affe' -H 'password: rkp' -i http://localhost:8080/document --data '{
  "documentFQN": "deeper/and/deeper/README.txt",
  "documentContent": {
    "value": [
    1,2,3,4,5,6,7,8
    ]
  }
}'

echo "get deep document"
curl -f -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: affe' -H 'password: rkp' -i 'http://localhost:8080/document/%22deeper/and/deeper/README.txt%22'

echo "link deep document"
curl -f -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: affe' -H 'password: rkp' -i http://localhost:8080/document/link --data '{
  "source": "deeper/and/deeper/README.txt",
  "destination": "green/bucket/README.txt"
}'

echo "get linked document"
curl -f -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: affe' -H 'password: rkp' -i 'http://localhost:8080/document/%22green/bucket/README.txt%22'

echo "delete user"
curl -f -X DELETE -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"affe", "readKeyPassword":"rkp"}'
