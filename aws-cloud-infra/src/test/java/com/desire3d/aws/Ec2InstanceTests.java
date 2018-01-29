package com.desire3d.aws;

import java.util.UUID;

import com.amazonaws.regions.Regions;
import com.desire3d.aws.dto.Ec2InstanceConfig;
import com.desire3d.aws.ec2.Ec2Instance;

public class Ec2InstanceTests {
	
	private final String ACCESS_KEY = "AKIAJLOJG5N26SRYSBQA";
	private final String SECRET_KEY = "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";
	
	private static final String LOCAL_KEY_STORE = "/home/mahesh/aws/keystore/";
	private static final String USER_DATA_EXCHANGE_PATH = "/Softwares/tc9/webapps/code-pipeline-service/WEB-INF/classes/application.properties";
	
	private Ec2Instance ec2Example;
	
//	@Before
	public void init() {
		ec2Example = new Ec2Instance(ACCESS_KEY, SECRET_KEY, Regions.US_EAST_2.getName());
	}
	
//	@Test
	public void testCreateInstance() {
		Ec2InstanceConfig config = new Ec2InstanceConfig(UUID.randomUUID().toString(), "ami-c52712a0", "t2.micro", "subnet-5f0e1c24", "sg-594cbe32")
									.withLocalKeyStore(LOCAL_KEY_STORE)
									.withUserDataExchangePath(USER_DATA_EXCHANGE_PATH); 
		
		ec2Example.createEc2Instance(config);
	}

	//	@Test
	public void testRunningInstances() {
		try {
			ec2Example.getRunningInstances();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	@Test
	public void testStartInstance() {
		ec2Example.startInstance("i-04338e34e794c3d3c");
	}

//	@Test
	public void testStopInstance() {
		ec2Example.stopInstance("i-04338e34e794c3d3c");
	}

//	@Test
	public void testTerminateInstance() {
		ec2Example.terminateInstance("i-04338e34e794c3d3c");
	}

	//	@Test
	public void testCreateKeyPair() {
		ec2Example.createKeyPair("myuserid", LOCAL_KEY_STORE);
	}
}
