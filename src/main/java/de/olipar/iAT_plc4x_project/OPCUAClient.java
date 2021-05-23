package de.olipar.iAT_plc4x_project;

import java.util.logging.Logger;

public class OPCUAClient {

	private String server_url;

	public OPCUAClient(String server_url, Logger logger) {
		if (!server_url.startsWith("opcua:")) {
			throw new IllegalArgumentException("OPC UA server URL must start with 'opcua:'!");
		}
		this.server_url = server_url;
	}
}
