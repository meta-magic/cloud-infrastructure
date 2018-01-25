package com.desire3d.aws;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.desire3d.aws.dto.Ec2InstanceConfig;
import com.desire3d.aws.ec2.Ec2Instance;

@SpringBootTest
@RunWith(SpringRunner.class)
public class Ec2InstanceTests {
	
	private final String ACCESS_KEY = "AKIAJLOJG5N26SRYSBQA";
	private final String SECRET_KEY = "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";
	
	private static final String LOCAL_KEY_STORE = "/home/mahesh/platform/workspace_after_jdo/aws-cloud-infra/src/main/resources/keystore/";
	private static final String USER_DATA_EXCHANGE_PATH = "/Softwares/tc9/webapps/code-pipeline-service/WEB-INF/classes/application.properties";
	
	private Ec2Instance ec2Example;
	
	@Before
	public void init() {
		ec2Example = new Ec2Instance(ACCESS_KEY, SECRET_KEY);
	}
	
	@Test
	public void testCreateInstance() {
		Ec2InstanceConfig config = new Ec2InstanceConfig(UUID.randomUUID().toString(), "ami-a5f3d9c0", "t2.micro", "subnet-5f0e1c24", "sg-594cbe32")
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
		ec2Example.startInstance("i-08b218fcee7a50650");
	}

	//	@Test
	public void testStopInstance() {
		ec2Example.stopInstance("i-08b218fcee7a50650");
	}

	//	@Test
	public void testTerminateInstance() {
		ec2Example.terminateInstance("i-08b218fcee7a50650");
	}

	//	@Test
	public void testCreateKeyPair() {
		ec2Example.createKeyPair("myuserid", LOCAL_KEY_STORE);
	}
}