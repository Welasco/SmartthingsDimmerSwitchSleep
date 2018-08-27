/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Dimmer Switch Sleep", namespace: "DimmerSwitchSleep", author: "victor@hepoca.com", ocfDeviceType: "oic.d.light", runLocally: false, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {
		capability "Switch Level"
		capability "Actuator"
		capability "Indicator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"
		capability "Light"

		fingerprint mfr:"0063", prod:"4457", deviceJoinName: "GE In-Wall Smart Dimmer"
		fingerprint mfr:"0063", prod:"4944", deviceJoinName: "GE In-Wall Smart Dimmer"
		fingerprint mfr:"0063", prod:"5044", deviceJoinName: "GE Plug-In Smart Dimmer"

		command "sleep1"
		command "sleep15"
		command "sleep30"
		command "sleep60"
	}

	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		status "09%": "command: 2003, payload: 09"
		status "10%": "command: 2003, payload: 0A"
		status "33%": "command: 2003, payload: 21"
		status "66%": "command: 2003, payload: 42"
		status "99%": "command: 2003, payload: 63"

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
		reply "200119,delay 5000,2602": "command: 2603, payload: 19"
		reply "200132,delay 5000,2602": "command: 2603, payload: 32"
		reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"
	}

	preferences {
		input "ledIndicator", "enum", title: "LED Indicator", description: "Turn LED indicator... ", required: false, options:["on": "When On", "off": "When Off", "never": "Never"], defaultValue: "off"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}

		standardTile("indicator", "device.indicatorStatus", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "when off", action:"indicator.indicatorWhenOn", icon:"st.indicators.lit-when-off"
			state "when on", action:"indicator.indicatorNever", icon:"st.indicators.lit-when-on"
			state "never", action:"indicator.indicatorWhenOff", icon:"st.indicators.never-lit"
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
		}

        standardTile("sleep1", "device.sleep1", width:2, height: 2, canChangeIcon: false, canChangeBackground: false) {
                state "sleep1off", label:'1 Min', action:'sleep1', icon:"st.secondary.off", backgroundColor: "#ffffff"
                state "sleep1on", label:'1 Min', action:'sleepoff', icon:"st.Office.office6", backgroundColor: "#00a0dc"
        }      

        standardTile("sleep15", "device.sleep15", width:2, height: 2, canChangeIcon: false, canChangeBackground: false) {
                state "sleep15off", label:'15 Min', action:'sleep15', icon:"st.secondary.off", backgroundColor: "#ffffff"
                state "sleep15on", label:'15 Min', action:'sleepoff', icon:"st.Office.office6", backgroundColor: "#00a0dc"
        }   

        standardTile("sleep30", "device.sleep30", width:2, height: 2, canChangeIcon: false, canChangeBackground: false) {
                state "sleep30off", label:'30 Min', action:'sleep30', icon:"st.secondary.off", backgroundColor: "#ffffff"
                state "sleep30on", label:'30 Min', action:'sleepoff', icon:"st.Office.office6", backgroundColor: "#00a0dc"
        }   

        standardTile("sleep60", "device.sleep60", width:2, height: 2, canChangeIcon: false, canChangeBackground: false) {
                state "sleep60off", label:'60 Min', action:'sleep60', icon:"st.secondary.off", backgroundColor: "#ffffff"
                state "sleep60on", label:'60 Min', action:'sleepoff', icon:"st.Office.office6", backgroundColor: "#00a0dc"
        }   						  

		main(["switch"])
		details(["switch", "level", "refresh", "sleep1", "sleep15", "sleep30", "sleep60"])

	}
}

def installed() {
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	response(refresh())
}

def updated(){
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
  switch (ledIndicator) {
        case "on":
            indicatorWhenOn()
            break
        case "off":
            indicatorWhenOff()
            break
        case "never":
            indicatorNever()
            break
        default:
            indicatorWhenOn()
            break
    }
}

def getCommandClassVersions() {
	[
		0x20: 1,  // Basic
		0x26: 1,  // SwitchMultilevel
		0x56: 1,  // Crc16Encap
		0x70: 1,  // Configuration
	]
}

def parse(String description) {
	log.debug "parse method executed started!"
	def result = null
	if (description != "updated") {
		log.debug "parse() >> zwave.parse($description)"
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "BasicReport method executed started!"
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	log.debug "BasicSet method executed started!"
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	log.debug "SwitchMultilevelReport method executed started!"
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	log.debug "SwitchMultilevelSet method executed started!"
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	log.debug "dimmerEvents method executed started!"
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value == 99 ? 100 : cmd.value)
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug "ConfigurationReport method executed started!"
	log.debug "ConfigurationReport $cmd"
	def value = "when off"
	if (cmd.configurationValue[0] == 1) {value = "when on"}
	if (cmd.configurationValue[0] == 2) {value = "never"}
	createEvent([name: "indicatorStatus", value: value])
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	log.debug "Hail method executed started!"
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "ManufacturerSpecificReport method executed started!"
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
	log.debug "SwitchMultilevelStopLevelChange method executed started!"
	[createEvent(name:"switch", value:"on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	log.debug "Crc16Encap method executed started!"
	def versions = commandClassVersions
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "All other commands method executed started!"
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on() {
	log.debug "ON method executed started!"
	delayBetween([
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def off() {
	log.debug "OFF method executed started!"
	delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def setLevel(value) {
	log.debug "setLevel method executed started!"
	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	if (level > 0) {
		sendEvent(name: "switch", value: "on")
	} else {
		sendEvent(name: "switch", value: "off")
	}
	delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value, duration) {
	log.debug "setLevel duration method executed started!"
	log.debug "setLevel >> value: $value, duration: $duration"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	def getStatusDelay = duration < 128 ? (duration*1000)+2000 : (Math.round(duration / 60)*60*1000)+2000
	delayBetween ([zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format(),
				   zwave.switchMultilevelV1.switchMultilevelGet().format()], getStatusDelay)
}

/**
 * Sleep Method
 * */

def sleep1() {

	def min = 1
	log.debug "Sleep1 method executed started!"
	sendEvent(name: "switch", value: "on")
    sendEvent(name: "sleep1", value: "sleep1on")
	sendEvent(name: "sleep15", value: "sleep15off")
	sendEvent(name: "sleep30", value: "sleep30off")
	sendEvent(name: "sleep60", value: "sleep60off")

    runIn(60 * min, sleepoff)
    delayBetween([
            on(),
            off()
    ], min*60000) //in ms:   60000 is 1 minute, 5*60000 is 5 mins	
}

def sleep15() {
	def min = 15
	log.debug "Sleep15 method executed started!"
	sendEvent(name: "switch", value: "on")
    sendEvent(name: "sleep1", value: "sleep1off")
	sendEvent(name: "sleep15", value: "sleep15on")
	sendEvent(name: "sleep30", value: "sleep30off")
	sendEvent(name: "sleep60", value: "sleep60off")

    runIn(60 * min, sleepoff)
    delayBetween([
            on(),
            off()
    ], min*60000) //in ms:   60000 is 1 minute, 5*60000 is 5 mins	
}

def sleep30() {
	def min = 30
	log.debug "Sleep30 method executed started!"
	sendEvent(name: "switch", value: "on")
    sendEvent(name: "sleep1", value: "sleep1off")
	sendEvent(name: "sleep15", value: "sleep15off")
	sendEvent(name: "sleep30", value: "sleep30on")
	sendEvent(name: "sleep60", value: "sleep60off")

    runIn(60 * min, sleepoff)
    delayBetween([
            on(),
            off()
    ], min*60000) //in ms:   60000 is 1 minute, 5*60000 is 5 mins	
}

def sleep60() {
	def min = 60
	log.debug "Sleep60 method executed started!"
	sendEvent(name: "switch", value: "on")
    sendEvent(name: "sleep1", value: "sleep1off")
	sendEvent(name: "sleep15", value: "sleep15off")
	sendEvent(name: "sleep30", value: "sleep30off")
	sendEvent(name: "sleep60", value: "sleep60on")

    runIn(60 * min, sleepoff)
    delayBetween([
            on(),
            off()
    ], min*60000) //in ms:   60000 is 1 minute, 5*60000 is 5 mins	
}

def sleepoff(){
	log.debug "Sleepoff method executed started!"
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "sleep1", value: "sleep1off")
	sendEvent(name: "sleep15", value: "sleep15off")
	sendEvent(name: "sleep30", value: "sleep30off")
	sendEvent(name: "sleep60", value: "sleep60off")	

    off()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	log.debug "ping method executed started!"
	refresh()
}

def refresh() {
	log.debug "refresh method executed started!"
	log.debug "refresh() is called"
	def commands = []
	commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
	if (getDataValue("MSR") == null) {
		commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	delayBetween(commands,100)
}

void indicatorWhenOn() {
	log.debug "indicatorWhenOn method executed started!"
	sendEvent(name: "indicatorStatus", value: "when on", displayed: false)
	sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()))
}

void indicatorWhenOff() {
	log.debug "indicatorWhenOff method executed started!"
	sendEvent(name: "indicatorStatus", value: "when off", displayed: false)
	sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 3, size: 1).format()))
}

void indicatorNever() {
	log.debug "indicatorNever method executed started!"
	sendEvent(name: "indicatorStatus", value: "never", displayed: false)
	sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 3, size: 1).format()))
}

def invertSwitch(invert=true) {
	log.debug "invertSwitch method executed started!"
	if (invert) {
		zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format()
	}
	else {
		zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1).format()
	}
}
