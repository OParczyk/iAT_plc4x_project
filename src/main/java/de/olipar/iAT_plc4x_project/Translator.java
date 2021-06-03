package de.olipar.iAT_plc4x_project;

import java.util.List;
import java.util.TimerTask;

import org.javatuples.Pair;

public class Translator extends TimerTask {
	private MyMQTTClient mqtt;
	private String mqttTopicPrefix;
	private OPCUAClient opc;

	public Translator(MyMQTTClient mqtt, String mqttTopicPrefix, OPCUAClient opc) {
		if (mqtt == null) {
			throw new NullPointerException("MQTT client MUST NOT be null!");
		}
		if (mqttTopicPrefix == null) {
			throw new NullPointerException("MQTT topic prefix MUST NOT be null!");
		}
		if (opc == null) {
			throw new NullPointerException("OPC client MUST NOT be null!");
		}
		this.mqtt = mqtt;
		this.mqttTopicPrefix = mqttTopicPrefix;
		this.opc = opc;
	}

	@Override
	public void run() {
		for (Pair<String, List<String>> values : opc.readValues()) {
			for (String value : values.getValue1()) {
				mqtt.publish(mqttTopicPrefix + values.getValue0(), value.getBytes(), 2);
			}
		}
	}
}
