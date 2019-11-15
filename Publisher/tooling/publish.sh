#!/bin/bash

CONTRACT_ADDRESS="0xa01f53d4e4521941c68ba3570f0a4f1618363ceed09b45fa715b230eaae1a232"
OWNER_ADDRESS="0xa02df9004be3c4a20aeb50c459212412b1d0a58da3e1ac70ba74dde6b4accf4b"
PRIVATE_KEY="0xcc76648ce8798bc18130bc9d637995e5c42a922ebeab78795fac58081b9cf9d4"
NODE_ADDRESS="127.0.0.1:8545"
TOOLS_JAR=mavenonipfsTool.jar
PUBLISHER_JARS="Publisher.jar:../../lib/*"

function require_success()
{
        if [ $1 -ne 0 ]
        then
                echo $1
                echo "Failed"
                exit 1
        fi
}

function wait_for_receipt()
{
        receipt="$1"
        result="1"
        while [ "1" == "$result" ]
        do
                echo " waiting..."
                sleep 1
                `./rpc.sh --check-receipt-status "$receipt" "$NODE_ADDRESS"`
                result=$?
                if [ "2" == "$result" ]
                then
                        echo "Error"
                        exit 1
                fi
        done
}

# To execute this script must have 5 arguments.
if [ $# -ne 5 ]
then
    echo 'Incorrect number of arguments given!, it should have 5 arguments'
    echo './publish.sh [filename] [groupId] [artifactId] [version] [type]'
    exit 1
fi

echo "Push file $1 to the IPFS server..."
CID="$(java -cp $PUBLISHER_JARS mavenonipfs.publisher.Publisher "--p" "$1")"
echo "IPFS node returns $CID"


echo "Get transaction counts"
count=`./rpc.sh --get-tx-count "$OWNER_ADDRESS" "$NODE_ADDRESS"`
echo "Got count $count"


echo "Sending publish call..."
# publish(String groupId, String artifactId, String version, String type, String cid)
callPayload="$(java -cp $TOOLS_JAR cli.ComposeCallPayload "publish" "$2" "$3" "$4" "$5" "$CID")"
receipt=`./rpc.sh --call "$PRIVATE_KEY" "$count" "$CONTRACT_ADDRESS" "$callPayload" "0" "$NODE_ADDRESS"`
echo "$receipt"
require_success $?

echo "Transaction returned receipt: \"$receipt\".  Waiting for transaction to complete..."
wait_for_receipt "$receipt"
echo "Transaction completed"
