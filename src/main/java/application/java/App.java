/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

// Running TestApp: 
// gradle runApp 

package application.java;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;


public class App {

	static {
		System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
	}

	// helper function for getting connected to the gateway
	public static Gateway connect() throws Exception{
		// Load a file system based wallet for managing identities.
		Path walletPath = Paths.get("wallet");
		Wallet wallet = Wallets.newFileSystemWallet(walletPath);
		// load a CCP
		Path networkConfigPath = Paths.get("..", "..", "test-network", "organizations", "peerOrganizations", "org1.example.com", "connection-org1.yaml");

		Gateway.Builder builder = Gateway.createBuilder();
		builder.identity(wallet, "admin").networkConfig(networkConfigPath).discovery(true);
		return builder.connect();
	}

	public static void main(String[] args) throws Exception {
		// enrolls the admin and registers the user
		try {
			EnrollAdmin.main(null);
			// RegisterUser.main(null);
		} catch (Exception e) {
			System.err.println(e);
		}

		// connect to the network and invoke the smart contract
		try (Gateway gateway = connect()) {

			// get the network and contract
			Network network = gateway.getNetwork("mychannel");
			Contract contract = network.getContract("basic");

			byte[] result;

			System.out.println("Submit Transaction: InitLedger creates the initial set of tasks on the ledger.");
			contract.submitTransaction("InitLedger");

			System.out.println("\n");
			result = contract.evaluateTransaction("GetAllTasks");
			System.out.println("Evaluate Transaction: GetAllTasks, result: " + new String(result));

			// System.out.println("\n");
			// System.out.println("Submit Transaction: CreateTask task13");
			//CreateTask creates an task with ID task13, username task_poster6, role poster, taskName EMNIST_NN, TaskDescription a test sample of the combination of federated learning and blockchain, dataset EMNIST, modelName NN, num_clients 10, epochs 5, bonus of 15 and false
			// contract.submitTransaction("CreateTask", "task13", "task_poster6", "poster", "EMNIST_NN", "a test sample of the combination of federated learning and blockchain", "EMNIST", "NN", "10", "5", "15", "false");

			System.out.println("\n");
			System.out.println("Evaluate Transaction: ReadTask task13");
			// ReadTask returns a task with given taskID
			result = contract.evaluateTransaction("ReadTask", "task13");
			System.out.println("result: " + new String(result));

			System.out.println("\n");
			System.out.println("Evaluate Transaction: TaskExists task1");
			// TaskExists returns "true" if an task with given taskID exist
			result = contract.evaluateTransaction("TaskExists", "task1");
			System.out.println("result: " + new String(result));

			System.out.println("\n");
			System.out.println("Submit Transaction: UpdateTask task1, new username : task_poster6");
			// UpdateTask updates an existing task with new properties. Same args as CreateTask
			contract.submitTransaction("UpdateTask", "task1", "task_poster6", "poster", "EMNIST_NN", "a test sample of the combination of federated learning and blockchain", "EMNIST", "NN", "10", "5", "15", "false");

			System.out.println("\n");
			System.out.println("Evaluate Transaction: ReadTask task1");
			result = contract.evaluateTransaction("ReadTask", "task1");
			System.out.println("result: " + new String(result));

			try {
				System.out.println("\n");
				System.out.println("Submit Transaction: UpdateTask task70");
				//Non existing task task70 should throw Error
				contract.submitTransaction("UpdateTask", "task70", "task_poster6", "poster", "EMNIST_NN", "a test sample of the combination of federated learning and blockchain", "EMNIST", "NN", "10", "5", "15", "false");
			} catch (Exception e) {
				System.err.println("Expected an error on UpdateTask of non-existing Task: " + e);
			}

			System.out.println("\n");
			System.out.println("Submit Transaction: TransferTask task1 from username task_poster6 > username task_poster1");
			// TransferTask transfers an task with given ID to new owner Tom
			contract.submitTransaction("TransferTask", "task6", "task_poster1");

			System.out.println("\n");
			System.out.println("Evaluate Transaction: ReadTask task1");
			result = contract.evaluateTransaction("ReadTask", "task1");
			System.out.println("result: " + new String(result));
		}
		catch(Exception e){
			System.err.println(e);
		}

	}
}
