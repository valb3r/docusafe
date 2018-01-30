#@IgnoreInspection BashAddShebang
trap error ERR

function error () {
	print "an error occured"
	exit 1
}

function print () {
	{
	echo "==================================================================================="
	echo $1
	echo " "
	} | tee -a curl.log
}

function checkGuards() {
	user=$1
	expected=$2

	guardKeys=$(find target/filesystemstorage -type f |  grep "^target/filesystemstorage/BP-$user/.KEYSTORE" | grep bucketGuardKey | wc -l)
	if (( guardKeys == expected )) 
	then
		echo "ok Anzahl von $user GuardKeys ist $expected.  Das ist fein." | tee -a curl.log
	else
		print "DANGER DANGER ACHTUNG FEHLER. ANZAHL DER GUARD KEYs von $user IST NICHT KORREKT expected $expected but was $guardKeys"
		exit 1;
	fi

	guards=$(find target/filesystemstorage -type f |  grep "^target/filesystemstorage/BP-$user/.KEYSTORE/.*UBER." | wc -l)
	if (( guards == expected )) 
	then
		echo "ok Anzahl von $user Guards ist $expected.  Das ist fein." | tee -a curl.log
	else
		print "DANGER DANGER ACHTUNG FEHLER. ANZAHL DER GUARD von $user IST NICHT KORREKT expected $expected but was $guards"
		exit 1;
	fi
}

rm -f curl.log
print "delete user, if exists, ignore error"
curl -X DELETE -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"peter", "readKeyPassword":"rkp"}' >> curl.log
curl -X DELETE -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"francis", "readKeyPassword":"passWordXyZ"}' >> curl.log

print "create user"
curl -f -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"peter", "readKeyPassword":"rkp"}' >> curl.log

print "get README.txt of home dir"
curl -f -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i 'http://localhost:8080/document/%22README.txt%22' >> curl.log

print "save deep document"
curl -f -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i http://localhost:8080/document --data '{
  "documentFQN": "deeper/and/deeper/README.txt",
  "documentContent": {
    "value": [
    1,2,3,4,5,6,7,8
    ]
  }
}' >> curl.log

print "get deep document"
curl -f -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i 'http://localhost:8080/document/%22deeper/and/deeper/README.txt%22' >> curl.log

print "link deep document"
curl -f -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i http://localhost:8080/document/link --data '{
  "source": "deeper/and/deeper/README.txt",
  "destination": "green/bucket/README.txt"
}' >> curl.log

print "get linked document"
curl -f -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i 'http://localhost:8080/document/%22green/bucket/README.txt%22' >> curl.log

print "create user francis"
curl -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"francis", "readKeyPassword":"passWordXyZ"}' >> curl.log

print "grant peters deeper/and/deeper to francis"
curl -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i http://localhost:8080/grant/document --data '{
  "documentDirectoryFQN": "deeper/and/deeper",
  "receivingUser": "francis",
  "accessType" : "WRITE"
}' >> curl.log

print "francis liest Document von Peter"
curl -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: francis' -H 'password: passWordXyZ' -i 'http://localhost:8080/grant/document/peter/%22/deeper/and/deeper/README.txt%22' >> curl.log

print "check filesystem"
find target/filesystemstorage -type f >> curl.log

checkGuards peter   3
checkGuards francis 2

print "EVERYTHING WENT FINE so FAR"

print "delete user"
curl -f -X DELETE -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"peter", "readKeyPassword":"rkp"}' >> curl.log
curl -f -X DELETE -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"francis", "readKeyPassword":"passWordXyZ"}' >> curl.log
