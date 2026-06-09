#!/bin/bash

KEY="SanjayIsKumar"
#echo $KEY
EXPECTED=$(echo -n $KEY | sha256sum | awk '{print $1}')

while true
do

output=$(ncat -l 40123 --verbose 2>&1)
ipsender=$(echo "$output" | sed -n '4p' | grep -oP '(?<=Connection from )[0-9.]+')
received=$(echo "$output" | sed -n '5p')
#echo "Received: $received"

if [[ "$received" == "$EXPECTED" ]]; then
   #echo "success"
    echo "SHUTDOWN-INITIATED-SUCCESSFULLY" | ncat "$ipsender" 40123
    shutdown now
    break
else
    echo "failed" > /dev/null
fi

done
