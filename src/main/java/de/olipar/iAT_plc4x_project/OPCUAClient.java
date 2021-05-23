package de.olipar.iAT_plc4x_project;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.exceptions.PlcUnsupportedOperationException;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.javatuples.Pair;

public class OPCUAClient {

	private String server_url;
	private Logger logger;
	private PlcConnection plcConnection;
	private List<Pair<String, String>> readItemList;

	public OPCUAClient(String server_url, Logger logger) {
		if (server_url == null) {
			throw new NullPointerException("OPC UA server URL MUST NOT be null!");
		}
		if (!server_url.startsWith("opcua:")) {
			throw new IllegalArgumentException("OPC UA server URL MUST start with 'opcua:'!");
		}
		this.server_url = server_url;
		this.logger = logger;
		readItemList = new LinkedList<Pair<String, String>>();
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

	public List<Pair<String, List<String>>> readValues() {
		// We will convert all values to string before returning.
		// Casting back should be possible if necessary
		// Handling all types differently is impractical here.

		PlcReadRequest.Builder readRequestBuilder;
		PlcReadResponse response;
		List<Pair<String, List<String>>> ret = new LinkedList<Pair<String, List<String>>>();

		if (!plcConnection.isConnected()) {
			logger.warning("We aren't conntected to opc ua. Trying to connect...");
			connect();
			if (!plcConnection.isConnected()) {
				return null;
			}
		}
		try {
			readRequestBuilder = plcConnection.readRequestBuilder();
		} catch (PlcUnsupportedOperationException e) {
			logger.warning("We cannot read from " + server_url);
			return null;
		}
		for (Pair<String, String> item : readItemList) {
			readRequestBuilder.addItem(item.getValue0(), item.getValue1());
		}
		PlcReadRequest readRequest = readRequestBuilder.build();

		try {
			response = readRequest.execute().get();
		} catch (InterruptedException e) {
			logger.warning("Interrupt occured during read request to opc ua server");
			return null;
		} catch (ExecutionException e) {
			logger.warning("Execution of read request to opc ua server was unsuccessful:");
			logger.warning(e.getCause().getMessage());
			return null;
		}
		for (String fieldName : response.getFieldNames()) {
			int numValues = response.getNumberOfValues(fieldName);
			List<String> valuesOfField = new LinkedList<String>();
			for (int i = 0; i < numValues; i++) {
				valuesOfField.add(response.getObject(fieldName, i).toString());
			}
			ret.add(new Pair<String, List<String>>(fieldName, valuesOfField));
		}

		return ret;
	}
}
