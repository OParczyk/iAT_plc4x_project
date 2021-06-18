package de.olipar.iAT_plc4x_project;

import java.util.Timer;
import java.util.logging.Logger;

public class Main {

	private static Logger logger;
	private static OPCUAClientWrapper opc;
	private static MQTTClientWrapper mqtt;
	private static String mqttTopicPrefix = "iat/test/";
	private static final long TRANSLATE_CYCLE_MS = 2000;

	public static void main(String[] args) {

		logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		opc = new OPCUAClientWrapper("opcua:tcp://localhost:4840/?discovery=false", logger);
		opc.connect();
		mqtt = new MQTTClientWrapper("tcp://test.mosquitto.org:1883", logger);
		Translator translator = new Translator(mqtt, mqttTopicPrefix, opc, logger);
		mqtt.setCallback(translator);
		mqtt.connect();

		opc.addReadItem("simulatorwert", "ns=2;s=items-lrBeltDriveSpeed:UDINT");
		// Get OPC values and sent to MQTT every TRANSLATE_CYCLE_MS
		Timer timer = new Timer();
		timer.schedule(translator, 0, TRANSLATE_CYCLE_MS);

	}

}
