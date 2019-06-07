package ibpv2gateway;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

public class IBPV2Gateway {
	
	public static void main(String argsv[]) throws Exception{
		
		IBPV2Gateway gateway = new IBPV2Gateway();
		
		NetworkConfig config = NetworkConfig.fromJsonFile(new File(
				gateway.getClass().getClassLoader().getResource("connection.json").getFile()
				));
		
		
		HFCAClient hfcaClient = HFCAClient.createNewInstance(config.getOrganizationInfo("Consortium").getCertificateAuthorities().get(0));
		
		UserContext user = new UserContext();
		user.setName("client"); 
		user.setMspId("Consortium");
		user.setEnrollment(hfcaClient.enroll("client", "client"));
		
		
		HFClient hfClient = HFClient.createNewInstance();
	    CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
	    hfClient.setCryptoSuite(cryptoSuite);
	    hfClient.setUserContext(user);
	   
	    Peer peer = hfClient.newPeer("184.173.1.173:31546", "grpcs://184.173.1.173:31546");
        Orderer orderer = hfClient.newOrderer("184.173.1.173:30305", "grpcs://184.173.1.173:30305");
        Channel channel = hfClient.newChannel("default");
        channel.addPeer(peer);
        channel.addOrderer(orderer);
        channel.initialize();
        
      
        TransactionProposalRequest tpr = hfClient.newTransactionProposalRequest();
        ChaincodeID cid = ChaincodeID.newBuilder().setName("loan").build();
        tpr.setChaincodeID(cid);
        tpr.setFcn("createLoan");
        tpr.setArgs(new String[]{"ganeshram-loan-89125", "{\"amount\":\"100000\"}"});
        tpr.setProposalWaitTime(5000);
        
        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
        tpr.setTransientMap(tm2);
        
        
        Collection<ProposalResponse> responses = channel.sendTransactionProposal(tpr);
        responses.stream().forEach(response -> response.getMessage());
        List<ProposalResponse> invalid = responses.stream().filter(r -> r.isInvalid()).collect(Collectors.toList());
        if (!invalid.isEmpty()) {
            invalid.forEach(response -> {
                System.out.println(response.getMessage());
            });
            throw new RuntimeException("invalid response(s) found");
        }
        
        channel.sendTransaction(responses).get();
        
       
       
	}
	
	
	
}
