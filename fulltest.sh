trap error ERR

function error () {
	echo "an error occured"
	exit 1
}

echo "build standalone server"
mvn clean install -DskipTests > /dev/null

echo "start standalone server"
java -jar target/documentsafe-1.0-SNAPSHOT.jar > documentsafe.console.out.log &
pid=$!
echo "pid ist $pid"

started=0
while (( started == 0 ))
do
	echo "$(date) wait for server to start"
	sleep 1
	started=$(grep "Started RestApplication" documentsafe.console.out.log | wc -l)
done
echo "server is up"

./dorest.sh

echo "kill standalone server"
kill $pid
