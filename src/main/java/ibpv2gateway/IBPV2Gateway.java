package ibpv2gateway;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

public class IBPV2Gateway {
	
	public static void main(String argsv[]) throws Exception{

        //System.setProperty("javax.net.ssl.trustStore", "/Users/ganeshram/projects/blockchain/IBPV2JavaClient/IBV2Client/src/main/resources/myLocalTrustStore");
        //System.setProperty("javax.net.ssl.trustStorePassword","changeit");
		
		IBPV2Gateway gateway = new IBPV2Gateway();
		
		NetworkConfig config = NetworkConfig.fromJsonFile(new File(
				gateway.getClass().getClassLoader().getResource("connection.json").getFile()
				));
		
                
		HFCAClient hfcaClient = HFCAClient.createNewInstance(config.getOrganizationInfo("B2S").getCertificateAuthorities().get(0));
		UserContext user = new UserContext();
		user.setName("application"); 
		user.setMspId("B2S");
        user.setEnrollment(hfcaClient.enroll("ganeshram", "ganeshram"));
        
         //PEERS

         
		
		
		HFClient hfClient = HFClient.createNewInstance();
	    CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
	    hfClient.setCryptoSuite(cryptoSuite);
	    hfClient.setUserContext(user);
        Properties test = config.getPeerProperties("173.193.75.101:30644");
        test.toString();
        Peer peer = hfClient.newPeer("173.193.75.101:30562", "grpcs://173.193.75.101:30562",config.getPeerProperties("173.193.75.101:30562"));
        //Peer peer2 = hfClient.newPeer("173.193.75.101:31910", "grpcs://173.193.75.101:31910");
        Peer peer3 = hfClient.newPeer("173.193.75.101:30644", "grpcs://173.193.75.101:30644",config.getPeerProperties("173.193.75.101:30644"));
        Orderer orderer = hfClient.newOrderer("173.193.75.101:31695", "grpcs://173.193.75.101:31695");
        Channel channel = hfClient.newChannel("newch");
        channel.addPeer(peer);
        //channel.addPeer(peer2);
        channel.addPeer(peer3);
        channel.addOrderer(orderer);
        channel.initialize();

       


  
        TransactionProposalRequest tpr = hfClient.newTransactionProposalRequest();
        ChaincodeID cid = ChaincodeID.newBuilder().setName("b2spointsbank").build();
        tpr.setChaincodeID(cid);
        tpr.setFcn("createB2Spointsbank");
        tpr.setArgs(new String[]{"ganeshram12", "{\"points\":\"100\"}"});
        tpr.setProposalWaitTime(5000);
        
        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
        tpr.setTransientMap(tm2);
        
        
        Collection<ProposalResponse> responses = channel.sendTransactionProposal(tpr);
        for(ProposalResponse response : responses){
            System.out.println("-------------RESPONSE INTROSPECTION-------------");
            System.out.println(response.getPeer().getName());
            System.out.println(response.getStatus().getStatus());
            System.out.println(response.getMessage());
            System.out.println("--------------RESPONSE INTROSPECTION-------------");
        }
        
        responses.stream().forEach(response -> response.getMessage());

        List<ProposalResponse> invalid = responses.stream().filter(r -> r.isInvalid()).collect(Collectors.toList());
        if (!invalid.isEmpty()) {
            invalid.forEach(response -> {
                System.out.println(response.getMessage());
            });
            //throw new RuntimeException("invalid response(s) found");
        }
        
        channel.sendTransaction(responses).get();

        System.out.println("****************************************DONE WRITING******************************************");

        



        QueryByChaincodeRequest tpr2 = hfClient.newQueryProposalRequest();
        //ChaincodeID cid = ChaincodeID.newBuilder().setName("b2spointsbank").build();
        tpr2.setChaincodeID(cid);
        tpr2.setFcn("readB2Spointsbank");
        tpr2.setArgs(new String[]{"ganeshram12"});
        tpr2.setProposalWaitTime(5000);

        Collection<ProposalResponse> responses2 = channel.queryByChaincode(tpr2);
        
        for(ProposalResponse response : responses2){
            System.out.println("-------------RESPONSE INTROSPECTION-------------");
            System.out.println(response.getPeer().getName());
            System.out.println(response.getStatus().getStatus());
            System.out.println(response.getProposalResponse().getResponse().getPayload().toStringUtf8());
            System.out.println("--------------RESPONSE INTROSPECTION-------------");
        }
        
        responses2.stream().forEach(response -> response.getMessage());

        List<ProposalResponse> invalid2 = responses2.stream().filter(r -> r.isInvalid()).collect(Collectors.toList());
        if (!invalid2.isEmpty()) {
            invalid2.forEach(response -> {
                System.out.println(response.getMessage());
            });
            throw new RuntimeException("invalid response(s) found");
        }
        
        //channel.sendTransaction(responses2).get();
        
        
       
       
	}
	
	
	
}
