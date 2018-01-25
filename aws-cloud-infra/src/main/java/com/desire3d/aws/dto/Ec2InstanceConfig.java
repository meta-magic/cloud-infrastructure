package com.desire3d.aws.dto;

import java.io.Serializable;

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
			String userDataExchangePath) {
		super();
		this.instanceName = instanceName;
		this.amiId = amiId;
		this.instanceType = instanceType;
		this.subnetId = subnetId;
		this.securityGroupId = securityGroupId;
		this.localKeyStore = localKeyStore;
		this.userDataExchangePath = userDataExchangePath;
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

	public void setLocalKeyStore(String localKeyStore) {
		this.localKeyStore = localKeyStore;
	}

	public void setUserDataExchangePath(String userDataExchangePath) {
		this.userDataExchangePath = userDataExchangePath;
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