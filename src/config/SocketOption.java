package config;

public class SocketOption {
	private String serverIp;
	private int serverReliablePort;
	private int serverNonReliablePort;
	private int clientReliablePort;
	private int clientNonReliablePort;

	private int clientReliableSegSize;
	private int clientReliableSleepTime;
	private int clientReliableTimeout;

	private int clientNonReliableSegSize;
	private int clientNonReliableSleepTime;
	private int clientNonReliableTimeout;

	private int connectReRequestNum;
	private boolean autoConnect;

	public SocketOption() {

	}

	public SocketOption(String serverIp, int serverReliablePort, int serverNonReliablePort, int clientReliablePort,
			int clientNonReliablePort, int clientReliableSegSize, int clientReliableSleepTime,
			int clientReliableTimeout, int clientNonReliableSegSize, int clientNonReliableSleepTime,
			int clientNonReliableTimeout, int connectReRequestNum, boolean autoConnect) {
		this.setServerIp(serverIp);
		this.setServerReliablePort(serverReliablePort);
		this.setServerNonReliablePort(serverNonReliablePort);
		this.clientReliablePort = clientReliablePort;
		this.clientNonReliablePort = clientNonReliablePort;
		this.clientReliableSegSize = clientReliableSegSize;
		this.clientReliableSleepTime = clientReliableSleepTime;
		this.clientReliableTimeout = clientReliableTimeout;
		this.clientNonReliableSegSize = clientNonReliableSegSize;
		this.clientNonReliableSleepTime = clientNonReliableSleepTime;
		this.clientNonReliableTimeout = clientNonReliableTimeout;
		this.connectReRequestNum = connectReRequestNum;
		this.autoConnect = autoConnect;
	}

	public static SocketOption getDefaultOption() {
		SocketOption socketOption = new SocketOption("163.180.117.118", 3001, 3002, 2001, 2002, 1024, 30, 2000, 1024,
				30, 1000, 5, false);
		return socketOption;
	}

	public int getClientNonReliableSleepTime() {
		return clientNonReliableSleepTime;
	}

	public void setClientNonReliableSleepTime(int clientNonReliableSleepTime) {
		this.clientNonReliableSleepTime = clientNonReliableSleepTime;
	}

	public int getClientReliableSleepTime() {
		return clientReliableSleepTime;
	}

	public void setClientReliableSleepTime(int clientReliableSleepTime) {
		this.clientReliableSleepTime = clientReliableSleepTime;
	}

	public int getClientNonReliableSegSize() {
		return clientNonReliableSegSize;
	}

	public void setClientNonReliableSegSize(int clientNonReliableSegSize) {
		this.clientNonReliableSegSize = clientNonReliableSegSize;
	}

	public int getClientReliablePort() {
		return clientReliablePort;
	}

	public void setClientReliablePort(int clientReliablePort) {
		this.clientReliablePort = clientReliablePort;
	}

	public int getClientNonReliablePort() {
		return clientNonReliablePort;
	}

	public void setClientNonReliablePort(int clientNonReliablePort) {
		this.clientNonReliablePort = clientNonReliablePort;
	}

	public int getClientReliableSegSize() {
		return clientReliableSegSize;
	}

	public void setClientReliableSegSize(int clientReliableSegSize) {
		this.clientReliableSegSize = clientReliableSegSize;
	}

	public int getClientReliableTimeout() {
		return clientReliableTimeout;
	}

	public void setClientReliableTimeout(int clientReliableTimeout) {
		this.clientReliableTimeout = clientReliableTimeout;
	}

	public int getClientNonReliableTimeout() {
		return clientNonReliableTimeout;
	}

	public void setClientNonReliableTimeout(int clientNonReliableTimeout) {
		this.clientNonReliableTimeout = clientNonReliableTimeout;
	}

	public boolean isAutoConnect() {
		return autoConnect;
	}

	public void setAutoConnect(boolean autoConnect) {
		this.autoConnect = autoConnect;
	}

	public int getConnectReRequestNum() {
		return connectReRequestNum;
	}

	public void setConnectReRequestNum(int connectReRequestNum) {
		this.connectReRequestNum = connectReRequestNum;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public int getServerReliablePort() {
		return serverReliablePort;
	}

	public void setServerReliablePort(int serverReliablePort) {
		this.serverReliablePort = serverReliablePort;
	}

	public int getServerNonReliablePort() {
		return serverNonReliablePort;
	}

	public void setServerNonReliablePort(int serverNonReliablePort) {
		this.serverNonReliablePort = serverNonReliablePort;
	}
}
