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

file=docusafe-rest.client/target/dsc 
size=$(ls -sk $file | cut -f  2 -d " ")

# write stream and read bytes and stream ===================
# ==========================================================
print "$(date) write file stream oriented with size $size"
java -jar $file -ws $file

print "$(date) read file byte oriented with size $size"
java -jar $file -rb $file $file-as-bytes2
diff $file $file-as-bytes2

print "$(date) read file stream oriented with size $size"
java -jar $file -rs $file $file-as-stream2
diff $file $file-as-stream2

# write bytes and read bytes and stream ===================
# =========================================================
print "$(date) write file byte oriented with size $size"
java -jar $file -wb $file

print "$(date) read file byte oriented with size $size"
java -jar $file -rb $file $file-as-bytes
diff $file $file-as-bytes

print "$(date) read file stream oriented with size $size"
java -jar $file -rs $file $file-as-stream
diff $file $file-as-stream

print "STREAM TESTING SUCCESSFULL"
