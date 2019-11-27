#!/bin/bash

IPFS1_PATH="./ipfs1"
IPFS2_PATH="./ipfs2"
LOCAL_HOST="127.0.0.1"
NODE_IP="/ip4/$LOCAL_HOST/tcp"
NODE_PORT_SWARM1="4001"
NODE_PORT_SWARM2="4002"
NODE_PORT_API1="5001"
NODE_PORT_API2="5002"
NODE_PORT_GATEWAY1="9001"
NODE_PORT_GATEWAY2="9002"
LOG_KERNEL="kernel.log"
LOG_BOOTSTRAP="bootstrap.log"
LOG_BLOCKSIGNER="blocksigner.log"
LOG_READER="reader.log"
LOG_MVN="mvn.log"
KERNEL_RPC_PORT="8545"
WEB_SERVER_LISTEN_PORT="2000"
LOG_LISTEN_START_BLOCK="3"
MAVEN_CONTRACT_DEPLOY_ADDRESS="0xa01f53d4e4521941c68ba3570f0a4f1618363ceed09b45fa715b230eaae1a232"
GROUP_ID1="testing"

function print_log()
{
    echo
    echo "$1"
    echo
}

function clean_log()
{
    rm $LOG_KERNEL
    rm $LOG_BOOTSTRAP
    rm $LOG_BLOCKSIGNER
    rm $LOG_READER
    rm $LOG_MVN
}

function clean_mvn_dependency()
{
    print_log "Clean Downstream folder and the maven dependency."
    rm -r ./Downstream/target
    # Clean the groupID folder in the maven default repo
    rm -r ~/.m2/repository/$GROUP_ID1
}

function retry_10()
{
    if [[ "$1" -eq "10" ]]
    then
        print_log "Retry 10 times, exit this shell script!"
        exit 1
    fi
}

function shutdown_ipfs()
{
    print_log "Shutdown ipfs1 daemon..."
    kill -SIGINT $PID_NODE1

    print_log "Shutdown ipfs2 daemon..."
    kill -SIGINT $PID_NODE2

    COUNT=$(pgrep ipfs | wc -l)
    print_log "Ipfs shutdown remaining processes: $COUNT"
    RETRY=0
    while [ $COUNT -ne 0 ]
    do
        sleep 1
        COUNT=$(pgrep ipfs | wc -l)
        ((RETRY++))
        retry_10 $RETRY
        echo "Ipfs shutdown remaining processes: $COUNT, retries: $RETRY"
    done
    print_log "Ipfs nodes shutdown finished."
}

function shutdown_reader()
{
    print_log "Shutdown reader..."

    kill -SIGTERM $PID_READER

    COUNT=$(ps $PID_READER | grep "Reader" | wc -l)

    print_log "Shutdown reader: $COUNT"

    RETRY=0
    while [ $COUNT -ne 0 ]
    do
        kill -SIGTERM $PID_READER
        sleep 1
        COUNT=$(ps $PID_READER | grep "Reader" | wc -l)
        retry_10 $RETRY
        ((RETRY++))
        echo "shutdown remaining processes: $COUNT, retries: $RETRY"
    done

    print_log "Reader shutdown finished."
}

function shutdown_oan()
{
    print_log "Shutdown the OAN kernel..."

    trap 'trap - SIGTERM && kill $PID_OAN' SIGINT SIGTERM EXIT

    check_kernel_shutdown_log

    print_log "The OAN kernel shutdown finished."
}

function shutdown_blocksigner()
{
    print_log "Shutdown blocksigner..."

    #Can't terminate the blocksigner by sending Ctrl + C, just hack it by SIGTERM
    kill -SIGTERM $PID_BLOCKSIGNER

    COUNT=$(ps $PID_BLOCKSIGNER | grep "org.aion.staker.ExternalStaker" | wc -l)

    RETRY=0
    while [ $COUNT -ne 0 ]
    do
        sleep 1
        kill -SIGTERM $PID_BLOCKSIGNER
        retry_10 $RETRY
        ((RETRY++))
        echo "Waiting the blocksigner shutdown, retries: $RETRY"
    done

    print_log "The blocksigner shutdown finished."
}

function check_bootstrap_log()
{
    COUNT=$(cat $LOG_BOOTSTRAP | grep "Aion kernel graceful shutdown successful!" | wc -l)
    while [ $COUNT -eq 0 ]
    do
        sleep 3
        echo "Waiting bootstrap script finish..."
        COUNT=$(cat $LOG_BOOTSTRAP | grep "Aion kernel graceful shutdown successful!" | wc -l)
    done
}

function check_kernel_rpc_log()
{
    COUNT=$(cat $LOG_KERNEL | grep "rpc-server - (UNDERTOW) started" | wc -l)
    while [ $COUNT -eq 0 ]
    do
        sleep 1
        echo "Waiting rpc server start..."
        COUNT=$(cat $LOG_KERNEL | grep "rpc-server - (UNDERTOW) started" | wc -l)
    done
}

function check_kernel_shutdown_log()
{
    COUNT=$(cat $LOG_KERNEL | grep "rpc-server - (UNDERTOW) started" | wc -l)
    while [ $COUNT -eq 0 ]
    do
        sleep 1
        echo "Waiting the kernel shutdown gracefully..."
        COUNT=$(cat $LOG_KERNEL | grep "Aion kernel graceful shutdown successful!" | wc -l)
    done
}

function check_blocksigner_log()
{
    COUNT=$(cat $LOG_BLOCKSIGNER | grep "Using coinbase Address" | wc -l)
    while [ $COUNT -eq 0 ]
    do
        sleep 1
        echo "Waiting the blocksigner grep the coinbase from the kernel..."
        COUNT=$(cat $LOG_BLOCKSIGNER | grep "Using coinbase Address" | wc -l)
    done
}

function check_reader_log()
{
    COUNT=$(cat $LOG_READER | grep "$1" | wc -l)
    while [ $COUNT -eq 0 ]
    do
        sleep 1
        echo "Waiting the reader print <$1>..."
        COUNT=$(cat $LOG_READER | grep "$1" | wc -l)
    done

    print_log "Found the log event <$1>"
}

function shutdown_all()
{
    shutdown_ipfs
    shutdown_blocksigner
    shutdown_oan
    shutdown_reader
}

function test_failure()
{
    print_log "Integration test failure!, shutdown all processes!"

    shutdown_all
}

print_log "Start the integration env setup..."
print_log "Clean up the ipfs folder..."

rm -rf $IPFS1_PATH $IPFS2_PATH

clean_mvn_dependency

print_log "Clean up the log genarated by the privious executing result..."

clean_log

print_log "Init 2 ipfs nodes and copy the preset config into the folders."

#This script assume you already installed the go-ipfs.

export IPFS_PATH=$IPFS1_PATH
ipfs init
ipfs config --json Addresses '{"Swarm": ["/ip4/127.0.0.1/tcp/4001"], "Announce": [], "NoAnnounce": [], "API": "/ip4/127.0.0.1/tcp/5001", "Gateway": "/ip4/127.0.0.1/tcp/9001"}'
ipfs config --bool Discovery.MDNS.Enabled "false"
ipfs bootstrap rm --all
NODE_ID1=$(ipfs id -f="<id>\n")

print_log "Generate node1Id: $NODE_ID1"

export IPFS_PATH=$IPFS2_PATH
ipfs init
ipfs config --json Addresses '{"Swarm": ["/ip4/127.0.0.1/tcp/4002"], "Announce": [], "NoAnnounce": [], "API": "/ip4/127.0.0.1/tcp/5002", "Gateway": "/ip4/127.0.0.1/tcp/9002"}'
ipfs config --bool Discovery.MDNS.Enabled "false"
ipfs bootstrap rm --all
NODE_ID2=$(ipfs id -f="<id>\n")

print_log "Generated node2Id: $NODE_ID2"

print_log "Adding node1 info to the node2 bootstrap list"

ipfs bootstrap add "$NODE_IP/$NODE_PORT_SWARM1/ipfs/$NODE_ID1"

print_log "Adding node2 info to the node1 bootstrap list"

export IPFS_PATH=$IPFS1_PATH
ipfs bootstrap add "$NODE_IP/$NODE_PORT_SWARM2/ipfs/$NODE_ID2"

print_log "Executing the ipfs node1 in the background"
ipfs daemon &

PID_NODE1=$(ps aux | grep -i 'ipfs daemon$' | awk 'NR==1{print $2}')

print_log "The node1 PID: $PID_NODE1"

export IPFS_PATH=$IPFS2_PATH

print_log "Executing the ipfs node2 in the background"

ipfs daemon &

PID_NODE2=$(ps aux | grep -i 'ipfs daemon' | awk 'NR==2{print $2}')

print_log "The node2 PID: $PID_NODE2"

print_log "Checking 2 ipfs nodes connected each other..."

PEER1=$(ipfs swarm peers)
RETRY=0
while [ "$PEER1" = "" ]
do
    sleep 1
    echo "Checking peer1 connection setup..."
    PEER1=$(ipfs swarm peers)
    retry_10 $RETRY
    ((RETRY++))
done

echo "PEER1 = $PEER1"
if [[ "$PEER1" != "$NODE_IP/$NODE_PORT_SWARM1/ipfs/$NODE_ID1" ]]
then
    print_log "Can not find the peer1 info from the ipfs node2. Exit the script."

    shutdown_ipfs
    exit 1
fi

print_log "Found peer1 info in the ipfs node2."

export IPFS_PATH=$IPFS1_PATH
PEER2=$(ipfs swarm peers)
RETRY=0
while [ "$PEER2" = "" ]
do
    sleep 1

    print_log "Checking peer2 connection setup..."

    PEER2=$(ipfs swarm peers)
    retry_10 $RETRY
    ((RETRY++))
done

echo "PEER2 = $PEER2"
if [[ "$PEER2" != "$NODE_IP/$NODE_PORT_SWARM2/ipfs/$NODE_ID2" ]]
then
    print_log "Can not find the peer2 info from the ipfs node1. Exit the script."

    shutdown_ipfs
    exit 1
fi

print_log "Found peer2 info in the ipfs node1."

print_log "Initialize the blockchain kernel custom network environment, it will take a while..."
print_log "You can check the bootstrap status by using <tail -f $LOG_BOOTSTRAP> under the IntegTest folder."

# This script assume the folder has the OAN java blockchain kernel and put it into the <oan> filder
cd oan/tooling/customBootstrap
nohup ./customNetworkBootstrap.sh > ../../../$LOG_BOOTSTRAP
cd ../../../

check_bootstrap_log

print_log "OAN customm network setup finished."

print_log "Launching the OAN kernel..."

cd oan/
nohup ./aion.sh -n custom > ../$LOG_KERNEL &
cd ..

check_kernel_rpc_log

PID_OAN=$(ps aux | grep -i 'org.aion.Aion -n custom' -m1 | awk -F ' ' '{print $2}')

print_log "The OAN kernel PID: $PID_OAN"

if [[ "$PID_OAN" == "" ]]
then
    print_log "Kernel launch failed. Exit the script."

    shutdown_ipfs
    exit 1
fi

print_log "Launching the blocksigner..."

cd oan/tooling/externalStaker/
nohup ./launchStaker.sh > ../../../$LOG_BLOCKSIGNER &
cd ../../../

check_blocksigner_log

PID_BLOCKSIGNER=$(ps aux | grep -i 'org.aion.staker.ExternalStaker' -m1 | awk -F ' ' '{print $2}')

print_log "The blockSigner PID: $PID_BLOCKSIGNER"


SWITCH_FROM_INTEGTEST_TO_TOOLING_PATH="../Publisher/tooling"
SWITCH_FROM_TOOLING_TO_INTEGTEST_PATH="../../IntegTest"

print_log "Start the integ tests..."

# Deploy the maven on IPFS contract, This script assume the dev already put the contract jar in the <Pubisher/tooling> folder
cd $SWITCH_FROM_INTEGTEST_TO_TOOLING_PATH
./deployMavenContract.sh
cd $SWITCH_FROM_TOOLING_TO_INTEGTEST_PATH

READER_JAR_PATH=../Reader/build/main/Reader.jar:../lib/*
KH="--kernelHostname"
KP="--kernelPort"
IH="--ipfsHostname"
IP="--ipfsPort"
LP="--listenPort"
CA="--contractAddress"
SB="--startingBlockNumber"
#TODO: verify the reader read the deploy event log from the blockchain.

# claim groupID
print_log "claim the groupID < $GROUP_ID1 >"

cd $SWITCH_FROM_INTEGTEST_TO_TOOLING_PATH
./claimGroupId.sh $GROUP_ID1
cd $SWITCH_FROM_TOOLING_TO_INTEGTEST_PATH


ARTIFACT_ID="upstream"
VERSION="1.0"
TYPEJAR="1"
TYPEPOM="0"
JARFILE="upstream-1.0.jar"
POMFILE="pom.xml"

print_log "Publish the upstream file"

cd $SWITCH_FROM_INTEGTEST_TO_TOOLING_PATH
# ./publish.sh [filename] [groupId] [artifactId] [version] [type]
./publish.sh "$SWITCH_FROM_TOOLING_TO_INTEGTEST_PATH/Upstream/target/$JARFILE" $GROUP_ID1 $ARTIFACT_ID $VERSION $TYPEJAR
./publish.sh "$SWITCH_FROM_TOOLING_TO_INTEGTEST_PATH/Upstream/$POMFILE" $GROUP_ID1 $ARTIFACT_ID $VERSION $TYPEPOM
cd $SWITCH_FROM_TOOLING_TO_INTEGTEST_PATH


# TODO: verify the reader read the claimID event log from the blockchain.
nohup java -cp $READER_JAR_PATH Reader \
         $KH $LOCAL_HOST $KP $KERNEL_RPC_PORT $IH $LOCAL_HOST $IP $NODE_PORT_API2 $LP $WEB_SERVER_LISTEN_PORT $CA $MAVEN_CONTRACT_DEPLOY_ADDRESS $SB $LOG_LISTEN_START_BLOCK \
         > $LOG_READER &

PID_READER=$(ps aux | grep -i 'Reader' -m1 | awk -F ' ' '{print $2}')

print_log "The blockSigner PID: $PID_READER"

check_reader_log "CLAIM: $GROUP_ID1"
check_reader_log "PUBLISH: $GROUP_ID1 $ARTIFACT_ID $VERSION.jar"
check_reader_log "PUBLISH: $GROUP_ID1 $ARTIFACT_ID $VERSION.pom"

print_log "Check Downstream maven test..."

cd Downstream
mvn -U clean test > ../$LOG_MVN
cd ..

MVN_RESULT=$(cat $LOG_MVN | grep "BUILD SUCCESS" | wc -l)

if [[ "$MVN_RESULT" -eq "0" ]]
then
    test_failure
    TEST_RESULT="failure!"
else
    print_log "Declaim the groupID < $GROUP_ID1 >"

    cd $SWITCH_FROM_INTEGTEST_TO_TOOLING_PATH
    ./deClaimGroupId.sh $GROUP_ID1
    cd $SWITCH_FROM_TOOLING_TO_INTEGTEST_PATH

    check_reader_log "DECLAIM: $GROUP_ID1"

    shutdown_all
    TEST_RESULT="Success!"
fi

print_log "Integ test finished! Test $TEST_RESULT"
