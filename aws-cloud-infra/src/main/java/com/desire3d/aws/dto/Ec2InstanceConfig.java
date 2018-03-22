package com.desire3d.aws.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * Class to supply aws cloud ec2 instance configuration to instances
 * 
 * @author Mahesh Pardeshi
 *
 */
public class Ec2InstanceConfig implements Serializable {

	private static final long serialVersionUID = -8490023840042408372L;

	private final String instanceName;

	private final String amiId;

	private final String instanceType;

	private final String subnetId;

	private final String securityGroupId;

	private String localKeyStore;

	private String userDataExchangePath;
	
	private Map<String, Object> userData;
	
	/** CONSTANTS FOR USER DATA */
	public interface UserDataConstants {
		
		String PERSON_ID = "person.id";
		String USER_ID = "user.id";
		String USER_NAME = "user.name";
		String ORGANIZATION_NAME = "organization.name";

	}
	
	/**
	 * @param instanceName
	 * @param amiId
	 * @param instanceType
	 * @param subnetId
	 * @param securityGroupId
	 */
	public Ec2InstanceConfig(String instanceName, String amiId, String instanceType, String subnetId, String securityGroupId) {
		super();
		this.instanceName = instanceName;
		this.amiId = amiId;
		this.instanceType = instanceType;
		this.subnetId = subnetId;
		this.securityGroupId = securityGroupId;
	}

	/**
	 * @param instanceName
	 * @param amiId
	 * @param instanceType
	 * @param subnetId
	 * @param securityGroupId
	 * @param localKeyStore
	 * @param userDataExchangePath
	 */
	public Ec2InstanceConfig(String instanceName, String amiId, String instanceType, String subnetId, String securityGroupId, String localKeyStore,
			String userDataExchangePath, Map<String, Object> userData) {
		super();
		this.instanceName = instanceName;
		this.amiId = amiId;
		this.instanceType = instanceType;
		this.subnetId = subnetId;
		this.securityGroupId = securityGroupId;
		this.localKeyStore = localKeyStore;
		this.userDataExchangePath = userDataExchangePath;
		this.userData = userData;
	}

	/** 
	 * method to set local key store path to save instance access key's 
	 * */
	public Ec2InstanceConfig withLocalKeyStore(final String localKeyStore) {
		setLocalKeyStore(localKeyStore);
		return this;
	}

	public Ec2InstanceConfig withUserDataExchangePath(final String userDataExchangePath) {
		setUserDataExchangePath(userDataExchangePath);
		return this;
	}
	
	public Ec2InstanceConfig withUserData(Map<String, Object> userData) {
		setUserData(userData);
		return this;
	}

	public void setLocalKeyStore(String localKeyStore) {
		this.localKeyStore = localKeyStore;
	}

	public void setUserDataExchangePath(String userDataExchangePath) {
		this.userDataExchangePath = userDataExchangePath;
	}
	
	public Map<String, Object> getUserData() {
		return userData;
	}

	public void setUserData(Map<String, Object> userData) {
		this.userData = userData;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public String getAmiId() {
		return amiId;
	}

	public String getInstanceType() {
		return instanceType;
	}

	public String getSubnetId() {
		return subnetId;
	}

	public String getSecurityGroupId() {
		return securityGroupId;
	}

	public String getLocalKeyStore() {
		return localKeyStore;
	}

	public String getUserDataExchangePath() {
		return userDataExchangePath;
	}
}