package de.olipar.iAT_plc4x_project;

import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MyMQTTClient {
	private String clientID;
	private MqttClient client;
	private Logger logger;

	public MyMQTTClient(String brokerURL, Logger logger) {
		if (brokerURL == null) {
			throw new NullPointerException("MQTT broker URL MUST NOT be null!");
		}
		if (logger == null) {
			throw new NullPointerException("Logger MUST NOT be null!");
		}

		this.logger = logger;
		clientID = MqttAsyncClient.generateClientId();
		MemoryPersistence persistence = new MemoryPersistence();

		try {
			client = new MqttClient(brokerURL, clientID, persistence);
		} catch (MqttException e) {
			logger.warning("An MQTT Exception occurred: " + e.getMessage());
			logger.warning("Caused by " + e.getCause());
		}
	}

	public void publish(String topic, byte[] payload, int qos) {
		if (topic == null) {
			throw new NullPointerException("Topic for MQTT message MUST NOT be null!");
		}
		if (topic.isEmpty()) {
			throw new IllegalArgumentException("Topic for MQTT message MUST NOT be empty!");
		}
		if (payload == null) {
			throw new NullPointerException("MQTT message payload MUST NOT be null!");
		}
		if (qos < 0 || qos > 2) {
			throw new IllegalArgumentException("MQTT QoS must be 0, 1 or 2");
		}

		MqttMessage message = new MqttMessage(payload);
		message.setQos(qos);
		try {
			client.publish(topic, message);
		} catch (MqttPersistenceException e) {
			logger.warning("A persistence Exception occurred: " + e.getMessage());
			logger.warning("Caused by " + e.getCause());
			logger.warning("Message might not have been published.");
		} catch (MqttException e) {
			logger.warning("An MQTT Exception occurred while publishing a message: " + e.getMessage());
			logger.warning("Caused by " + e.getCause());
			logger.warning("Message might not have been published.");
		}
	}
}
