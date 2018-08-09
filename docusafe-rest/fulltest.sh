trap error ERR INT

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
mvn -f ../pom.xml clean package -DskipTests  > /dev/null

echo "start standalone server"
java -jar ../docusafe-rest/target/docusafe-rest.jar -ERASE_DATABASE $* > documentsafe.console.out.log &
pid=$!
echo "pid ist $pid"

limit=30
counter=0
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
	let counter=counter+1
	if (( counter == limit )) 
	then
		echo "maximale Wartezeit von $limit Sekunden ueberschritten"
		error
	fi
	echo "gewartet bis jetzt $counter Sekunden"
done
echo "server is up"

filesystem=1
function testParams {
	for var in "$@"
	do
		if [[ $var == -DSC-MINIO* ]] 
		then 
			filesystem=0
		fi
		if [[ $var == -DSC-MONGO* ]] 
		then 
			filesystem=0
		fi
		if [[ $var == -DSC-FILESYSTEM* ]]
		then
			filesystem=1
		fi
	done
}
testParams $*

echo filesystem $filesystem

./dorest.sh $filesystem
echo "warte nun 60 sekunden"
./streamTest.sh
./streamByteTest.sh

echo "kill standalone server with pid $pid"
kill $pid
