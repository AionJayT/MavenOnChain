This is publisher

To proceed the publisher app intetact with the Aion blockchain kernel and the IPFS node, you must 

1. Build the mavenonipfs.jar by running `mvn test` in the `<repo_path>/Contract` folder.
2. cp the `mavenonipfs.jar` into the `<repo_path>/Publisher/tooling/` folder.
3. execute `ant` in `<repo_path>/Publisher`, the publisher app will be built and copy into the `tooling` folder.
4. `cd MavenOnIPFSTool` and execute `ant`, the mavenonipfstool.jar will be copied into the `tooling` folder.
5. `cd tooling` folder.
6. Deploy the avm contract by executing `./deployMavenContract.sh`, becareful the privatekey and address settings in the script.
7. use `claimGroupId.sh`, `deClaimGroupId.sh` and `publish.sh` to interact with the blockchain and ipfs.
