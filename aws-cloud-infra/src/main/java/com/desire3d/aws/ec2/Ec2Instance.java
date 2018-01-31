package com.desire3d.aws.ec2;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
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
	
	private static final Logger logger = org.apache.log4j.Logger.getLogger(Ec2Instance.class);
	
	private final String ACCESS_KEY;
	private final String SECRET_KEY;
	
	public static AWSCredentials credentials;
	public static AmazonEC2 amazonEC2;
	private Regions region;
	
	public Ec2Instance(final String accessKey, final String secretKey, final String regionName) {
		super();
		this.ACCESS_KEY = accessKey;
		this.SECRET_KEY = secretKey;
		this.region = Regions.fromName(regionName);
		init();
	}

	private void init() {
		logger.info("-------------INITILIZING AMAZON EC2 SERVICE-------------");
		
		credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
		
		AmazonEC2ClientBuilder amazonEC2ClientBuilder = AmazonEC2ClientBuilder.standard()
									.withCredentials(new AWSStaticCredentialsProvider(credentials));
		if(region != null) {
			amazonEC2ClientBuilder.withRegion(region);
		}
		
		amazonEC2 = amazonEC2ClientBuilder.build();
		logger.info("-------------INITILIZATION OF AMAZON EC2 SERVICE COMPLETED-------------");
	}
	
	public List<Instance> createInstance(final Ec2InstanceConfig ec2InstanceConfig) {
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
			logger.info("-------------AMAZON EC2 INSTANCE CREATION SUCCESSFULLY COMPLETED-------------");
			return runInstancesResult.getReservation().getInstances();
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
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
	
	/**
	 * method to get a single instance by id
	 * 
	 * @param instanceId
	 * @return {@link Instance}
	 * */
	public Instance getInstanceById(final String instanceId) {
		final DescribeInstancesResult describeInstancesResult = amazonEC2.describeInstances(new DescribeInstancesRequest());
		for(Reservation reservation : describeInstancesResult.getReservations()) {
			for(Instance instance : reservation.getInstances()) {
				if(instance.getInstanceId().equals(instanceId)) {
					return instance;
				}
			}
		}
		return null;
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
		logger.info(runningInstances.size() + " Running Instances Available");
		logger.info("-------------REQUEST TO GET AMAZON EC2 RUNNING INSTANCE COMPLETED-------------");
		return runningInstances;
	}
	

	/**
	 * method to get current status on instances
	 * @param instances
	 * @return {@link InstanceStatus}
	 * */
	public List<InstanceStatus> getInstanceStatus(final Collection<Instance> instances) {
		return amazonEC2.describeInstanceStatus(new DescribeInstanceStatusRequest()
													.withIncludeAllInstances(true)
													.withInstanceIds(
															instances.parallelStream()
																	.map(instance -> instance.getInstanceId())
																	.collect(Collectors.toSet())
												)
											).getInstanceStatuses();
	}
	
	/**
	 * method to get current status on instances
	 * @param instances
	 * @return {@link InstanceStatus}
	 * */
	public List<InstanceStatus> getInstanceStatus(final String... instanceIds) {
		return amazonEC2.describeInstanceStatus(new DescribeInstanceStatusRequest()
													.withIncludeAllInstances(true)
													.withInstanceIds(instanceIds)
											).getInstanceStatuses();
	}
	
	/** 
	 * method used to wait until instances states changed to running
	 * 
	 * @param instanceIds
	 * @return {@link InstanceStatus}   
	 * */
	public List<InstanceStatus> waitForStatusChangeToRunning(final Collection<String> instanceIds) {
		return waitForStatusChange(InstanceStateName.Running.toString(), instanceIds);
	}
	
	/** 
	 * method used to wait until state changed to running
	 * 
	 * @param instanceIds
	 * @return {@link InstanceStatus}   
	 * */
	public List<InstanceStatus> waitForStatusChangeToRunning(final String... instanceIds) {
		return waitForStatusChange(InstanceStateName.Running.toString(), instanceIds);
	}

	/** 
	 * method used to wait until instances states changed to running
	 * 
	 * @param instanceIds
	 * @return {@link InstanceStatus}   
	 * */
	public List<InstanceStatus> waitForStatusChangeToStopped(final Collection<String> instanceIds) {
		return waitForStatusChange(InstanceStateName.Stopped.toString(), instanceIds);
	}
	
	/** 
	 * method used to wait until instances states changed to running
	 * 
	 * @param instanceIds
	 * @return {@link InstanceStatus}   
	 * */
	public List<InstanceStatus> waitForStatusChangeToStopped(final String... instanceIds) {
		return waitForStatusChange(InstanceStateName.Stopped.toString(), instanceIds);
	}

	/** 
	 * method used to wait until instances states changed to running
	 * 
	 * @param instances
	 * @return {@link InstanceStatus}   
	 * */
	public List<InstanceStatus> waitForStatusChangeToTerminated(final java.util.Collection<String> instanceIds) {
		return waitForStatusChange(InstanceStateName.Terminated.toString(), instanceIds);
	}
	
	/** 
	 * method used to wait until instances states changed to running
	 * 
	 * @param instanceIds
	 * @return {@link InstanceStatus}   
	 * */
	public List<InstanceStatus> waitForStatusChangeToTerminated(final String... instanceIds) {
		return waitForStatusChange(InstanceStateName.Terminated.toString(), instanceIds);
	}
	
	private List<InstanceStatus> waitForStatusChange(final String stateName, java.util.Collection<String> instanceIds) {
		return this.requestWait(stateName, new DescribeInstanceStatusRequest()
						.withIncludeAllInstances(true)
						.withInstanceIds(instanceIds)
					);
	}
	
	private List<InstanceStatus> waitForStatusChange(final String stateName, String... instanceIds) {
		return this.requestWait(stateName, new DescribeInstanceStatusRequest()
						.withIncludeAllInstances(true)
						.withInstanceIds(instanceIds)
					);
	}
	
	private List<InstanceStatus> requestWait(final String stateName, final DescribeInstanceStatusRequest describeInstanceRequest) {
		logger.info("-------------AMAZON EC2 INSTANCE WAITING FOR " + stateName + " STATE-------------");		
		final Integer waitThreshold = 180; 
		Integer waitingTime = 0;
		
		List<InstanceStatus> instanceStatusList;
		do {
			instanceStatusList = amazonEC2.describeInstanceStatus(describeInstanceRequest)
										  .getInstanceStatuses();
			Integer runningInstanceCount = instanceStatusList.parallelStream()
											.filter(instanceStatus -> instanceStatus.getInstanceState().getName().equals(stateName))
											.collect(Collectors.toList())
											.size();
			
			if(!instanceStatusList.isEmpty() && runningInstanceCount == instanceStatusList.size()) {
				logger.info("-------------AMAZON EC2 INSTANCE WAITING TIME FOR " + stateName + " STATE HAS EXPIRED-------------");
				break;
			} else if (waitingTime >= waitThreshold) {
				break;
			} else { 
				waitingTime = waitingTime + 20;
				this.sleep(20);
			}
		} while (true);
		logger.info("-------------AMAZON EC2 INSTANCE TURNED TO " + stateName + " STATE-------------");
		return instanceStatusList;
	}
	
	private void sleep(final long timeout) {
		try {
			TimeUnit.SECONDS.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
		String encodedUserDataScript = java.util.Base64.getEncoder().encodeToString(script.getBytes());
		logger.info("-------------USER DATA PAREPARED WITH BASE64 ENCODING "+ encodedUserDataScript +"-------------");
		return encodedUserDataScript;
	}
	
	/** METHOD TO HANDLE KEY PAIR OPERATIONS */
	private void createKeyPair(final String keyName, final String localKeyStore) {
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
									.append("/")
									.append(keyName)
									.append(".pem")
									.toString();
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			writer.print(privateKey);
			writer.close();
			logger.info("-------------KEY-PAIR FILE " + fileName + " WRITEN-------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private TagSpecification createTagSpecification(String instanceName) {
		return new TagSpecification()
				.withResourceType(ResourceType.Instance)
				.withTags(new Tag("Name", instanceName));
	}
}