/*
 Easy talking with PLC4X Practical Demonstration, ESP32 Part
Copyright (C) 2021 Oliver Parczyk

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
 * Most of the readout functionality for the DHT11 temperature sensor is based on
 * or taken from the example project "DHT_ESP32" from the DHTesp library licensed under GPLv3.
 * The version used can be found here:
 * https://github.com/beegee-tokyo/DHTesp/blob/affec2841444f06b3c64dc023aee8e9ff20bbec0/examples/DHT_ESP32/DHT_ESP32.ino
 * Last accessed on the 19th of July 2021
 * Archived here:
 * https://web.archive.org/web/20210719143957/https://github.com/beegee-tokyo/DHTesp/blob/affec2841444f06b3c64dc023aee8e9ff20bbec0/examples/DHT_ESP32/DHT_ESP32.ino
 * 
 * Much of the MQTT sending and receiving functionality is based on the example project
 * "SimpleMQTTClient" from the EspMQTTClient library licensed under GPLv3.
 * The version used can be found here:
 * https://github.com/plapointe6/EspMQTTClient/blob/d54a898b9a14d0c6e4f85db112029b198976aa23/examples/SimpleMQTTClient/SimpleMQTTClient.ino
 * Last accessed on the 19th of July 2021
 * Archived here:
 * https://web.archive.org/web/20210719144455/https://github.com/plapointe6/EspMQTTClient/blob/d54a898b9a14d0c6e4f85db112029b198976aa23/examples/SimpleMQTTClient/SimpleMQTTClient.ino
 */
#include "DHTesp.h"
#include <Ticker.h>
#include "EspMQTTClient.h"
#define SIREN 15
#define RED 2
#define GREEN 0
#define BLUE 4
#define X 32
#define Y 33

EspMQTTClient client(
  "ssid",
  "wifi password",
  "5.196.95.208",  // MQTT Broker server ip
  "",   // Username Can be omitted if not needed
  "",   // Password Can be omitted if not needed
  "TestClient0815iAT",     // Client name that uniquely identify your device
  1883              // The MQTT port, default to 1883. this line can be omitted
);

DHTesp dht;

void tempTask(void *pvParameters);
bool getTemperature();
void triggerGetTemp();

/** Task handle for the light value read task */
TaskHandle_t tempTaskHandle = NULL;
/** Ticker for temperature reading */
Ticker tempTicker;
/** Comfort profile */
ComfortState cf;
/** Flag if task should run */
bool tasksEnabled = false;
/** Pin number for DHT11 data pin */
int dhtPin = 19;

/**
 * initTemp
 * Setup DHT library
 * Setup task and timer for repeated measurement
 * @return bool
 *    true if task and timer are started
 *    false if task or timer couldn't be started
 */
bool initTemp() {
  byte resultValue = 0;
  // Initialize temperature sensor
  dht.setup(dhtPin, DHTesp::DHT11);
  Serial.println("DHT initiated");

  // Start task to get temperature
  xTaskCreatePinnedToCore(
      tempTask,                       /* Function to implement the task */
      "tempTask ",                    /* Name of the task */
      4000,                           /* Stack size in words */
      NULL,                           /* Task input parameter */
      5,                              /* Priority of the task */
      &tempTaskHandle,                /* Task handle. */
      1);                             /* Core where the task should run */

  if (tempTaskHandle == NULL) {
    Serial.println("Failed to start task for temperature update");
    return false;
  } else {
    // Start update of environment data every 2 seconds
    tempTicker.attach(2, triggerGetTemp);
  }
  return true;
}

/**
 * triggerGetTemp
 * Sets flag dhtUpdated to true for handling in loop()
 * called by Ticker getTempTimer
 */
void triggerGetTemp() {
  if (tempTaskHandle != NULL) {
     xTaskResumeFromISR(tempTaskHandle);
  }
}

/**
 * Task to reads temperature from DHT11 sensor
 * @param pvParameters
 *    pointer to task parameters
 */
void tempTask(void *pvParameters) {
  Serial.println("tempTask loop started");
  while (1) // tempTask loop
  {
    if (tasksEnabled) {
      // Get temperature values
      getTemperature();
    }
    // Got sleep again
    vTaskSuspend(NULL);
  }
}

/**
 * getTemperature
 * Reads temperature from DHT11 sensor
 * @return bool
 *    true if temperature could be aquired
 *    false if aquisition failed
*/
bool getTemperature() {
  // Reading temperature for humidity takes about 250 milliseconds!
  // Sensor readings may also be up to 2 seconds 'old' (it's a very slow sensor)
  TempAndHumidity newValues = dht.getTempAndHumidity();
  // Check if any reads failed and exit early (to try again).
  if (dht.getStatus() != 0) {
    Serial.println("DHT11 error status: " + String(dht.getStatusString()));
    return false;
  }
  client.publish("iat/test/temp", String((int)(newValues.temperature*10))); // You can activate the retain flag by setting the third parameter to true

  return true;
}



void setup()
{
  Serial.begin(115200);

  // Optional functionnalities of EspMQTTClient : 
  client.enableDebuggingMessages(); // Enable debugging messages sent to serial output

  pinMode(SIREN, OUTPUT);
  pinMode(RED, OUTPUT);
  pinMode(GREEN, OUTPUT);
  pinMode(BLUE, OUTPUT);

  pinMode(X, INPUT);
  pinMode(Y, INPUT);

  analogReadResolution(10);

  initTemp();
  // Signal end of setup() to tasks
  tasksEnabled = true;
  
}

int state[]={LOW,LOW,LOW,LOW};
// This function is called once everything is connected (Wifi and MQTT)
void onConnectionEstablished()
{
  
  client.subscribe("iat/test/xTLsiren", [](const String & topic, const String & payload) {
    state[0]=(payload=="true")?HIGH:LOW;
  });
  client.subscribe("iat/test/xTLred", [](const String & topic, const String & payload) {
    state[1]=(payload=="true")?HIGH:LOW;
  });
  client.subscribe("iat/test/xTLyellow", [](const String & topic, const String & payload) {
    state[2]=(payload=="true")?HIGH:LOW;
  });
  client.subscribe("iat/test/xTLgreen", [](const String & topic, const String & payload) {
    state[3]=(payload=="true")?HIGH:LOW;
  });
}

unsigned long last_time=0;
void loop()
{
  client.loop();
  client.setMqttReconnectionAttemptDelay(2);
  digitalWrite(SIREN,state[0]);
  digitalWrite(RED,state[1]|state[2]);
  digitalWrite(GREEN,state[2]|state[3]);
  digitalWrite(BLUE,state[0]);
  if (!tasksEnabled) {
    // Wait 2 seconds to let system settle down
    delay(2000);
    // Enable task that will read values from the DHT sensor
    tasksEnabled = true;
    if (tempTaskHandle != NULL) {
      vTaskResume(tempTaskHandle);
    }
  }
  if(millis()-last_time>100){
    client.publish("iat/test/X", String(analogRead(X)),0);
    client.publish("iat/test/Y", String(analogRead(Y)),0);
    last_time=millis();
  }
}
