/**
 *  Hue Dimmer Switch
 *
 *  Copyright 2016 Stephen McLaughlin
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
    definition (name: "Hue Dimmer Switch ZHA", namespace: "digitalgecko", author: "Stephen McLaughlin") {
        capability "Configuration"
        capability "Battery"
        capability "Refresh"
        capability "Button"
        capability "Sensor"

        fingerprint profileId: "0104", endpointId: "02", application:"02", outClusters: "0019", inClusters: "0000,0001,0003,000F,FC00", manufacturer: "Philips", model: "RWL020", deviceJoinName: "Hue Dimmer Switch (ZHA)"
        fingerprint profileId: "0104", endpointId: "02", application:"02", outClusters: "0019", inClusters: "0000,0001,0003,000F,FC00", manufacturer: "Philips", model: "RWL021", deviceJoinName: "Hue Dimmer Switch"

        attribute "lastAction", "string"
        attribute "useButtonNames", "boolean"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles(scale: 2) {

        multiAttributeTile(name:"lastAction", type: "generic", width: 6, height: 4){
            tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
                attributeState "battery", label: '${currentValue}%',icon: "https://raw.githubusercontent.com/paulsheldon/SmartThings-PS/develop/resources/devicetype-icons/battery050.png"
            }
            tileAttribute ("device.lastAction", key: "PRIMARY_CONTROL") {
                attributeState "active", label:'${currentValue}', icon:"st.Home.home30"
            }
        }

        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 5, height: 1) {
                    state("battery", label:'${currentValue}% battery', unit:"")
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
                state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main "lastAction"
        details(["lastAction","battery","refresh","configure"])
    }

    preferences {
        input description: "The settings below allow the button to be returned as 1,2,3,4 as required for Smart Apps or 'on','up','down','off' as per original device handler.", type: "paragraph", element: "paragraph", title: "BUTTON NAMES"
        input "buttonNames", "bool", title:"Button Names - Use 1,2,3,4", description:"Use Button Numbers for Smart Apps"
    }
}

// parse events into attributes
def parse(String description) {
    def msg = zigbee.parse(description)

    //log.warn msg
    /// Actual code down here
    List map = []
    if (description?.startsWith('catchall:')) {
        map = parseCatchAllMessage(description)
    }

    def result = map ? map : null

    if (description?.startsWith('enroll request')) {
        List cmds = enrollResponse()
        result = cmds?.collect { new physicalgraph.device.HubAction(it) }
    }
    else if (description?.startsWith('read attr -')) {
        result = parseReportAttributeMessage(description).each { createEvent(it) }
    }
    return result
}

private List parseReportAttributeMessage(String description) {
    Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
        def nameAndValue = param.split(":")
        map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
    }

    List result = []

    // Battery
    if (descMap.cluster == "0001" && descMap.attrId == "0020") {
        // log.warn descMap
        result << getBatteryResult(Integer.parseInt(descMap.value, 16))
    }

    return result
}

private boolean shouldProcessMessage(cluster) {
    // 0x0B is default response indicating message got through
    boolean ignoredMessage = cluster.profileId != 0x0104 ||
    cluster.command == 0x0B ||
    (cluster.data.size() > 0 && cluster.data.first() == 0x3e)
    return !ignoredMessage
}

/*
getBatteryResult
 */
//TODO: needs calibration
private Map getBatteryResult(rawValue) {
    //log.debug "Battery rawValue = ${rawValue}"

    def result = [
    name: 'battery',
    value: '--',
    translatable: true
    ]

    def volts = rawValue / 10

    if (rawValue == 0 || rawValue == 255) {}
    else {
        if (volts > 3.5) {
            result.descriptionText = "{{ device.displayName }} battery has too much power: (> 3.5) volts."
        }
        else {
            if (device.getDataValue("manufacturer") == "SmartThings") {
                volts = rawValue // For the batteryMap to work the key needs to be an int
                def batteryMap = [28:100, 27:100, 26:100, 25:90, 24:90, 23:70,
                22:70, 21:50, 20:50, 19:30, 18:30, 17:15, 16:1, 15:0]
                def minVolts = 15
                def maxVolts = 28

                if (volts < minVolts)
                    volts = minVolts
                else if (volts > maxVolts)
                    volts = maxVolts
                    def pct = batteryMap[volts]
                    if (pct != null) {
                        result.value = pct
                        result.descriptionText = "{{ device.displayName }} battery was {{ value }}%"
                    }
            }
            else {
                def minVolts = 2.1
                def maxVolts = 3.0
                def pct = (volts - minVolts) / (maxVolts - minVolts)
                def roundedPct = Math.round(pct * 100)
                if (roundedPct <= 0)
                    roundedPct = 1
                    result.value = Math.min(100, roundedPct)
                    result.descriptionText = "{{ device.displayName }} battery was {{ value }}%"
            }
        }
    }

    return result
}

private List getButtonResult(rawValue) {
    def result
    def button = rawValue[0]
    def buttonState = rawValue[4]
    def buttonHoldTime = rawValue[6]
    def hueStatus = (button as String) + "00" + (buttonState as String) // This is the state in the HUE api
    log.info "Button: " + button + "  Hue Code: " + hueStatus + "  Hold Time: " + buttonHoldTime + "  Button State: " + buttonState
    //   result.data = ['buttonNumber': button]

    def buttonValue=["on","up","down", "off"]
    sendEvent(name: "buttonNames", value: buttonNames)
    def buttonName= (buttonNames)?   "$button" : buttonValue[button-1]
    def buttonTitle= buttonValue[button-1]
    log.info "Button Names: $buttonNames : $buttonName"

    // The button is pressed, aka: pushed + released, with 0 hold time
    if ( buttonState == 0 ) {
        result = [createEvent(name: "button", value: "pressed" , data: [buttonNumber: buttonName], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)]
        sendEvent(name: "lastAction", value: buttonTitle + " pressed")
    }
    // The button is pressed, aka: pushed + released, with at least 1s hold time
    else if ( buttonState == 2 ) {
        result = [
        createEvent(name: "button", value: "pushed" , data: [buttonNumber: buttonName], descriptionText: "$device.displayName button $button was pushed", isStateChange: true),
        createEvent(name: "button", value: "released" , data: [buttonNumber: buttonName], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
        ]
        sendEvent(name: "lastAction", value: buttonTitle + " pushed")
        sendEvent(name: "lastAction", value: buttonTitle + " released")
    }
    // The button is released, with at least 1s hold time. This code happens after the button is held
    else if ( buttonState == 3 ) {
        result = [
        createEvent(name: "button", value: "released", data: [buttonNumber: buttonName], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
        ]
        sendEvent(name: "lastAction", value: buttonTitle + " released")
    }
    // The button is held
    else if ( buttonHoldTime == 8 ) {
        result = [createEvent(name: "button", value: "held", data: [buttonNumber: buttonName], descriptionText: "$device.displayName button $button was held", isStateChange: true)]
        sendEvent(name: "lastAction", value: buttonTitle + " held")
    }
    else {
        return
    }
    return result

}

/*
parseCatchAllMessage
 */
private List parseCatchAllMessage(String description) {
    List resultMap = []
    def cluster = zigbee.parse(description)
    if (shouldProcessMessage(cluster)) {
        switch(cluster.clusterId) {
            case 0x0001:
            // 0x07 - configure reporting
            if (cluster.command != 0x07) {
                resultMap = [getBatteryResult(cluster.data.last())]
            }
            break

            case 0xFC00:
            if ( cluster.command == 0x00 ) {
                resultMap = getButtonResult( cluster.data );
            }
            break

        }
    }

    return resultMap
}


def refresh() {
    //log.debug "Refresh"

    def refreshCmds = []

    refreshCmds += "st rattr 0x${device.deviceNetworkId} 0x02 0x0001 0x0020"; // WORKS! - Fetches battery from 0x02


    //   configCmds += zigbee.configureReporting(0x406,0x0000, 0x18, 30, 600, null) // motion // confirmed

    //refreshCmds += zigbee.configureReporting(0x000F, 0x0055, 0x10, 30, 30, null);
    //	refreshCmds += "zdo bind 0xDAD6 0x01 0x02 0x000F {00178801103317AA} {}"
    //    refreshCmds += "delay 2000"
    //    refreshCmds += "st cr 0xDAD6 0x02 0x000F 0x0055 0x10 0x001E 0x001E {}"
    //    refreshCmds += "delay 2000"

    //refreshCmds += zigbee.configureReporting(0x000F, 0x006F, 0x18, 0x30, 0x30);
    //	refreshCmds += "zdo bind 0x${device.deviceNetworkId} 0x02 0x02 0xFC00 {${device.zigbeeId}} {}"
    //   refreshCmds += "delay 2000"
    //  refreshCmds += "st cr 0x${device.deviceNetworkId} 0x02 0xFC00 0x0000 0x18 0x001E 0x001E {}"
    //  refreshCmds += "delay 2000"
    //log.debug refreshCmds

    return refreshCmds
}

def configure() {
    //	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
    //log.debug "Configuring Reporting and Bindings."
    def configCmds = []

    // Configure Button Count
    sendEvent(name: "numberOfButtons", value: 4, displayed: false)

    // Monitor Buttons
    //TODO: This could be zigbee.configureReporting(0xFC00, 0x0000, 0x18, 0x001e, 0x001e); but no idea how to point it at a different endpoint
    configCmds += "zdo bind 0x${device.deviceNetworkId} 0x02 0x02 0xFC00 {${device.zigbeeId}} {}"
    configCmds += "delay 2000"
    configCmds += "st cr 0x${device.deviceNetworkId} 0x02 0xFC00 0x0000 0x18 0x001E 0x001E {}"
    configCmds += "delay 2000"

    // Monitor Battery
    //TODO: This could be zigbee.batteryConfig(); but no idea how to point it at a different endpoint
    configCmds += "zdo bind 0x${device.deviceNetworkId} 0x02 0x02 0x0001 {${device.zigbeeId}} {}"
    configCmds += "delay 2000"
    configCmds += "st cr 0x${device.deviceNetworkId} 0x02 0x0001 0x0020 0x20 0x001E 0x0258 {}"
    //    configCmds += "st cr 0x${device.deviceNetworkId} 0x02 0x0001 0x0020 0x20 0x001E 0x001e {}"

    configCmds += "delay 2000"
    return configCmds + refresh()
}

def configureHealthCheck() {
    Integer hcIntervalMinutes = 12
    refresh()
    sendEvent(name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def updated() {
	configureHealthCheck()
}