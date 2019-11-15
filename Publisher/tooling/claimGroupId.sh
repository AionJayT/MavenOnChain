#!/bin/bash

CONTRACT_ADDRESS="0xa01f53d4e4521941c68ba3570f0a4f1618363ceed09b45fa715b230eaae1a232"
OWNER_ADDRESS="0xa02df9004be3c4a20aeb50c459212412b1d0a58da3e1ac70ba74dde6b4accf4b"
PRIVATE_KEY="0xcc76648ce8798bc18130bc9d637995e5c42a922ebeab78795fac58081b9cf9d4"
NODE_ADDRESS="127.0.0.1:8545"
TOOLS_JAR=mavenonipfsTool.jar

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

# To execute this script must have 1 arguments.
if [ $# -ne 1 ]
then
    echo 'Incorrect number of arguments given!, it should has 1 argument'
    echo './claimGroupId.sh [groupId]'
    exit 1
fi

echo "Get transaction counts"
count=`./rpc.sh --get-tx-count "$OWNER_ADDRESS" "$NODE_ADDRESS"`
echo "Got count $count"


echo "Sending claimGroupId call..."
# claimGroupId(String groupId)
callPayload="$(java -cp $TOOLS_JAR cli.ComposeCallPayload "claimGroupId" "$1")"
receipt=`./rpc.sh --call "$PRIVATE_KEY" "$count" "$CONTRACT_ADDRESS" "$callPayload" "1000000000000000000000" "$NODE_ADDRESS"`
echo "$receipt"
require_success $?

echo "Transaction returned receipt: \"$receipt\".  Waiting for transaction to complete..."
wait_for_receipt "$receipt"
echo "Transaction completed"

