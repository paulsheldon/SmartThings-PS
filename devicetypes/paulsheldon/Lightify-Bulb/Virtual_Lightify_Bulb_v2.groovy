/**
 *  Virtual Device for Osram Lightify Bulb (Color Temp Adjustable)
 *
 *  Copyright 2014 SmartThings
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
 *  2016-01-26 - Updated virtual device to use official Color Temperature Capability instead of custom commands
 * 
 */
 
metadata {
	definition (name: "Virtual Color Temp v2", namespace: "Sticks18", author: "Scott Gibson") {

		capability "Actuator"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Color Temperature"
        
        	attribute "bulbTemp", "string"
        
	}


	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#79b821"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}
        controlTile("colorSliderControl", "device.colorTemperature", "slider", height: 1, width: 2, inactiveLabel: false, range: "(2700..6500)") {
			state "colorTemperature", action:"color temperature.setColorTemperature"
		}
		valueTile("kelvin", "device.colorTemperature") {
			state "colorTemperature", label:'${currentValue}k',
				backgroundColors:[
					[value: 2900, color: "#FFA757"],
					[value: 3300, color: "#FFB371"],
					[value: 3700, color: "#FFC392"],
					[value: 4100, color: "#FFCEA6"],
					[value: 4500, color: "#FFD7B7"],
					[value: 4900, color: "#FFE0C7"],
					[value: 5300, color: "#FFE8D5"],
                    [value: 6600, color: "#FFEFE1"]
				]
		}
        valueTile("bulbTemp", "device.bulbTemp", inactiveLabel: false, decoration: "flat") {
			state "bulbTemp", label: '${currentValue}'
		}
        valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
			state "level", label: 'Level ${currentValue}%'
		}
		

		main(["switch"])
		details(["switch", "bulbTemp", "refresh", "levelSliderControl", "level", "colorSliderControl", "kelvin"])
	}
}

// Parse incoming device messages to generate events

def parse(String description) {}

def on() {
	log.debug "on()"
	sendEvent(name: "switch", value: "on")
}

def off() {
	log.debug "off()"
	sendEvent(name: "switch", value: "off") 
}

def refresh() {
    log.debug "refresh"
}

def setLevel(value) {
	log.trace "setLevel($value)"
    
    if (val == 0){ // I liked that 0 = off
    	sendEvent(name: "level", value: value)
    	sendEvent(name: "switch", value: "off")
    }
    else
    {
    	sendEvent(name: "switch", value: "on")
        sendEvent(name: "level", value: value)
    }
}

def setColorTemperature(value) {
	
    log.trace "setColorTemperature($value)"

    def degrees = Math.max(2700, value)
    degrees = Math.min(6500, value)
	
    def bTemp = getBulbTemp(value)
    
    log.trace degrees
	
    sendEvent(name: "colorTemperature", value: degrees)
    sendEvent( name: "bulbTemp", value: bTemp)
    
}


private getBulbTemp(value) {
	
    def s = "Soft White"
    
	if (value < 2900) {
    	return s
    } 
    else if (value < 3350) {
    	s = "Warm White"
        return s
    }
    else if (value < 3900) {
    	s = "Cool White"
        return s
    }
    else if (value < 4800) {
    	s = "Bright White"
        return s
    }
    else if (value < 5800) {
    	s = "Natural"
        return s
    }
    else {
    	s = "Daylight"
        return s
    }

}

