# SYSTEM DESCRIPTION:

Very far from home, SpaceY had the brillant idea to "accidentally" ship one of its employees to Mars in order to do some maintenance to the automation platform in charge of monitoring and controlling the environment.
The unfortunate operator, after doing its computer science wizardry, it's now able to use the platform to check environmental metrics and decide when and how actuate changes in the habitat.

# USER STORIES:

01) As an Operator, I want the system to periodically poll the sensors to collect environment metrics from the devices that don't push data automatically
02) As an Operator, I want the system to subscribe to the telemetry streams so that I can receive high-frequency feedbacks
03) As an Operator, I want the ingestion service to handle network disconnections by automatically attempt to reconnect to the devices
04) As an Operator, I want heterogeneous data from various sensors to be normalized into a single standard schema
05) As an Operator, I want normalized events to be ingested by a message broker
06) As an Operator, I want the system to cache the latest known state of each sensor
07) As an Operator, I want to define a new rule by comparing metrics to treshold values and select an actuator to be triggered
08) As an Operator, I want the rules I create to be persisted in a database
09) As an Operator, I want the automation system to dynamically evaluate rules as soon as a new event occurs
10) As an Operator, I want the automation rules engine to automatically trigger an actuator state update when a rule's condition is met
11) As an operator, I want to view the list of existing rules
12) As an operator, I want to delete a rule among the existing ones
13) As an Operator, I want the interface to use different visualizations based on the data type
14) As an Operator, I want to see the current state the actuators on the dashboard
15) As an Operator, I want to manually trigger an actuator even if no rule condition is met
16) As an Operator, I want an interface to compose rules by selecting the sensor, operator, threshold value and actuator
17) As an Operator, I want to view a sensor's data as a chart that progressively updates while the page is open
18) As an Operator, I want the dashboard to receive sensor data in real-time without reloading the page

