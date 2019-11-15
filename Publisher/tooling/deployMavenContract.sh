#!/bin/bash

OWNER_ADDRESS="0xa02df9004be3c4a20aeb50c459212412b1d0a58da3e1ac70ba74dde6b4accf4b"
PRIVATE_KEY="0xcc76648ce8798bc18130bc9d637995e5c42a922ebeab78795fac58081b9cf9d4"
JAR_PATH="mavenonipfs.jar"
NODE_ADDRESS="127.0.0.1:8545"

function require_success()
{
	if [ $1 -ne 0 ]
	then
                echo $1
		echo "Failed"
		exit 1
	fi
}

echo "Get transaction counts"
count=`./rpc.sh --get-tx-count "$OWNER_ADDRESS" "$NODE_ADDRESS"`
echo "Got count $count"

echo "Deploying the mavenonipfs contract..."
receipt=`./rpc.sh --deploy "$PRIVATE_KEY" "$count" "$NODE_ADDRESS" "$JAR_PATH" ""`
require_success $?

echo "Deployment returned receipt: \"$receipt\".  Waiting for deployment to complete..."
address=""
while [ "" == "$address" ]
do
	echo " waiting..."
	sleep 1
	address=`./rpc.sh --get-receipt-address "$receipt" "$NODE_ADDRESS"`
	require_success $?
done
echo "Deployed mavenonchain to address: \"$address\""
