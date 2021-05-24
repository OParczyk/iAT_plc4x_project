package de.olipar.iAT_plc4x_project;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.javatuples.Pair;

public class Main extends TimerTask {

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
		// Get OPC values and sent to MQTT every TRANSLATE_CYCLE_MS
		Timer timer = new Timer();
		timer.schedule(new Main(), 0, TRANSLATE_CYCLE_MS);

	}

	@Override
	public void run() {
		for (Pair<String, List<String>> values : opc.readValues()) {
			for (String value : values.getValue1()) {
				mqtt.publish(mqttTopicPrefix + "/" + values.getValue0(), value.getBytes(), 2);
			}
		}
	}

}
