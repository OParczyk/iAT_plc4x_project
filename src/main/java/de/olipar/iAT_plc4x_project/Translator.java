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

public class Translator extends TimerTask implements MqttCallback {
	private MQTTClientWrapper mqtt;
	private String mqttTopicPrefix;
	private OPCUAClientWrapper opc;
	private Logger logger;
	private Map<String, String> topicToNodeIDMap;

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

	public void subscribeToChannel(String topicSuffix, String nodeID) {
		topicToNodeIDMap.put(mqttTopicPrefix + topicSuffix, nodeID);
		mqtt.subscribe(mqttTopicPrefix + topicSuffix);
	}

	@Override
	public void run() {
		for (Pair<String, List<String>> values : opc.readValues()) {
			for (String value : values.getValue1()) {
				mqtt.publish(mqttTopicPrefix + values.getValue0(), value.getBytes(), 1);
			}
		}
	}

	public void connectionLost(Throwable cause) {
		logger.warning("MQTT lost connection, cause:");
		cause.printStackTrace();
		logger.warning("reconnecting");
		mqtt.connect();
	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		logger.info("Received message on " + topic + ": " + new String(message.getPayload()));

		opc.writeValue("sTestopc", topicToNodeIDMap.get(topic), new String(message.getPayload()));
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		return;

	}
}
