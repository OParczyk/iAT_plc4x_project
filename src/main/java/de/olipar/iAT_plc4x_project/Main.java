package de.olipar.iAT_plc4x_project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.javatuples.Pair;

public class Main extends TimerTask implements MqttCallback {

	private static Logger logger;
	private static OPCUAClient opc;
	private static MyMQTTClient mqtt;
	private static String mqttTopicPrefix = "iat/test/";
	private static final long TRANSLATE_CYCLE_MS = 2000;
	private static Map<String, String> topicToNodeIDMap;

	public static void main(String[] args) {
		logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		opc = new OPCUAClient("opcua:tcp://localhost:12686/", logger);
		opc.connect();
		mqtt = new MyMQTTClient("tcp://test.mosquitto.org:1883", logger);
		mqtt.setCallback(new Main());
		mqtt.connect();

		topicToNodeIDMap = new HashMap<String, String>();

		opc.addReadItem("simulatorwert", "ns=2;s=items-lrBeltDriveSpeed:UDINT");
		// Get OPC values and sent to MQTT every TRANSLATE_CYCLE_MS
		Timer timer = new Timer();
		timer.schedule(new Main(), 0, TRANSLATE_CYCLE_MS);

	}

	private void subscribeToChannel(String topicSuffix, String nodeID) {
		topicToNodeIDMap.put(mqttTopicPrefix + topicSuffix, nodeID);
		mqtt.subscribe(mqttTopicPrefix + topicSuffix);
	}

	@Override
	public void run() {
		for (Pair<String, List<String>> values : opc.readValues()) {
			for (String value : values.getValue1()) {
				mqtt.publish(mqttTopicPrefix + values.getValue0(), value.getBytes(), 2);
			}
		}
	}

	public void connectionLost(Throwable cause) {
		logger.warning("MQTT lost connection, retrying...");
		mqtt.connect();
	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		logger.info("Received message on " + topic + ": " + new String(message.getPayload()));

		// TODO: Check if casting is necessary.
		opc.writeValue(topic, topicToNodeIDMap.get(mqttTopicPrefix + topic), message);
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		return;

	}

}
