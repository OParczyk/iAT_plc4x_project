package de.olipar.iAT_plc4x_project;

import java.util.logging.Logger;

public class Main {

	private static Logger logger;
	private static OPCUAClient opc;
	private static MyMQTTClient mqtt;
	private static String mqttTopicPrefix = "iat/test";
	private static final long TRANSLATE_CYCLE_MS = 2000;

	public static void main(String[] args) {
		logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		opc = new OPCUAClient("opcua:tcp://localhost:12686/", logger);
		opc.connect();
		mqtt = new MyMQTTClient("tcp://test.mosquitto.org:1883", logger);
		mqtt.connect();

		opc.addReadItem("simulatorwert", "ns=2;s=items-lrBeltDriveSpeed:UDINT");

	}

}
