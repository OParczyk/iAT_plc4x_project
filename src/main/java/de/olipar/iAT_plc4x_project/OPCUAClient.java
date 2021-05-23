package de.olipar.iAT_plc4x_project;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.javatuples.Pair;

public class OPCUAClient {

	private String server_url;
	private Logger logger;
	private PlcConnection plcConnection = null;
	private List<Pair<String, String>> readItemList;

	public OPCUAClient(String server_url, Logger logger) {
		if (!server_url.startsWith("opcua:")) {
			throw new IllegalArgumentException("OPC UA server URL must start with 'opcua:'!");
		}
		this.server_url = server_url;
		this.logger = logger;
		readItemList = new LinkedList();
	}

	public void connect() {
		try {
			PlcConnection plcConnection = new PlcDriverManager().getConnection(server_url);
			logger.info("Connected to OPC UA server");
			this.plcConnection = plcConnection;
		} catch (PlcConnectionException e) {
			logger.warning("Connection to " + server_url + " failed: " + e.getMessage());
		}
	}

	public void addReadItem(String name, String nodeID) {
		if (name == null) {
			throw new NullPointerException("Item name MUST NOT be null!");
		}
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Item name MUST NOT be empty");
		}
		if (nodeID == null) {
			throw new NullPointerException("nodeID MUST NOT be null!");
		}
		if (nodeID.isEmpty()) {
			throw new IllegalArgumentException("nodeID MUST NOT be empty");
		}
		readItemList.add(new Pair<String, String>(name, nodeID));
	}
}
