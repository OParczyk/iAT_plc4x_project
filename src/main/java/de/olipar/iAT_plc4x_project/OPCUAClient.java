package de.olipar.iAT_plc4x_project;

import java.util.logging.Logger;

import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;

public class OPCUAClient {

	private String server_url;
	private Logger logger;
	private boolean is_connected = false;
	private PlcConnection plcConnection = null;

	public OPCUAClient(String server_url, Logger logger) {
		if (!server_url.startsWith("opcua:")) {
			throw new IllegalArgumentException("OPC UA server URL must start with 'opcua:'!");
		}
		this.server_url = server_url;
		this.logger = logger;
	}

	public void connect() {
		try {
			PlcConnection plcConnection = new PlcDriverManager().getConnection(server_url);
			logger.info("Connected to OPC UA server");
			this.is_connected = true;
			this.plcConnection = plcConnection;
		} catch (PlcConnectionException e) {
			logger.warning("Connection to " + server_url + " failed: " + e.getMessage());
		}
	}
}
