#!/bin/bash

IPFS1_PATH="./ipfs1"
IPFS2_PATH="./ipfs2"
NODE_IP="/ip4/127.0.0.1/tcp"
NODE_PORT_SWARM1="4001"
NODE_PORT_SWARM2="4002"
NODE_PORT_API1="5001"
NODE_PORT_API2="5002"
NODE_PORT_GATEWAY1="9001"
NODE_PORT_GATEWAY2="9002"
LOG_KERNEL="kernel.log"
LOG_BOOTSTRAP="bootstrap.log"
LOG_BLOCKSIGNER="blocksigner.log"


function clean_log()
{
    rm $LOG_KERNEL
    rm $LOG_BOOTSTRAP
    rm $LOG_BLOCKSIGNER
}

function shutdown_ipfs()
{
    echo
    echo "Shutdown ipfs1 daemon..."
    kill -SIGINT $PID_NODE1

    echo "Shutdown ipfs2 daemon..."
    kill -SIGINT $PID_NODE2

    COUNT=$(pgrep ipfs | wc -l)
    echo "Ipfs shutdown remaining processes: $COUNT"
    while [ $COUNT -ne 0 ]
    do
        sleep 1
        COUNT=$(pgrep ipfs | wc -l)
        echo "Ipfs shutdown remaining processes: $COUNT"
    done
    echo "Ipfs nodes shutdown finished."
    echo
}

function shutdown_oan()
{
    echo
    echo "Shutdown the OAN kernel..."

    trap 'trap - SIGTERM && kill $PID_OAN' SIGINT SIGTERM EXIT

    check_kernel_shutdown_log

    echo "The OAN kernel shutdown finished."
    echo
}

function shutdown_blocksigner()
{
    echo
    echo "Shutdown blocksigner..."
    #Can't terminate the blocksigner by sending Ctrl + C, just hack it by SIGKILL
    kill -SIGKILL $PID_BLOCKSIGNER

    COUNT=$(ps $PID_BLOCKSIGNER | grep "org.aion.staker.ExternalStaker" | wc -l)

    while [ $COUNT -ne 0 ]
    do
        sleep 1
        echo "Waiting the blocksigner shutdown"
    done

    echo "The blocksigner shutdown finished."
    echo
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

echo "Start the integration env setup..."

echo
echo "Clean up the ipfs folder..."
echo
rm -rf $IPFS1_PATH $IPFS2_PATH
echo "Clean up the log genarated by the privious executing result..."
clean_log

echo
echo "Init 2 ipfs nodes and copy the preset config into the folders."
echo

#This script assume you already installed the go-ipfs.

export IPFS_PATH=$IPFS1_PATH
ipfs init
ipfs config --json Addresses '{"Swarm": ["/ip4/127.0.0.1/tcp/4001"], "Announce": [], "NoAnnounce": [], "API": "/ip4/127.0.0.1/tcp/5001", "Gateway": "/ip4/127.0.0.1/tcp/9001"}'
ipfs config --bool Discovery.MDNS.Enabled "false"
ipfs bootstrap rm --all
NODE_ID1=$(ipfs id -f="<id>\n")
echo
echo "Generate node1Id: $NODE_ID1"
echo
export IPFS_PATH=$IPFS2_PATH
ipfs init
ipfs config --json Addresses '{"Swarm": ["/ip4/127.0.0.1/tcp/4002"], "Announce": [], "NoAnnounce": [], "API": "/ip4/127.0.0.1/tcp/5002", "Gateway": "/ip4/127.0.0.1/tcp/9002"}'
ipfs config --bool Discovery.MDNS.Enabled "false"
ipfs bootstrap rm --all
NODE_ID2=$(ipfs id -f="<id>\n")
echo
echo "Generated node2Id: $NODE_ID2"
echo

echo "Adding node1 info to the node2 bootstrap list"
echo
ipfs bootstrap add "$NODE_IP/$NODE_PORT_SWARM1/ipfs/$NODE_ID1"
echo
echo "Adding node2 info to the node1 bootstrap list"
echo
export IPFS_PATH=$IPFS1_PATH
ipfs bootstrap add "$NODE_IP/$NODE_PORT_SWARM2/ipfs/$NODE_ID2"
echo

echo "Executing the ipfs node1 in the background"
ipfs daemon &

PID_NODE1=$(ps aux | grep -i 'ipfs daemon$' | awk 'NR==1{print $2}')
echo
echo "The node1 PID: $PID_NODE1"
echo

export IPFS_PATH=$IPFS2_PATH
echo "Executing the ipfs node2 in the background"
ipfs daemon &

PID_NODE2=$(ps aux | grep -i 'ipfs daemon' | awk 'NR==2{print $2}')
echo
echo "The node2 PID: $PID_NODE2"
echo

echo "Checking 2 ipfs nodes connected each other..."
PEER1=$(ipfs swarm peers)
RETRY=0
while [ "$PEER1" = "" ]
do
    sleep 1
    echo "Checking peer1 connection setup..."
    PEER1=$(ipfs swarm peers)
    ((RETRY++))
    if [[ "$RETRY" -eq "10" ]]
    then
        shutdown_ipfs
        echo "Can not find the peer1 info from the ipfs node2. Exit the script."
        exit 1
    fi
done

echo "PEER1 = $PEER1"
if [[ "$PEER1" != "$NODE_IP/$NODE_PORT_SWARM1/ipfs/$NODE_ID1" ]]
then
    echo "Can not find the peer1 info from the ipfs node2. Exit the script."
    shutdown_ipfs
    exit 1
fi

echo "Found peer1 info in the ipfs node2."

export IPFS_PATH=$IPFS1_PATH
PEER2=$(ipfs swarm peers)
RETRY=0
while [ "$PEER2" = "" ]
do
    sleep 1
    echo "Checking peer2 connection setup..."
    PEER2=$(ipfs swarm peers)
    ((RETRY++))
    if [[ "$RETRY" -eq "10" ]]
    then
        shutdown_ipfs
        echo "Can not find the peer2 info from the ipfs node1. Exit the script."
        exit 1
    fi
done

echo "PEER2 = $PEER2"
if [[ "$PEER2" != "$NODE_IP/$NODE_PORT_SWARM2/ipfs/$NODE_ID2" ]]
then
    echo "Can not find the peer2 info from the ipfs node1. Exit the script."
    shutdown_ipfs
    exit 1
fi

echo "Found peer2 info in the ipfs node1."

echo
echo "Initialize the blockchain kernel custom network environment, it will take a while..."
echo "You can check the bootstrap status by using <tail -f $LOG_BOOTSTRAP> under the IntegTest folder."
echo
# This script assume the folder has the OAN java blockchain kernel and put it into the <oan> filder
cd oan/tooling/customBootstrap
nohup ./customNetworkBootstrap.sh > ../../../$LOG_BOOTSTRAP
cd ../../../

check_bootstrap_log

echo
echo "OAN customm network setup finished."
echo

echo
echo "Launching the OAN kernel..."
echo
cd oan/
nohup ./aion.sh -n custom > ../$LOG_KERNEL &
cd ..

check_kernel_rpc_log

PID_OAN=$(ps aux | grep -i 'org.aion.Aion -n custom' -m1 | awk -F ' ' '{print $2}')
echo "The OAN kernel PID: $PID_OAN"
if [[ "$PID_OAN" == "" ]]
then
    echo "Kernel launch failed. Exit the script."
    shutdown_ipfs
    exit 1
fi

echo
echo "Launching the blocksigner..."
echo
cd oan/tooling/externalStaker/
nohup ./launchStaker.sh > ../../../$LOG_BLOCKSIGNER &
cd ../../../

check_blocksigner_log

PID_BLOCKSIGNER=$(ps aux | grep -i 'org.aion.staker.ExternalStaker' -m1 | awk -F ' ' '{print $2}')
echo "The blockSigner PID: $PID_BLOCKSIGNER"


echo "Start the integ tests..."
#TODO: running Tests in here!!!
#...................
echo "End of the integ tests..."



#Shutdown process. Notice: please shutdown blocksigner before shutdown the blockchain kernel.
echo
echo "Start shutdown process..."
shutdown_ipfs
shutdown_blocksigner
shutdown_oan

echo
echo "Integ test finished!"
echo
