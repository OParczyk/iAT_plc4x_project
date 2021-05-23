package de.olipar.iAT_plc4x_project;

import java.util.logging.Logger;

import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;

public class OPCUAClient {

	private String server_url;
	private Logger logger;

	public OPCUAClient(String server_url, Logger logger) {
		if (!server_url.startsWith("opcua:")) {
			throw new IllegalArgumentException("OPC UA server URL must start with 'opcua:'!");
		}
		this.server_url = server_url;
		this.logger = logger;
	}

}
