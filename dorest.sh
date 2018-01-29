trap error ERR

function error () {
	echo "an error occured"
	exit 1
}

echo "delete user, if exists, ignore error"
curl -X DELETE -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"affe", "readKeyPassword":"rkp"}' > curl1.log

echo "create user"
curl -f -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"affe", "readKeyPassword":"rkp"}' > curl2.log

echo "get README.txt of home dir"
curl -f -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: affe' -H 'password: rkp' -i 'http://localhost:8080/document/%22README.txt%22' > curl3.log

echo "save deep document"
curl -f -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: affe' -H 'password: rkp' -i http://localhost:8080/document --data '{
  "documentFQN": "deeper/and/deeper/README.txt",
  "documentContent": {
    "value": [
    1,2,3,4,5,6,7,8
    ]
  }
}' > curl4.log

echo "get deep document"
curl -f -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: affe' -H 'password: rkp' -i 'http://localhost:8080/document/%22deeper/and/deeper/README.txt%22' > curl5.log

echo "link deep document"
curl -f -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: affe' -H 'password: rkp' -i http://localhost:8080/document/link --data '{
  "source": "deeper/and/deeper/README.txt",
  "destination": "green/bucket/README.txt"
}' > curl6.log

echo "get linked document"
curl -f -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: affe' -H 'password: rkp' -i 'http://localhost:8080/document/%22green/bucket/README.txt%22' > curl7.log

echo "--------------------- check filesystem ----------"
find target/filesystemstorage
echo "----------------------"
guards=$(find target/filesystemstorage -name '*bucketGuardKey' | wc -l)
if (( guards == 3 )) 
then
	echo "ok Anzahl der Guards ist 3.  Das ist fein."
else
	echo "DANGER DANGER ACHTUNG FEHLER. ANZAHL DER GUARDS IST NICHT KORREKT"
	exit 1;
fi

echo "EVERYTHING WENT FINE so FAR"

echo "delete user"
curl -f -X DELETE -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"affe", "readKeyPassword":"rkp"}' > curl.8.log
