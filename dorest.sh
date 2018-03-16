#@IgnoreInspection BashAddShebang

trap error ERR

function error () {
	print "  A N    E R R O R    O C C U R R E D"
	exit 1
}
function print () {
	{
	echo "$(date) ==================================================================================="
	echo "$(date) $1"
	echo "$(date)  "
	} | tee -a curl.log
}

function checkGuards() {
	if [[ filesystem -ne 1 ]]
	then
		echo "no guards check, filesystem is not active"
		return
	fi
	user=$1
	expected=$2

	guardKeys=$(find target/filesystemstorage -type f |  grep "^target/filesystemstorage/BP-$user/.KEYSTORE" | grep bucketGuardKey | grep -v $META | wc -l)
	if (( guardKeys == expected )) 
	then
		echo "ok Anzahl von $user GuardKeys ist $expected.  Das ist fein." | tee -a curl.log
	else
		print "DANGER DANGER ACHTUNG FEHLER. ANZAHL DER GUARD KEYs von $user IST NICHT KORREKT expected $expected but was $guardKeys"
		exit 1;
	fi

	guards=$(find target/filesystemstorage -type f |  grep "^target/filesystemstorage/BP-$user/.KEYSTORE/KS-$user.DK.*" | grep -v $META | wc -l)
	if (( guards == expected )) 
	then
		echo "ok Anzahl von $user Guards ist $expected.  Das ist fein." | tee -a curl.log
	else
		print "DANGER DANGER ACHTUNG FEHLER. ANZAHL DER GUARD von $user IST NICHT KORREKT expected $expected but was $guards"
		exit 1
	fi
}

function checkCurl() {
	status=$1
	shift
	rm -f curl.out
	rm -f curl.error
	curl "$@" > curl.out 2>curl.error
	ret=$?
	if (( ret==0 )) 
	then
		cat curl.out >> curl.log
		httpStatus=$(cat curl.out | head -n 1 | cut -d$' ' -f2)
	else
		cat curl.error >> curl.log
		httpStatus=$(cat curl.error)
                httpStatus=$(echo ${httpStatus##*The requested URL returned error: })
	fi
	rm -f curl.out
	rm -f curl.error

	if [[ status -eq "any" ]]
	then
		echo "$httpStatus is ignored" | tee -a curl.log
	else
		if (( httpStatus!=status )) 
		then
			echo "expected status $status but was $httpStatus of cmd" | tee -a curl.log
			exit 1
		else
			echo "expected status was $httpStatus" | tee -a curl.log
		fi
	fi
}

filesystem=1
if [[ $# -eq 1 ]]
then
	filesystem=$1
fi

echo "DO REST FILESYSTEM ACTIVE: $filesystem"
META="._META-INFORMATION_"

rm -f curl.log
print "delete user, if exists, ignore error"
checkCurl any -X DELETE -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"peter", "readKeyPassword":"rkp"}'
checkCurl any -X DELETE -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"francis", "readKeyPassword":"passWordXyZ"}' 

print "create user peter"
checkCurl 200 -f -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"peter", "readKeyPassword":"rkp"}' 
checkGuards peter   1

print "check user peter exists"
checkCurl 200 -f -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -i 'http://localhost:8080/internal/user/peter'

print "check user francis does not exist yet"
checkCurl 404 -f -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -i 'http://localhost:8080/internal/user/francis'

print "peter gets README.txt of home dir"
checkCurl 200 -f -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i 'http://localhost:8080/document/%22README.txt%22' >> curl.log

print "peter saves deep document"
checkCurl 200 -f -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i http://localhost:8080/document --data '{
  "documentFQN": "deeper/and/deeper/README.txt",
  "documentContent": "AFFE"
}' 
checkGuards peter   2

print "peter gets deep document"
checkCurl 200 -f -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i 'http://localhost:8080/document/%22deeper/and/deeper/README.txt%22' 

print "peter links deep document"
checkCurl 200 -f -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i http://localhost:8080/document/link --data '{
  "source": "deeper/and/deeper/README.txt",
  "destination": "green/bucket/README.txt"
}' 

print "peter gets linked document"
checkCurl 200 -f -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i 'http://localhost:8080/document/%22green/bucket/README.txt%22' 
checkGuards peter   3

print "create user francis"
checkCurl 200 -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"francis", "readKeyPassword":"passWordXyZ"}' 
checkGuards francis 1

print "peter grants read permsission for  deeper/and/deeper to francis"
checkCurl 200 -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i http://localhost:8080/grant/document --data '{
  "documentDirectoryFQN": "deeper/and/deeper",
  "receivingUser": "francis",
  "accessType" : "READ"
}' 
checkGuards francis 2

print "francis liest deeper Document von Peter"
checkCurl 200 -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: francis' -H 'password: passWordXyZ' -i 'http://localhost:8080/granted/document/peter/%22/deeper/and/deeper/README.txt%22' 

print "francis tries to  save peters deeper document"
checkCurl 403 -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: francis' -H 'password: passWordXyZ' -i http://localhost:8080/granted/document/peter --data '{
  "documentFQN": "deeper/and/deeper/README.txt",
  "documentContent": "AFFEFE"
}' 

print "francis liest nicht existentes Document von Peter"
checkCurl 409 -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: francis' -H 'password: passWordXyZ' -i 'http://localhost:8080/granted/document/peter/%22/deeper/and/deeper/README-notexist.txt%22' 

print "peter grants write permsission for  deeper/and/deeper to francis"
checkCurl 200 -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i http://localhost:8080/grant/document --data '{
  "documentDirectoryFQN": "deeper/and/deeper",
  "receivingUser": "francis",
  "accessType" : "WRITE"
}' 
checkGuards francis 2

print "francis tries to  save peters deeper document"
checkCurl 200 -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: francis' -H 'password: passWordXyZ' -i http://localhost:8080/granted/document/peter --data '{
  "documentFQN": "deeper/and/deeper/README.txt",
  "documentContent": "AFFEFE"
}' 

print "peter removes grant permsission for  deeper/and/deeper from francis"
checkCurl 200 -X PUT -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i http://localhost:8080/grant/document --data '{
  "documentDirectoryFQN": "deeper/and/deeper",
  "receivingUser": "francis",
  "accessType" : "NONE"
}' 
checkGuards francis 1

print "francis tries to read deeper Document von Peter"
checkCurl 403 -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: francis' -H 'password: passWordXyZ' -i 'http://localhost:8080/granted/document/peter/%22/deeper/and/deeper/README.txt%22' 

if [[ filesystem -eq 1 ]]
then
	print "check filesystem"
	find target/filesystemstorage -type f >> curl.log
fi

checkGuards peter   3
checkGuards francis 1

print "peter gets README.txt of home dir"
checkCurl 200 -f -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i 'http://localhost:8080/document/%22README.txt%22' >> curl.log

print "peter deletes README.txt of home dir"
checkCurl 200 -f -X DELETE -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i 'http://localhost:8080/document/%22README.txt%22' >> curl.log 

print "peter expetects 404 for  README.txt of home dir"
checkCurl 404 -f -X GET -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'userid: peter' -H 'password: rkp' -i 'http://localhost:8080/document/%22README.txt%22' >> curl.log


print "EVERYTHING WENT FINE so FAR"

print "delete user"
# checkCurl -f -X DELETE -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"peter", "readKeyPassword":"rkp"}'
# checkCurl -f -X DELETE -H 'Content-Type: application/json' -H 'Accept: application/json' -i http://localhost:8080/internal/user --data '{"userID":"francis", "readKeyPassword":"passWordXyZ"}'
