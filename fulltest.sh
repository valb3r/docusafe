trap error ERR

function error () {
	echo "=================================================="
	echo "            NOT GOOD, AN ERROR OCCURED"
	echo "=================================================="
	echo "an error occured"
	echo "try to kill serverproccess"
	echo "kill -9 $pid"
 	kill -9 $pid
	exit 1
}

rm -f *.log

echo "build standalone server"
mvn clean package -DskipTests  > /dev/null

echo "start standalone server"
java -jar docusafe-rest/target/docusafe-rest-0.1.0-SNAPSHOT.jar $* > documentsafe.console.out.log &
pid=$!
echo "pid ist $pid"

started=0
while (( started == 0 ))
do
	echo "$(date) wait for server to start"
	sleep 1
	grep "WARN" documentsafe.console.out.log | xargs echo
	grep "ERROR" documentsafe.console.out.log | xargs echo
	started=$(grep "Started RestApplication" documentsafe.console.out.log | wc -l)
	failed=$(grep -i "application failed to start" documentsafe.console.out.log | wc -l)
	if (( failed == 1 )) 
	then
		echo "application can not start. see log file or documentsafe.console.out.log"
		error
	fi
done
echo "server is up"

filesystem=1
function testParams {
	for var in "$@"
	do
		if [[ $var == "mongodb" ]]
		then
			filesystem=0
		fi
		if [[ $var == "filesystem" ]]
		then
			filesystem=1
		fi
	done
}
testParams $*

echo filesystem $filesystem

./dorest.sh $filesystem
./streamTest.sh

echo "kill standalone server with pid $pid"
kill $pid
