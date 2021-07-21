# iAT_plc4x_project

This Project is part of a demonstration and evaluation project for the PLC4X framework.

## Building the project

This project can be built using maven:

```bash
$ mvn package
```

The portable executable .jar file will be generated at `target/iAT_plc4x_project-1.0.0-jar-with-dependencies.jar`

## Notes on Licensing
This Project links to Apache PLC4X, which is licensed under the Apache 2.0 license <cite>[[1]][1]</cite>.
Further, this project links to the Eclipse Paho Java MQTT client library. This library is dual-licensed under both the <cite>[Eclipse Public License 2.0 (EPL2.0)][5] and the <cite>[Eclipse Distribution License v1.0][4]</cite>. <cite>[[3]][3]</cite>

This project in turn is licensed under the GPLv3 license. This is permittable, as both the FSF and Apache Foundation agree that compatibility between GPLv3 and Apache 2.0 is possible, if it's the GPLv3-licensed code that links to the Apache 2.0-licensed code. <cite>[[2]][2]</cite> The same thing applies to Javatuples, which is also used here.<br />

While the EPL2.0 and GPL licenses are considered incompatible <cite>[[6]][6]</cite> we can still incorporate the Eclipse Paho Java MQTT client library as we can select license terms due to them being dual-licensed. <cite>[[7]][7]</cite> The Eclipse Distribution License v1.0 is a 3-clause BSD license, which in turn is considered compatible with the GPL. <cite>[[8]][8]</cite>

[1]: https://github.com/apache/plc4x/blob/develop/LICENSE
[2]: https://www.apache.org/licenses/GPL-compatibility.html

[3]: https://github.com/eclipse/paho.mqtt.java/blob/master/LICENSE
[4]: https://www.eclipse.org/org/documents/edl-v10.php
[5]: https://www.eclipse.org/legal/epl-2.0/
[6]: https://www.eclipse.org/legal/eplfaq.php#GPLCOMPATIBLE
[7]: https://www.eclipse.org/legal/eplfaq.php#DUALLIC
[8]: https://www.gnu.org/licenses/license-list.en.html#ModifiedBSD