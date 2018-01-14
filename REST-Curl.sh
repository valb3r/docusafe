echo "json ------"
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:8080/info
echo ""

echo "xml--------"
curl -i -H "Accept: application/xml" -H "Content-Type: application/xml" -X GET http://localhost:8080/info
echo ""
