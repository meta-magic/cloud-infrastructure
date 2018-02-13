package com.desire3d.aws;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.junit.Before;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.desire3d.aws.dto.Ec2InstanceConfig;
import com.desire3d.aws.ec2.Ec2Instance;

public class Ec2InstanceTests {
	
	private static final Logger logger = org.apache.log4j.Logger.getLogger(Ec2Instance.class);
	
	private final String ACCESS_KEY = "AKIAJLOJG5N26SRYSBQA";
	private final String SECRET_KEY = "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";
	
	private static final String LOCAL_KEY_STORE = "/home/mahesh/aws/keystore/";
	private static final String USER_DATA_EXCHANGE_PATH = "/Softwares/tc9/webapps/code-pipeline-service/WEB-INF/classes/application.properties";
	
	private Ec2Instance ec2Example;
	
	private final String instanceId = "i-039e310921a68ff44";
	
	@Before
	public void init() {
		ec2Example = new Ec2Instance(ACCESS_KEY, SECRET_KEY, Regions.US_EAST_2.getName());
	}
	
//	@Test
	public void testCreateInstance() {
		Ec2InstanceConfig config = new Ec2InstanceConfig("test-" + UUID.randomUUID().toString(), "ami-857440e0", "t2.micro", "subnet-5f0e1c24", "sg-594cbe32")
									.withLocalKeyStore(LOCAL_KEY_STORE)
									.withUserDataExchangePath(USER_DATA_EXCHANGE_PATH); 
		
		List<Instance> result = ec2Example.createInstance(config);
		List<String> instanceIds = result.parallelStream().map(i -> i.getInstanceId()).collect(Collectors.toList());
		List<InstanceStatus> instanceStatuses = ec2Example.waitForStatusChangeToRunning(instanceIds);
		Instance instance = ec2Example.getInstanceById(instanceIds.get(0));
		
		logger.info(instanceStatuses.parallelStream().map(is -> is.getInstanceState().getName()).collect(Collectors.toList()) + " "+ instance);
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
		ec2Example.startInstance(instanceId);
		List<InstanceStatus> instanceStatuses = ec2Example.waitForStatusChangeToRunning(instanceId);
		logger.info(instanceStatuses.parallelStream().map(is -> is.getInstanceState().getName()).collect(Collectors.toList()));
	}

//	@Test
	public void testStopInstance() {
		ec2Example.stopInstance(instanceId);
		List<InstanceStatus> instanceStatuses = ec2Example.waitForStatusChangeToStopped(instanceId);
		logger.info(instanceStatuses.parallelStream().map(is -> is.getInstanceState().getName()).collect(Collectors.toList()));
	}

//	@Test
	public void testTerminateInstance() {
		ec2Example.terminateInstance(instanceId);
		List<InstanceStatus> instanceStatuses = ec2Example.waitForStatusChangeToTerminated(instanceId);
		logger.info(instanceStatuses.parallelStream().map(is -> is.getInstanceState().getName()).collect(Collectors.toList()));
	}
}
