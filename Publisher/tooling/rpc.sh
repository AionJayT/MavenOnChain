#!/bin/bash
TOOLS_JAR=mavenonipfsTool.jar

function print_help() {
	echo ' '
	echo 'Usage: ./rpc.sh [OPTIONS]'
	echo ' '
	echo 'OPTIONS:'
	echo ' '
	echo '--deploy <private_key> <nonce> <node_address> <jar_path> [argument]'
	echo '    Action: Deploys the specified dApp.'
	echo '    Returns: A transaction receipt hash that can be used later to query the transaction result.'
	echo ' '
	echo '--call <private_key> <nonce> <target_address> <serialized_call> <node_address>'
	echo '    Action: Calls into the specified dApp, invoking the specified method with the zero or more'
	echo '            provided arguments.'
	echo '            All arguments must first declare the type of the argument followed by the argument'
	echo '            Supported argument types: -I (int), -J (long), -S (short), -C (char), -F (float),'
	echo '                                      -D (double), -Z (boolean), -A (address), -T (string)'
	echo '    Returns: A transaction receipt hash that can be used later to query the transaction result.'
	echo ' '
	echo '--get-receipt-address <receipt hash> <node_address>'
	echo '  Returns the address from a deployment - fails with status 1 if not found and status 2 if failed'
	echo ' '
	echo '--get-tx-count <account_address> <node_address>'
	echo '  Check the current transaction nonce of the given account'
	echo ' '

	echo '--check-receipt-status <receipt hash> <node_address>'
	echo '  Checks the receipt status, exit 0 on pass - fails with status 1 if not found and status 2 if failed'
	echo ' '
}

function receipt_data() {
	header='{"jsonrpc":"2.0","id":1,"method":"eth_getTransactionReceipt","params":["'
	receipt="$1"
	footer='"]}'
	echo $header$receipt$footer
}

function eth_getTransactionCount() {
        header='{"jsonrpc":"2.0","id":1,"method":"eth_getTransactionCount","params":["'
        address="$1"
        comma='", "'
        block="pending"
        footer='"]}'
        echo $header$address$comma$block$footer
}

function extract_status() {
	if [[ "$1" =~ (\"status\".+\"id) ]];
	then
		result=${BASH_REMATCH[0]:10}
		echo ${result:0:3}
	fi
}

function extract_receipt_hash() {
	if [[ "$1" =~ (\"result\":\"0x[0-9a-f]{64}) ]];
	then
		echo ${BASH_REMATCH[0]:10:66}
	fi
}

function extract_address_from_receipt() {
	if [[ "$1" =~ (\"contractAddress\".+\"id) ]];
	then
		result=${BASH_REMATCH[0]:19}
		echo ${result:0:66}
	fi
}

function extract_number() {
        if [[ "$1" =~ (\"result\":\"0x[0-9a-f]{1,64}) ]];
        then
                echo $((16#${BASH_REMATCH[0]:12}))
        fi
}

function bad_connection_msg() {
	echo ' '
	echo "Unable to establish a connection using "$1". "
	echo "Ensure that the kernel is running and that it is running on the specified host and port, and that the kernel rpc connection is enabled. "
	echo 'The kernel rpc connection details can be modified in the configuration file at: config/config.xml'
}

if [ $# -eq 0 ]
then
	print_help
	exit 1
fi

function send_raw_and_print_receipt() {
	# send the transaction.
	payload='{"jsonrpc":"2.0","method":"eth_sendRawTransaction","params":["'$1'"],"id":1}'
	response=$(curl -s -X POST -H "Content-Type: application/json" --data "$payload" "$2")
	if [ $? -eq 7 ]
	then
		bad_connection_msg "$2"
		exit 1
	fi

	receipt_hash=$(extract_receipt_hash "$response")
	echo $receipt_hash
}

if [ "$1" = '--get-receipt-address' ]
then
	# get receipt must have 3 arguments.
	if [ $# -ne 3 ]
	then
		echo 'Incorrect number of arguments given!'
		print_help
		exit 1
	fi

	# query the transaction receipt.
	response=$(curl -s -X POST -H "Content-Type: application/json" --data "$(receipt_data "$2")" "$3")
	if [ $? -eq 7 ]
	then
		bad_connection_msg
		exit 1
	fi

	status=$(extract_status "$response")

	if [ "0x0" == "$status" ]
	then
		exit 2
	fi
	address=$(extract_address_from_receipt "$response")
	echo "$address"

elif [ "$1" = '--deploy' ]
then
	# Deploy has 4 arguments:
	private_key="$2"
	nonce="$3"
	node_address="$4"
	jar_path="$5"
	jar_bytes=""
	
	# Grab the bytes of the deployment jar.
	if [ $# -eq 5 ]
	then
		jar_bytes="$(java -cp $TOOLS_JAR cli.PackageJarAsHex "$jar_path")"
	elif [ $# -eq 6 ]
	then
		argument="$6"
		jar_bytes="$(java -cp $TOOLS_JAR cli.PackageJarAsHex "$jar_path" "$argument")"
	else
		echo 'Incorrect number of arguments given!'
		print_help
		exit 1
	fi

	if [ $? -ne 0 ]
	then
		echo 'PackageJarAsHex failed.'
		exit 1
	fi
	# Package the entire transaction.
	signed_deployment="$(java -cp $TOOLS_JAR cli.SignTransaction --privateKey "$private_key" --nonce "$nonce" --deploy "$jar_bytes")"
	if [ $? -ne 0 ]
	then
		echo 'Signing deployment transaction failed.'
		exit 1
	fi

	send_raw_and_print_receipt "$signed_deployment" "$node_address"

elif [ "$1" = '--call' ]
then
	# Call has 7 arguments:
	private_key="$2"
	nonce="$3"
	target_address="$4"
	serialized_call="$5"
	value="$6"
	
	if [ $# -eq 7 ]
	then
		# Package the entire transaction.
		signed_call="$(java -cp $TOOLS_JAR cli.SignTransaction --privateKey "$private_key" --nonce "$nonce" --destination "$target_address" --call "$serialized_call" --value "$value")"
		if [ $? -ne 0 ]
		then
			exit 1
		fi

		send_raw_and_print_receipt "$signed_call" "$7"
	else
		echo 'Incorrect number of arguments given!'
		print_help
		exit 1
	fi

elif [ "$1" = '--get-tx-count' ]
then
        # get-tx-count must have 3 arguments.
        if [ $# -ne 3 ]
        then
                echo 'Incorrect number of arguments given!'
                print_help
                exit 1
        fi

        # submit  and the sealinghash.
        response=$(curl -s -X POST -H "Content-Type: application/json" --data "$(eth_getTransactionCount "$2" )" "$3")
        if [ $? -eq 7 ]
        then
                bad_connection_msg
                exit 1
        fi


        echo "$(extract_number "$response")"

elif [ "$1" = '--check-receipt-status' ]
then
        # get receipt must have 3 arguments.
        if [ $# -ne 3 ]
        then
                echo 'Incorrect number of arguments given!'
                print_help
                exit 1
        fi

        # query the transaction receipt.
        response=$(curl -s -X POST -H "Content-Type: application/json" --data "$(receipt_data "$2")" "$3")
        if [ $? -eq 7 ]
        then
                bad_connection_msg
                exit 1
        fi

        status=$(extract_status "$response")

        if [ "0x0" == "$status" ]
        then
                exit 2
        elif [ "0x1" == "$status" ]
        then
                exit 0
        else
                exit 1
        fi
else
	print_help
	exit 1
fi
