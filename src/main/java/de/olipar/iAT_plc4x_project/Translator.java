package de.olipar.iAT_plc4x_project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.javatuples.Pair;

/**
 * Handles translation between OPC UA and MQTT. Does so by offering callbacks
 * for MQTT and timing OPC UA Polling. Contains a Map to get from MQTT topic to
 * OPC UA nodeID.
 *
 * @author Oliver Parczyk
 * @version 1.0
 * @since 1.0
 */
public class Translator extends TimerTask implements MqttCallback {
	private MQTTClientWrapper mqtt;
	private String mqttTopicPrefix;
	private OPCUAClientWrapper opc;
	private Logger logger;
	private Map<String, String> topicToNodeIDMap;

	/**
	 * Instantiates a new MQTT<->OPC UA translator.
	 *
	 * @param mqtt            A MQTTClientWrapper object for communication with MQTT
	 * @param mqttTopicPrefix The Prefix used for all MQTT channels
	 * @param opc             the A OPCUAClientWrapper object for communication with
	 *                        OPC UA
	 * @param logger          A java.util.logging.Logger object
	 */
	public Translator(MQTTClientWrapper mqtt, String mqttTopicPrefix, OPCUAClientWrapper opc, Logger logger) {
		if (mqtt == null) {
			throw new NullPointerException("MQTT client MUST NOT be null!");
		}
		if (mqttTopicPrefix == null) {
			throw new NullPointerException("MQTT topic prefix MUST NOT be null!");
		}
		if (opc == null) {
			throw new NullPointerException("OPC client MUST NOT be null!");
		}
		if (logger == null) {
			throw new NullPointerException("Logger MUST NOT be null!");
		}
		this.mqtt = mqtt;
		this.mqttTopicPrefix = mqttTopicPrefix;
		this.opc = opc;
		this.logger = logger;
		topicToNodeIDMap = new HashMap<String, String>();
	}

	/**
	 * Subscribes to a MQTT channel and adds it's topic to the translation map
	 *
	 * @param topicSuffix The suffix of the topic to subscribe to
	 * @param nodeID      The nodeID of the corresponding OPC UA node
	 */
	public void subscribeToChannel(String topicSuffix, String nodeID) {
		topicToNodeIDMap.put(mqttTopicPrefix + topicSuffix, nodeID);
		mqtt.subscribe(mqttTopicPrefix + topicSuffix);
	}

	/**
	 * Extension of TimerTask. Used for timed translation from OPC UA to MQTT
	 */
	@Override
	public void run() {
		for (Pair<String, List<String>> values : opc.readValues()) {
			for (String value : values.getValue1()) {
				mqtt.publish(mqttTopicPrefix + values.getValue0(), value.getBytes(), 1);
			}
		}
	}

	/**
	 * Implementation of MqttCallback. Logs the cause of connection loss and tries
	 * to immediately reconnect
	 *
	 * @param cause The cause of connection loss
	 */
	public void connectionLost(Throwable cause) {
		logger.warning("MQTT lost connection, cause:");
		cause.printStackTrace();
		logger.warning("reconnecting");
		mqtt.connect();
	}

	/**
	 * Implementation of MqttCallback. Handles translation of incoming MQTT messages
	 * to OPC UA
	 *
	 * @param topic   The topic the MQTT message arrived on
	 * @param message The message received on MQTT
	 */
	public void messageArrived(String topic, MqttMessage message) {
		logger.info("Received message on " + topic + ": " + new String(message.getPayload()));

		opc.writeValue("sTestopc", topicToNodeIDMap.get(topic), new String(message.getPayload()));
	}

	/**
	 * Implementation of MqttCallback. Just returns, does nothing.
	 *
	 * @param token The delivery token that completed
	 */
	public void deliveryComplete(IMqttDeliveryToken token) {
		return;

	}
}
