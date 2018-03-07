trap error ERR

function error () {
	echo "=================================================="
	echo "            NOT GOOD, AN ERROR OCCURED"
	echo "=================================================="
	echo "an error occured"
	echo "dont forget to kill serverproccess"
	echo "kill -9 $pid"
	exit 1
}

param=""
rm rr-*.log

if [[ -z $1 ]]
then
	echo "STARTE SERVER NORMAL"
else
	if [[ $1 -eq "encoff" ]]
	then
		param="encoff"
		echo "Starte Server mit abgeschalteter Encryption Policy"
	else
		echo "PARAMETER NICHT ERKANNT"
		exit 1
	fi
fi

echo "build standalone server"
mvn clean install -DskipTests > /dev/null

echo "start standalone server"
java -jar docusafe-rest/target/docusafe-rest-0.1.0-SNAPSHOT.jar $param > documentsafe.console.out.log &
pid=$!
echo "pid ist $pid"

started=0
while (( started == 0 ))
do
	echo "$(date) wait for server to start"
	sleep 1
	started=$(grep "Started RestApplication" documentsafe.console.out.log | wc -l)
	failed=$(grep -i "application failed to start" documentsafe.console.out.log | wc -l)
	if (( failed == 1 )) 
	then
		echo "application can not start. see log file or documentsafe.console.out.log"
		error
	fi
done
echo "server is up"

./dorest.sh
./streamTest.sh

echo "kill standalone server"
kill $pid
