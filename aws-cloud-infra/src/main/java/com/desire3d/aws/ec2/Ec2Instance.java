package com.desire3d.aws.ec2;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.ResourceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TagSpecification;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.desire3d.aws.dto.Ec2InstanceConfig;

/**
 * Class to use/handle ec2 service
 * 
 * @author Mahesh Pardeshi
 *
 */
public class Ec2Instance {
	
	private static final Logger logger = LoggerFactory.getLogger(Ec2Instance.class);
	
	private final String ACCESS_KEY;
	
	private final String SECRET_KEY;
	
//	private static final String IMAGE_ID = "ami-a5f3d9c0";	
//	private static final String INSTANCE_TYPE = "t2.micro";
//	private static final String SUBNET_ID = "subnet-5f0e1c24";
//	private static final String SECURITY_GROUP_ID = "sg-594cbe32";
//	private static final String LOCAL_KEY_STORE = "/home/mahesh/platform/workspace_after_jdo/aws-example/src/main/resources/keystore/";
//	private static final String USER_DATA_EXCHANGE_PATH = "/Softwares/tc9/webapps/code-pipeline-service/WEB-INF/classes/application.properties";
	
	public static AWSCredentials credentials;
	public static AmazonEC2 amazonEC2;
	public static Regions region = Regions.US_EAST_2;
	
	public Ec2Instance(final String accessKey, final String secretKey) {
		super();
		this.ACCESS_KEY = accessKey;
		this.SECRET_KEY = secretKey;
		init();
	}

	private void init() {
		logger.info("-------------INITILIZING AMAZON EC2 SERVICE-------------");
		
		credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
		
		amazonEC2 = AmazonEC2ClientBuilder.standard()
								.withRegion(region)
								.withCredentials(new AWSStaticCredentialsProvider(credentials))
								.build();
		logger.info("-------------INITILIZATION OF AMAZON EC2 SERVICE COMPLETED-------------");
	}
	
	public void createEc2Instance(final Ec2InstanceConfig ec2InstanceConfig) {
		logger.info("-------------AMAZON EC2 INSTANCE CREATION STARTED-------------");
		try {
			this.createKeyPair(ec2InstanceConfig.getInstanceName(), ec2InstanceConfig.getLocalKeyStore());
			RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
			runInstancesRequest.withImageId(ec2InstanceConfig.getAmiId())
								.withInstanceType(ec2InstanceConfig.getInstanceType())
								.withMinCount(1)
								.withMaxCount(1)
								.withSubnetId(ec2InstanceConfig.getSubnetId())
								.withSecurityGroupIds(ec2InstanceConfig.getSecurityGroupId())
								.withKeyName(ec2InstanceConfig.getInstanceName())
								.withTagSpecifications(createTagSpecification(ec2InstanceConfig.getInstanceName()))
								.withUserData(prepareUserData(ec2InstanceConfig.getInstanceName(), ec2InstanceConfig.getUserDataExchangePath()));

			final RunInstancesResult runInstancesResult = amazonEC2.runInstances(runInstancesRequest);
			final String reservationId = runInstancesResult.getReservation().getReservationId();
			logger.info("Ec2 {} reservation based on AMI {} creation started", reservationId, ec2InstanceConfig.getAmiId());
			this.waitForStatusChange(runInstancesResult.getReservation().getInstances());
		} catch(Exception e) {
			e.printStackTrace();
		}
		logger.info("-------------AMAZON EC2 INSTANCE CREATION SUCCESSFULLY COMPLETED-------------");
	}
	
	private String prepareUserData(final String userId, final String userDataExchangePath) {
		logger.info("-------------USER DATA PREPARATION STARTED-------------");

		StringBuilder commandBuilder = new StringBuilder();
		commandBuilder.append("#!/bin/bash");
		commandBuilder.append("\n");
		commandBuilder.append("echo userid=%s >> /home/ubuntu/userdetails.txt");
		commandBuilder.append("\n");
		commandBuilder.append("sed -i \"s/@@userid/%s/g\" ");
		commandBuilder.append(userDataExchangePath);

		String script = String.format(commandBuilder.toString(), userId, userId);
		System.out.println(script);
		String encodedUserDataScript = java.util.Base64.getEncoder().encodeToString(script.getBytes());
		logger.info("-------------USER DATA PAREPARED WITH BASE64 ENCODING {}-------------", encodedUserDataScript);
		return encodedUserDataScript;
	}

	private void waitForStatusChange(final List<Instance> instances) {
		logger.info("-------------AMAZON EC2 INSTANCE WAITING FOR RUNNING STATE-------------");
		
		Set<String> instanceIds = instances.parallelStream()
											.map(instance -> instance.getInstanceId())
											.collect(Collectors.toSet());
		
		DescribeInstanceStatusRequest describeInstanceRequest = new DescribeInstanceStatusRequest()
																	.withInstanceIds(instanceIds);
		do {
			List<InstanceStatus> instanceStatusList = amazonEC2.describeInstanceStatus(describeInstanceRequest)
																.getInstanceStatuses();
			Integer runningInstanceCount = 0;
			for(InstanceStatus instanceStatus : instanceStatusList) { 
				if(instanceStatus.getInstanceState().getName().equals(InstanceStateName.Running.toString())) {
					runningInstanceCount++;
				}
			}
			if(runningInstanceCount == instanceIds.size()) {
				break;
			} else { 
				this.sleep(30);
			}
		} while (true);
		logger.info("-------------AMAZON EC2 {} INSTANCE TURNED TO RUNNING STATE-------------",instanceIds);
	}
	
	private void sleep(final long timeout) {
		try {
			TimeUnit.SECONDS.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void startInstance(final String instanceId) {
		logger.info("-------------AMAZON EC2 INSTANCE START REQUEST STARTED-------------");
		StartInstancesRequest startInstancesRequest = new StartInstancesRequest().withInstanceIds(instanceId);
		amazonEC2.startInstances(startInstancesRequest);
		logger.info("-------------AMAZON EC2 INSTANCE START REQUEST SENT-------------");
	}

	public void stopInstance(final String instanceId) {
		logger.info("-------------AMAZON EC2 INSTANCE STOP REQUEST STARTED-------------");
		StopInstancesRequest stopInstancesRequest = new StopInstancesRequest().withInstanceIds(instanceId);
		amazonEC2.stopInstances(stopInstancesRequest);
		logger.info("-------------AMAZON EC2 INSTANCE STOP REQUEST SENT-------------");
	}

	public void terminateInstance(String instanceId) {
		logger.info("-------------AMAZON EC2 INSTANCE TERMINATATION REQUEST STARTED-------------");
		TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest().withInstanceIds(instanceId);
		amazonEC2.terminateInstances(terminateInstancesRequest);
		logger.info("-------------AMAZON EC2 INSTANCE STOP REQUEST SENT-------------");
	}
	
	/** METHOD TO HANDLE KEY PAIR OPERATIONS */
	public void createKeyPair(final String keyName, final String localKeyStore) {
		logger.info("-------------AMAZON EC2 INSTANCE KEY-PAIR CREATION STARTED-------------");
		CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest().withKeyName(keyName);
		CreateKeyPairResult createKeyPairResult = amazonEC2.createKeyPair(createKeyPairRequest);
		String privateKey = createKeyPairResult.getKeyPair().getKeyMaterial();
		this.writePemFile(keyName, privateKey, localKeyStore);
		logger.info("-------------AMAZON EC2 INSTANCE KEY-PAIR CREATION COMPLETED-------------");
	}
	
	@SuppressWarnings("unused")
	private List<KeyPairInfo> getKeyPairs() {
		DescribeKeyPairsResult describeKeyPairsResult = amazonEC2.describeKeyPairs();
		return describeKeyPairsResult.getKeyPairs();
	}
	
	private void writePemFile(final String keyName, final String privateKey, final String localKeyStore) {
		logger.info("-------------KEY-PAIR FILE WRITING STARTED-------------");
		try {
			String fileName = new StringBuilder(localKeyStore)
									.append(keyName)
									.append(".pem")
									.toString();
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			writer.print(privateKey);
			writer.close();
			logger.info("-------------KEY-PAIR FILE {} WRITEN-------------", fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private TagSpecification createTagSpecification(String instanceName) {
		return new TagSpecification()
				.withResourceType(ResourceType.Instance)
				.withTags(new Tag("Name", instanceName));
	}
	
	public List<Instance> getRunningInstances() {
		logger.info("-------------REQUEST TO GET AMAZON EC2 RUNNING INSTANCE STARTED-------------");
		List<Instance> runningInstances = new ArrayList<Instance>();
		try {
			DescribeInstancesResult result = amazonEC2.describeInstances();
			List<Reservation> reservations = result.getReservations();
			for (Reservation reservation : reservations) {
				List<Instance> instances = reservation.getInstances()
														.stream()
														.filter(instance -> instance.getState().getName().equals(InstanceStateName.Running.toString()))
														.collect(Collectors.toList());
				runningInstances.addAll(instances);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("{} Running Instances Available", runningInstances.size());
		logger.info("-------------REQUEST TO GET AMAZON EC2 RUNNING INSTANCE COMPLETED-------------");
		return runningInstances;
	}
}
