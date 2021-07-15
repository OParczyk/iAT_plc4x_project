package de.olipar.iAT_plc4x_project;

import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Wrapper for the Paho MQTT client. Deals with to be expected exceptions.
 *
 * @author Oliver Parczyk
 * @version 1.0
 * @since 1.0
 */
public class MQTTClientWrapper {
	private String clientID;
	private MqttClient client;
	private Logger logger;

	/**
	 * Instantiates a new Wrapper for the Paho MQTT Client
	 *
	 * @param brokerURL The MQTT broker URL
	 * @param logger    A java.util.logging.Logger object
	 */
	public MQTTClientWrapper(String brokerURL, Logger logger) {
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

	/**
	 * Open connection to the MQTT broker using Paho MQTT
	 */
	public void connect() {
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		try {
			client.connect(connOpts);
		} catch (MqttSecurityException e) {
			logger.warning("An MQTT Security Exception occurred connecting to broker: " + e.getMessage());
			logger.warning("Caused by " + e.getCause());
			logger.warning("Connection could not be established.");
		} catch (MqttException e) {
			logger.warning("An MQTT Exception occurred while connecting to broker: " + e.getMessage());
			logger.warning("Caused by " + e.getCause());
			logger.warning("Connection could not be established.");
		}
	}

	/**
	 * Publishes a payload to a topic
	 *
	 * @param topic   the topic to be published to
	 * @param payload the payload to be published
	 * @param qos     the MQTT Quality of service of this message. Either 0, 1 or 2.
	 */
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
		if (!client.isConnected()) {
			connect();
			if (!client.isConnected()) {
				return;
			}
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
		logger.info("Published '" + new String(payload) + "' to '" + topic);
	}

	/**
	 * Sets the callback function for Subscriptions
	 *
	 * @param callback MqttCallback implementation
	 */
	public void setCallback(MqttCallback callback) {
		if (callback == null) {
			throw new NullPointerException("Mqtt Callback MUST NOT be null!");
		}
		client.setCallback(callback);
	}

	/**
	 * Subscribes to given MQTT topic
	 *
	 * @param topic The topic to subscribe to
	 */
	public void subscribe(String topic) {
		if (topic == null) {
			throw new NullPointerException("Topic for MQTT message MUST NOT be null!");
		}
		if (topic.isEmpty()) {
			throw new IllegalArgumentException("Topic for MQTT message MUST NOT be empty!");
		}
		try {
			client.subscribe(topic);
		} catch (MqttException e) {
			logger.warning("An MQTT Exception occurred while subscribing to " + topic + ": " + e.getMessage());
			logger.warning("Caused by " + e.getCause());
			logger.warning("Subscription might not have been successful.");
		}
	}
}
