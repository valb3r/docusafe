echo "json ------"
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:8080/info
echo ""

echo "json ------"
curl -X PUT -H "Accept: application/json" -H "Content-Type: application/json" -d '{"info": "affe","documentKeyID": "123","documentLocation": {"documentID": "id","documentBucketPath": "bucket/1/2/3"}' http://localhost:8080/put
echo ""
