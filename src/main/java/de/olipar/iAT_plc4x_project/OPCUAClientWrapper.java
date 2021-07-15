package de.olipar.iAT_plc4x_project;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.exceptions.PlcUnsupportedOperationException;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.javatuples.Pair;

/**
 * Wrapper for the PLC4X OPC UA client. Deals with to be expected exceptions.
 *
 * @author Oliver Parczyk
 * @version 1.0
 * @since 1.0
 */
public class OPCUAClientWrapper {

	private String server_url;
	private Logger logger;
	private PlcConnection plcConnection;
	private List<Pair<String, String>> readItemList;

	/**
	 * Instantiates a new Wrapper for the OPC UA Client of PLC4X
	 *
	 * @param server_url The OPC UA server URL to connect to
	 * @param logger     A java.util.logging.Logger object
	 */
	public OPCUAClientWrapper(String server_url, Logger logger) {
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

	/**
	 * Open connection to OPC UA server using PLC4X
	 */
	public void connect() {
		try {
			PlcConnection plcConnection = new PlcDriverManager().getConnection(server_url);
			logger.info("Connected to OPC UA server");
			this.plcConnection = plcConnection;
		} catch (PlcConnectionException e) {
			logger.warning("Connection to " + server_url + " failed: " + e.getMessage());
		}
	}

	/**
	 * Adds an Item to be read to the requests to the OPC UA server
	 *
	 * @param name   A name given to this read Item
	 * @param nodeID the nodeID to be read
	 */
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

	/**
	 * Retrieves previously added values from the OPC UA server Will convert all
	 * values to string before returning, Casting back should be possible if
	 * necessary. Handling all types differently is highly impractical here.
	 *
	 * @return A List of Pairs tying the name of the request to the response(s)
	 */
	public List<Pair<String, List<String>>> readValues() {

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

	/**
	 * Sends a write request to selected nodeIDs
	 *
	 * @param fieldName A name given to this write Item
	 * @param nodeID    The nodeID to be written to
	 * @param values    The values that shall be written to the nodeID
	 */
	public void writeValue(String fieldName, String nodeID, Object... values) {
		logger.info("Writing to " + nodeID);
		if (fieldName == null) {
			throw new NullPointerException("fieldName MUST NOT be null!");
		}
		if (fieldName.isEmpty()) {
			throw new IllegalArgumentException("fieldName MUST NOT be empty");
		}
		if (nodeID == null) {
			throw new NullPointerException("nodeID MUST NOT be null!");
		}
		if (nodeID.isEmpty()) {
			throw new IllegalArgumentException("nodeID MUST NOT be empty");
		}
		if (values == null) {
			throw new NullPointerException("values MUST NOT be null!");
		}

		PlcWriteRequest.Builder writeRequestBuilder;
		PlcWriteResponse response = null;
		if (!plcConnection.isConnected()) {
			logger.warning("We aren't conntected to opc ua. Trying to connect...");
			connect();
			if (!plcConnection.isConnected()) {
				logger.warning("Reconnect to opc unsuccessful.");
				return;
			}
		}
		try {
			writeRequestBuilder = plcConnection.writeRequestBuilder();
		} catch (PlcUnsupportedOperationException e) {
			logger.warning("We cannot write to " + server_url);
			logger.warning("Because of: " + e.getMessage());
			return;
		}
		writeRequestBuilder.addItem(fieldName, nodeID, values[0]);
		PlcWriteRequest request = writeRequestBuilder.build();
		try {
			// blocking is fine since this will be called indirectly by mqtt asynchronously.
			response = request.execute().get();
		} catch (InterruptedException e) {
			logger.warning("Interrupt occured during write request to opc ua server");
		} catch (ExecutionException e) {
			logger.warning("Execution of write request to opc ua server was unsuccessful:");
			logger.warning(e.getCause().getMessage());
		}

		if (response.getResponseCode(fieldName) == PlcResponseCode.OK) {
			logger.info("Value[" + fieldName + "] successfully updated");
		} else {
			logger.warning("Error[" + fieldName + "]: " + response.getResponseCode(fieldName).name());
		}
	}
}
