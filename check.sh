echo "print check layer00"
find src/main/java/org/adorsys/documentsafe/layer00*/ -name '*.java' -exec grep "layer01\|layer02\|layer03" /dev/null {} \;
echo "print check layer01"
find src/main/java/org/adorsys/documentsafe/layer01*/ -name '*.java' -exec grep "layer02\|layer03" /dev/null {} \;
echo "print check layer02"
find src/main/java/org/adorsys/documentsafe/layer02*/ -name '*.java' -exec grep "layer03"  /dev/null {} \;
