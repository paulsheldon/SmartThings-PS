/*	DO NOT PUBLISH !!!!
*
*  Child Creator - Advanced Button Controller
*
*  Author: Paul Sheldon (Original by Stephan Hackett)
*
*
* 6/20/17 - fixed missing subs for notifications
* 1/14/18 - updated Version check code
* 1/15/18 - added icon support for Inovelli Switches (NZW30S and NZW31S)
* 		  - small adjustments to "Configure Button" page layout
* 1/28/18 - Added Icons and details for Remote ZRC-90US Button Controller.
* 2/08/18 - reformatted Button Config Preview
* 2/12/18 - re-did getDescription() to only display Pushed/Held preview if it exists
*			restructured detailsMap and button config build for easy editing
*			made subValue inputs "hidden" and "required" when appropriate
*
*  == Code now maintained by Paul Sheldon ==
* 2019-02-05    Added images and code for Hue Dimmer Switches & Colour Temp options
* 2019-09-25 Updated volume control, play/pause, next/previous track and mute/unmute for Sonos speakers, code provided by Gabor Szabados
* 2020-01-02 Added support (beta) for fan control, Inovelli Red Series Switch & Dimmer (inc config button 7)
* 2020-05-05 Added support WS200 Dimmer & Switch, Ikea Buttons provided by hyvamiesh, Dimming lights does not switch light off provided by hyvamiesh
* 2020-05-12 Added option to see button function in device list
*
*	DO NOT PUBLISH !!!!
*/

/**********************************************************************
 *  Define App
 **********************************************************************/

def version() { "v1.210602" }

definition(
        name: "ABC Child Creator",
        namespace: "paulsheldon",
        author: "Paul Sheldon (Original by Stephan Hackett)",
        description: "DO NOT PUBLISH",
        category: "My Apps",
        parent: "paulsheldon:ABC Manager",
        iconUrl: "https://raw.githubusercontent.com/paulsheldon/SmartThings-PS/master/resources/abc/images/abcNew.png",
        iconX2Url: "https://raw.githubusercontent.com/paulsheldon/SmartThings-PS/master/resources/abc/images/abcNew.png",
        iconX3Url: "https://raw.githubusercontent.com/paulsheldon/SmartThings-PS/master/resources/abc/images/abcNew.png",
)

preferences {
    page(name: "pageChooseButton",)
    page(name: "pageConfigButtons")
    page(name: "pageTimeInterval", title: "Only during a certain time") {
        section {
            input "starting", "time", title: "Starting", required: false
            input "ending", "time", title: "Ending", required: false
        }
    }
}

/**********************************************************************
 *  Setup and Configuration Commands:
 **********************************************************************/

def installed() {
    log.debug "${app.label}: Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "${app.label}: Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    log.debug "${app.label}: Initialize"
    app.label == app.name ? app.updateLabel(defaultLabel()) : app.updateLabel(app.label)
    subscribe(buttonDevice, "button", buttonEvent)
    state.lastshadesUp = true
}

/*
*Page Definitions
*/

def pageChooseButton() {
    dynamicPage(name: "pageChooseButton", install: true, uninstall: true) {
        section("Step 1: Select Button Device") {
            icon(title: "Choose an Icon", required=false)
            input "buttonDevice", "capability.button", title: "Button Device", multiple: false, required: true, submitOnChange: true
        }
        if (buttonDevice) {
            state.buttonType = getButtonType(buttonDevice.typeName)
            log.debug "Device Type is now set to: " + state.buttonType
            state.buttonCount = manualCount ?: buttonDevice.currentValue('numberOfButtons')

            log.debug "Device has " + state.buttonCount + " Buttons"
            section("Step 2: Configure Buttons for Selected Device") {
                if (state.buttonCount < 1) {
                    paragraph "The selected button device did not report the number of buttons it has. Please specify in the Advanced Config section below."
                } else {
                    log.debug("Show Hardware Specs: ${showHWSpecs == true ? 'Yes' : 'No'}")
                    for (i in 1..state.buttonCount) {
                        href "configButtonsPage", title: "Button ${i}" + ((showHWSpecs == true && getSpecText(i) != null) ? "\n ${getSpecText(i)}" : ""), state: getDescription(i) != "Tap to configure" ? "complete" : null, description: getDescription(i), params: [pbutton: i]
                    }
                }
            }
        }
        section("Set Custom Name (Optional)") {
            label title: "Assign a name:", required: false
        }
        section("Advanced Config:", hideable: true, hidden: hideOptionsSection()) {
            input "manualCount", "number", title: "Set/Override # of Buttons?", required: false, description: "Only set if DTH does not report", submitOnChange: true
            input "collapseAll", "bool", title: "Collapse Unconfigured Sections?", defaultValue: true
            input "showButtonImage", "bool", title: "Show Image on Button Setup?", defaultValue: true
            input "showHWSpecs", "bool", title: "Show H/W Specific Details?", defaultValue: true
            input "fanIgnoreOff", "bool", title: "Ignore Fan Off?", defaultValue: false
            input "sonos", "bool", title: "Using a Sonos?", defaultValue: false
        }
        section(title: "Only Execute When:", hideable: true, hidden: hideOptionsSection()) {
            def timeLabel = timeIntervalLabel()
            href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null
            input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
                    options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            input "modes", "mode", title: "Only when mode is", multiple: true, required: false
        }
    }
}





























def pageConfigButtons(params) {
    if (params.pbutton != null) state.currentButton = params.pbutton.toInteger()
    dynamicPage(name: "pageConfigButtons", title: "CONFIGURE BUTTON ${state.currentButton}:\n${state.buttonType}", getButtonSections(state.currentButton))
}

def getButtonSections(buttonNumber) {
    return {
        def picNameNoSpace = "${state.buttonType}${state.currentButton}.png" - " " - " " - " " - "/" - "-"
        log.debug("Button Image Name: $picNameNoSpace")
        log.debug picNameNoSpace
        log.debug("Show Button Image: ${showButtonImage == true ? 'Yes' : 'No'}")
        if (showButtonImage == true) {
            section() { //"Hardware specific info on button selection:") {
                paragraph image: "https://raw.githubusercontent.com/paulsheldon/SmartThings-PS/master/resources/abc/images/${picNameNoSpace}", "${getSpecText()}"
            }
        }
        def myDetail
        for (i in 1..23) {//Build 1st 23 Button Config Options
            myDetail = getPrefDetails().find { it.sOrder == i }
            section(hideable: true, hidden: !(shallHide("${myDetail.id}${buttonNumber}") || shallHide("${myDetail.sub}${buttonNumber}")), myDetail.secLabel) {
                input "${myDetail.id}${buttonNumber}_pushed", myDetail.cap, title: "When Pushed", multiple: true, required: false, submitOnChange: collapseAll
                if (myDetail.sub && isReq("${myDetail.id}${buttonNumber}_pushed")) input "${myDetail.sub}${buttonNumber}_pushed", "number", title: myDetail.sTitle, multiple: false, required: isReq("${myDetail.id}${buttonNumber}_pushed"), description: myDetail.sDesc
                if (showHeld()) input "${myDetail.id}${buttonNumber}_held", myDetail.cap, title: "When Held", multiple: true, required: false, submitOnChange: collapseAll
                if (myDetail.sub && isReq("${myDetail.id}${buttonNumber}_held")) input "${myDetail.sub}${buttonNumber}_held", "number", title: myDetail.sTitle, multiple: false, required: isReq("${myDetail.id}${buttonNumber}_held"), description: myDetail.sDesc
            }
            if ([3, 8, 10, 15, 18].contains(i)) section("") {}
        }
        section("Set Mode ", hideable: true, hidden: !shallHide("mode_${buttonNumber}")) {
            input "mode_${buttonNumber}_pushed", "mode", title: "When Pushed", required: false, submitOnChange: collapseAll
            if (showHeld()) input "mode_${buttonNumber}_held", "mode", title: "When Held", required: false, submitOnChange: collapseAll
        }
        def phrases = location.helloHome?.getPhrases()*.label
        if (phrases) {
            section("Run Routine", hideable: true, hidden: !shallHide("phrase_${buttonNumber}")) {
                log.trace phrases
                input "phrase_${buttonNumber}_pushed", "enum", title: "When Pushed", required: false, options: phrases, submitOnChange: collapseAll
                if (showHeld()) input "phrase_${buttonNumber}_held", "enum", title: "When Held", required: false, options: phrases, submitOnChange: collapseAll
            }
            section("Notifications: SMS, In App or Both", hideable: true, hidden: !shallHide("sms_${buttonNumber}")) {
                paragraph "****************\nWHEN PUSHED\n****************"
                input "sms_${buttonNumber}_pushed", "text", title: "Message", description: "Enter message to send", required: false, submitOnChange: collapseAll
                input "phoneNum_${buttonNumber}_pushed", "phone", title: "Send Text To", description: "Enter phone number", required: false, submitOnChange: collapseAll
                input "notify_${buttonNumber}_pushed", "bool", title: "Notify In App?", required: false, defaultValue: false, submitOnChange: collapseAll
                if (showHeld()) {
                    paragraph "*************\nWHEN HELD\n*************"
                    input "message_${buttonNumber}_held", "text", title: "Message", description: "Enter message to send", required: false, submitOnChange: collapseAll
                    input "phoneNum_${buttonNumber}_held", "phone", title: "Send Text To", description: "Enter phone number", required: false, submitOnChange: collapseAll
                    input "notify_${buttonNumber}_held", "bool", title: "Notify In App?", required: false, defaultValue: false, submitOnChange: collapseAll
                }
            }
            if (enableSpec()) {
                section(" ") {}
                section("Special", hideable: true, hidden: !shallHide("container_${buttonNumber}")) {
                    input "container_${buttonNumber}_pushed", "device.VirtualContainer", title: "When Pushed", required: false, submitOnChange: collapseAll
                    if (showHeld()) input "container_${buttonNumber}_held", "device.VirtualContainer", title: "When Held", required: false, submitOnChange: collapseAll
                }
            }
        }
    }
}


def enableSpec() {
    return false
}

def showHeld() {
    if (state.buttonType.contains("100+ ") || state.buttonType == "Cube Controller") return false
    else return true
}

def shallHide(myFeature) {
    if (collapseAll) return (settings["${myFeature}_pushed"] || settings["${myFeature}_held"] || settings["${myFeature}"])
    return true
}

def isReq(myFeature) {
    (settings[myFeature]) ? true : false
}

def getDescription(dNumber) {
    def description = ""
    if (!(settings.find { it.key.contains("_${dNumber}_") })) return "Tap to configure"
    if (settings.find {
        it.key.contains("_${dNumber}_pushed")
    }) description = "\nPUSHED:" + getDescDetails(dNumber, "_pushed") + "\n"
    if (settings.find {
        it.key.contains("_${dNumber}_held")
    }) description = description + "\nHELD:" + getDescDetails(dNumber, "_held") + "\n"
//if(anySettings) description = "PUSHED:"+getDescDetails(dNumber,"_pushed")+"\n\nHELD:"+getDescDetails(dNumber,"_held")//"CONFIGURED : Tap to edit"
    return description
}

def getDescDetails(bNum, type) {
    def numType = bNum + type
    log.info "$numType"
    def preferenceNames = settings.findAll { it.key.contains("_${numType}") }.sort()
//get all configured settings that: match button# and type, AND are not false
    if (!preferenceNames) {
        return " **Not Configured** "
    } else {
        def formattedPage = ""
        preferenceNames.each { eachPref ->
            def prefDetail = getPrefDetails().find {
                eachPref.key.contains(it.id)
            } //gets description of action being performed(eg Turn On)
            def prefDevice = " : ${eachPref.value}" - "[" - "]" //name of device the action is being performed on (eg Bedroom Fan)
            try {
                def prefSubValue = settings[prefDetail.sub + numType] ?: "(!Missing!)"
                if (prefDetail.type == "normal") formattedPage += "\n- ${prefDetail.desc}${prefDevice}"
                if (prefDetail.type == "hasSub") formattedPage += "\n- ${prefDetail.desc}${prefSubValue}${prefDevice}"
                if (prefDetail.type == "bool") formattedPage += "\n- ${prefDetail.desc}"
            }
            catch (e) {
                log.error "Sub Value failed"
            }
        }
        return formattedPage
    }
}


def defaultLabel() {
    return "${buttonDevice} Mapping"
}

def getPrefDetails() {
    def capPlayPause = 'capability.musicPlayer'
    def capVolume = 'capability.musicPlayer'
    def capTrack = 'capability.musicPlayer'
    def capMute = 'capability.musicPlayer'
    if (sonos == true) {
        capPlayPause = 'capability.mediaPlayback'
        capVolume = 'capability.audioVolume'
        capTrack = 'capability.mediaTrackControl'
        capMute = 'capability.audioMute'
    }
    def detailMappings =
            [
// Lights
[id: 'lightOn_', sOrder: 1, desc: 'Turn On ', comm: lightOn, type: 'normal', secLabel: 'Switches (Turn On)', cap: 'capability.switch'],
[id: 'lightOff_', sOrder: 2, desc: 'Turn Off', comm: lightOff, type: 'normal', secLabel: 'Switches (Turn Off)', cap: 'capability.switch'],
[id: 'lights_', sOrder: 3, desc: 'Toggle On/Off', comm: lightToggle, type: 'normal', secLabel: 'Switches (Toggle On/Off)', cap: 'capability.switch'],
[id: 'lightDim_', sOrder: 4, desc: 'Dim to ', comm: lightDim, sub: 'valLight', type: 'hasSub', secLabel: 'Dimmers (On to Level - Grp 1)', cap: 'capability.switchLevel', sTitle: 'Bright Level', sDesc: '0 to 100%'],
[id: 'lightD2m_', sOrder: 5, desc: 'Dim to ', comm: lightDim, sub: 'valLight2', type: 'hasSub', secLabel: 'Dimmers (On to Level - Grp 2)', cap: 'capability.switchLevel', sTitle: 'Bright Level', sDesc: '0 to 100%'],
[id: 'dimPlus_', sOrder: 6, desc: 'Brightness +', comm: lightDimUp, sub: 'valDimUp', type: 'hasSub', secLabel: 'Dimmers (Increase Level By)', cap: 'capability.switchLevel', sTitle: 'Increase by', sDesc: '0 to 15'],
[id: 'dimMinus_', sOrder: 7, desc: 'Brightness -', comm: lightDimDown, sub: 'valDimDown', type: 'hasSub', secLabel: 'Dimmers (Decrease Level By)', cap: 'capability.switchLevel', sTitle: 'Decrease by', sDesc: '0 to 15'],
[id: 'lightsDT_', sOrder: 8, desc: 'Turn Off/Dim', comm: lightDimOff, sub: 'valDimOff', type: 'hasSub', secLabel: 'Dimmers (Turn Off/Dim to)', cap: 'capability.switchLevel', sTitle: 'Bright Level', sDesc: '0 to 100%'],

// Colour Temperatures
[id: 'colourTempUp_', sOrder: 9, desc: 'Colour Temp Up ', comm: colourTempUp, sub: 'valColourTempUp', type: 'hasSub', secLabel: 'Light Colour Temp (Inc By)', cap: 'capability.colorTemperature', sTitle: 'Increase by', sDesc: '100 to 1000'],
[id: 'colourTempDown_', sOrder: 10, desc: 'Colour Temp Down ', comm: colourTempDown, sub: 'valColourTempDown', type: 'hasSub', secLabel: 'Light Colour Temp (Dec By)', cap: 'capability.colorTemperature', sTitle: 'Decrease by', sDesc: '100 to 1000'],

// Speakers
[id: 'speakerpp_', sOrder: 11, desc: 'Play/Pause', comm: speakerPlayPause, type: 'normal', secLabel: 'Speakers (Toggle Play-Pause)', cap: capPlayPause],
[id: 'speakervu_', sOrder: 12, desc: 'Volume +', comm: speakerVolumeUp, sub: 'valSpeakerVolumeUp', type: 'hasSub', secLabel: 'Speakers (Increase Vol By)', cap: capVolume, sTitle: 'Increase by', sDesc: '0 to 15'],
[id: 'speakervd_', sOrder: 13, desc: 'Volume -', comm: speakerVolumeDown, sub: 'valSpeakerVolumeDown', type: 'hasSub', secLabel: 'Speakers (Decrease Vol By)', cap: capVolume, sTitle: 'Decrease by', sDesc: '0 to 15'],
[id: 'speakernt_', sOrder: 14, desc: 'Next Track', comm: speakerNextTrack, type: 'normal', secLabel: 'Speakers (Go to Next Track)', cap: capTrack],
[id: 'speakerpt_', sOrder: 15, desc: 'Previous Track', comm: speakerPrevTrack, type: "normal", secLabel: 'Speakers (Go to Prev Track)', cap: capTrack],
[id: 'speakermu_', sOrder: 16, desc: 'Mute/Unmute', comm: speakerMuteUnmute, type: 'normal', secLabel: 'Speakers (Toggle Mute-Unmute)', cap: capMute],
// Sirens
[id: 'sirens_', sOrder: 17, desc: 'SIren Toggle', comm: sirenToggle, type: 'normal', secLabel: 'Sirens (Toggle)', cap: 'capability.alarm'],
// Locks
[id: 'locks_', sOrder: 18, desc: 'Lock', comm: lock, type: 'normal', secLabel: 'Locks (Lock)', cap: 'capability.lock'],
[id: 'locksUnlock_', sOrder: 19, desc: 'Unlock', comm: unlock, type: 'normal', secLabel: 'Locks (Unlock)', cap: 'capability.lock'],
[id: 'locksToggle_', sOrder: 20, desc: 'Toggle Lock', comm: lockToggle, type: 'normal', secLabel: 'Locks (Toggle)', cap: 'capability.lock'],

// Fans
[id: 'fanAdjust_', sOrder: 21, desc: 'Adjust', comm: fanAdjust, type: 'normal', secLabel: 'Fans (Low, Medium, High, Off)', cap: 'capability.switchLevel'],
// Shades
[id: 'shadeAdjust_', sOrder: 22, desc: 'Adjust', comm: shadeAdjust, type: 'normal', secLabel: 'Shades (Up, Down, Stop)', cap: 'capability.doorControl'],
// Switches
[id: 'offOnReset_', sOrder: 23, desc: 'Reset->On', comm: switchOffOnReset, type: 'normal', secLabel: 'Switches (Reset->On)', cap: 'capability.switch'],

// Misc Functions
[id: 'mode_', desc: 'Set Mode', comm: changeMode, type: 'normal'],
[id: 'phrase_', desc: 'Run Routine', comm: runRout, type: 'normal'],
[id: 'notifications_', desc: 'Push Notification', comm: messageHandle, sub: 'valNotify', type: 'bool'],
[id: 'phone_', desc: 'Send SMS to', comm: smsHandle, sub: 'notifications_', type: 'normal'],
[id: 'container_', desc: 'Cycle Playlist', comm: cyclePL, type: 'normal'],
            ]
    return detailMappings
}

/*
Button Functions
*/

def buttonEvent(evt) {
    if (allOk) {
        def buttonNumber = evt.jsonData.buttonNumber
        def pressType = evt.value
        log.debug "$buttonDevice: Button $buttonNumber was $pressType"
        def preferenceNames = settings.findAll { it.key.contains("_${buttonNumber}_${pressType}") }
        preferenceNames.each { eachPref ->
            def prefDetail = getPrefDetails()?.find {
                eachPref.key.contains(it.id)
            } //returns the detail map of id,desc,comm,sub
            try {
                def PrefSubValue = settings["${prefDetail.sub}${buttonNumber}_${pressType}"]
                //value of sub-setting (eg 100)
                if (prefDetail.sub) "$prefDetail.comm"(eachPref.value, PrefSubValue)
                else "$prefDetail.comm"(eachPref.value)
            }
            catch (e) {
                log.trace e
            }
        }
    }
}

/*
Light Functions
*/

def lightOn(devices) {
    log.info "Turning On: $devices"
    devices.on()
}

def lightOff(devices) {
    log.info "Turning Off: $devices"
    devices.off()
}

def lightToggle(devices) {
    log.info "Toggling Lights: $devices"
    if (devices*.currentValue('switch').contains('on')) devices.off()
    else (devices*.currentValue('switch').contains('off')) devices.on()
}

def lightDim(devices, level) {
    log.info "Dimming (to $level): $devices"
    devices.setLevel(level)
}

def lightDimUp(device, incLevel) {
    log.debug "Incrementing Light by +$incLevel: $device"
    def currentLevel = device.currentValue('level')[0]
//currentLevel return a list...[0] is first item in list ie volume level
    def newLevel = currentLevel.toInteger() + incLevel
    if (newLevel > 100) newLevel = 100
    device.setLevel(newLevel)
}

def lightDimDown(device, decLevel) {
    log.info "Decrementing Light by -$decLevel: $device"
    def currentLevel = device.currentValue('level')[0]
    def newLevel = currentLevel.toInteger() - decLevel
    if (newLevel < 1) newLevel = 1 //Disable turning off light by dimming too low.
    device.setLevel(newLevel)
}

def lightDimOff(devices, dimLevel) {
    log.info "Toggling On/Off | Dimming (to $dimLevel): $devices"
    if (devices*.currentValue('switch').contains('on')) devices.off()
    else devices.setLevel(dimLevel)
}

/*
Colour Temp Functions
*/

def colourTempUp(device, value) {
    if (value != null) {
        log.info "$value Incrementing Colour Temp: $device"
        def currentTemp = device.currentValue('colorTemperature')[0]
        def newTemp = currentTemp + value > 6500 ? 6500 : currentTemp + value
        device.setColorTemperature(newTemp)
        sendEvent(name: "colorName", value: colourTempName(newTemp))
        log.info "Colour Temp Changed to $colorTempName"
    }
}

def colourTempDown(device, value) {
    if (value != null) {
        log.info "Decrementing Colour Temp: $device"
        def currentTemp = device.currentValue('colorTemperature')[0]
        def newTemp = currentTemp - value < 2200 ? 2200 : currentTemp - value
        device.setColorTemperature(newTemp)
        sendEvent(name: "colorName", value: colourTempName(newTemp))
        log.info "Colour Temp Changed to $colorTempName"
    }
}

private colourTempName(value) {
    if (value != null) {
        if (value < 2500) return "Warm Glow"
        else if (value < 3000) return "Warm White"
        else if (value < 5000) return "Cool White"
        else if (value < 6000) return "Daylight"
        else return "Cool Daylight"
    }
    return "White"
}

/*
Speaker Functions
*/

def speakerPlayPause(device) {
    log.debug "Toggling Play/Pause: $device"
    if (sonos == true) device.currentValue('playbackStatus').contains('playing') ? device.pause() : device.play()
    else device.currentValue('status').contains('playing') ? device.pause() : device.play()
}

def speakerVolumeUp(device, incLevel) {
    log.debug "Incrementing Volume by +$incLevel: $device"
    def currentVolume = (sonos == true) ? device.currentValue('volume')[0] : device.currentValue('level')[0]
    def newVolume = currentVolume.toInteger() + incLevel
    if (newVolume > 100) newVolume = 100
    if (sonos == true) device.setVolume(newVolume)
    else device.setLevel(newVolume)
    log.debug "Volume increased by $incLevel to $newVolume"
}

def speakerVolumeDown(device, decLevel) {
    log.debug "Decrementing Volume by -$decLevel: $device"
    def currentVolume = (sonos == true) ? device.currentValue('volume')[0] : device.currentValue('level')[0]
    def newVolume = currentVolume.toInteger() - decLevel
    if (newVolume < 0) newVolume = 0
    if (sonos == true) device.setVolume(newVolume)
    else device.setLevel(newVolume)
    log.debug "Volume decreased by $decLevel to $newVolume"
}

def speakerNextTrack(device) {
    log.debug "Next Track Sent to: $device"
    device.nextTrack()
}

def speakerPrevTrack(device) {
    log.debug "Previous Track Sent to: $device"
    device.previousTrack()
}

def speakerMuteUnmute(device) {
    log.debug "Toggling Mute/Unmute: $device"
    device.currentValue('mute').contains('unmuted') ? device.mute() : device.unmute()
}

/*
Siren Functions
*/

def sirenToggle(devices) {
    log.debug "Toggling: $devices"
    if (devices*.currentValue('switch').contains('on')) devices.off()
    else if (devices*.currentValue('switch').contains('off')) devices.on()
}

/*
Lock Functions
*/

def lock(devices) {
    log.debug "Locking: $devices"
    devices.lock()
}

def unlock(devices) {
    log.debug "Unlocking: $devices"
    devices.unlock()
}

def lockToggle(devices) {
    log.debug "Toggling: $devices"
    if (devices*.currentValue("lock").contains('locked')) devices.unlock()
    else devices.lock()
}

/*
Fan Functions
*/

def fanAdjust(device) {
    log.debug "Adjusting: $device"
    def currentLevel = device.currentLevel
    if (device.currentSwitch == 'off') device.setLevel(15)
    else if (currentLevel < 34) device.setLevel(50)
    else if (currentLevel < 67) device.setLevel(90)
    else if (fanIgnoreOff) device.off()
    else device.SetLevel(15)
}

/*
Shade Functions
*/

def shadeAdjust(device) {
    log.debug "Shades: $device = ${device.currentMotor} state.lastUP = $state.lastshadesUp"
    if (device.currentMotor in ["up", "down"]) {
        state.lastshadesUp = device.currentMotor == "up"
        device.stop()
    } else {
        state.lastshadesUp ? device.down() : device.up()
// 	if(state.lastshadesUp) device.down()
// else device.up()
        state.lastshadesUp = !state.lastshadesUp
    }
}


/*
Switch Functions
*/

def switchOffOnReset(devices) {
    log.debug "Off/On Reset: $devices"
    if (devices*.currentValue('switch').contains('on')) {
        devices.off()
        devices.on()
    } else {
        devices.on()
        devices.off()
        devices.on()
    }
}

/*
Misc Functions
*/

def modeSet(mode) {
    log.debug "Changing Mode to: $mode"
    if (location.mode != mode && location.modes?.find { it.name == mode }) setLocationMode(mode)
}

def routineRun(rout) {
    log.debug "Running: $rout"
    location.helloHome.execute(rout)
}

def notificationHandle(msg, inApp) {
    if (inApp == true) {
        log.debug "Push notification sent"
        sendPush(msg)
    }
}

def smsHandle(phone, msg) {
    log.debug "SMS sent"
    sendSms(phone, msg ?: "No custom text entered on: $app.label")
}

def playlistCycle(device) {
//int currPL = device.currentValue('lastRun')
// int nextPL = currPL+1
    device.cycleChild()
//device.on(nextPL)
}

// execution filter methods
private getAllOk() {
    modeOk && daysOk && timeOk
}

private getModeOk() {
    def result = !modes || modes.contains(location.mode)
    result
}

private getDaysOk() {
    def result = true
    if (days) {
        def df = new java.text.SimpleDateFormat("EEEE")
        if (location.timeZone) df.setTimeZone(location.timeZone)
        else df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
        def day = df.format(new Date())
        result = days.contains(day)
    }
    result
}

private getTimeOk() {
    def result = true
    if (starting && ending) {
        def currTime = now()
        def start = timeToday(starting).time
        def stop = timeToday(ending).time
        result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
    }
    result
}

private hhmm(time, fmt = "h:mm a") {
    def t = timeToday(time, location.timeZone)
    def f = new java.text.SimpleDateFormat(fmt)
    f.setTimeZone(location.timeZone ?: timeZone(time))
    f.format(t)
}

private hideOptionsSection() {
    (starting || ending || days || modes || manualCount || fanIgnoreOff || sonos) ? false : true
}

private timeIntervalLabel() {
    (starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}

private def textHelp() {
    def text =
            section("User's Guide - Advanced Button Controller") {
                paragraph "This smart app allows you to use a device with buttons including, but not limited to:\n\n Aeon Labs Minimotes\n" +
                        "HomeSeer HS-WD100+ switches**\n HomeSeer HS-WS100+ switches\n Lutron Picos***\n" +
                        "Hue Dimmer switches***\n" +
                        "It is a heavily modified version of @dalec's 'Button Controller Plus' which is in turn " +
                        "a version of @bravenel's 'Button Controller+'."
            }

    section("Some of the included changes are:") {
        paragraph "A complete revamp of the configuration flow. You can now tell at a glance, what has been configured for each button." +
                "The button configuration page has been collapsed by default for easier navigation."
        paragraph "The original apps were hardcoded to allow configuring 4 or 6 button devices. " +
                "This app will automatically detect the number of buttons on your device or allow you to manually " +
                "specify (only needed if device does not report on its own)."
        paragraph "Allows you to give your button device full speaker control including: Play/Pause, NextTrack, Mute, VolumeUp/Down." +
                "(***Standard Pico remotes can be converted to Audio Picos)\n\nThe additional control options have been highlighted below."
    }

    section("Available Control Options are:") {
        paragraph "	Switches - Toggle \n" +
                "	Switches - Turn On \n" +
                "	Switches - Turn Off \n" +
                "	Dimmers - Toggle \n" +
                "	Dimmers - Set Level (Group 1) \n" +
                "	Dimmers - Set Level (Group 2) \n" +
                "	*Dimmers - Inc Level \n" +
                "	*Dimmers - Dec Level \n" +
                "	Fans - Low, Medium, High, Off \n" +
                "	Shades - Up, Down, or Stop \n" +
                "	Locks - Unlock Only \n" +
                "	Speaker - Play/Pause \n" +
                "	*Speaker - Next Track \n" +
                "	*Speaker - Previous Track \n" +
                "	*Speaker - Mute/Unmute \n" +
                "	*Speaker - Volume Up \n" +
                "	*Speaker - Volume Down \n" +
                "	Set Modes \n" +
                "	Run Routines \n" +
                "	Sirens - Toggle \n" +
                "	Push Notifications \n" +
                "	SMS Notifications \n" +
                "	Off-On Reset "
    }

    section("** Quirk for HS-WD100+ on Button 5 & 6:") {
        paragraph "Because a dimmer switch already uses Press&Hold to manually set the dimming level" +
                " please be aware of this operational behavior. If you only want to manually change" +
                " the dim level to the lights that are wired to the switch, you will automatically" +
                " trigger the 5/6 button event as well. And the same is true in reverse. If you" +
                " only want to trigger a 5/6 button event action with Press&Hold, you will be manually" +
                " changing the dim level of the switch simultaneously as well.\n" +
                "This quirk doesn't exist of course with the HS-HS100+ since it is not a dimmer."
    }

    section("*** Lutron Pico Requirements:") {
        paragraph "Lutron Picos are not natively supported by SmartThings. A Lutron SmartBridge Pro, a device running @njschwartz's python script (or node.js) and the Lutron Caseta Service Manager" +
                " SmartApp are also required for this functionality!\nSearch the forums for details."
    }

    section("*** Hue Dimmer Switch:") {
        paragraph "Hue Dimmer switch will require a device type handler that reports button numbered 1-4 not on,up,down& off. use the DTH from \n" +
                " SmartThings-PS/SmartThings/hue-dimmer-switch-zha"
    }

    section("*** Inovelli Red Series:") {
        paragraph "Added support for Inovelli Red Series including config button (7)"
    }
}

def getButtonType(buttonName) {
    if (buttonName.contains("Aeon Minimote")) return "Aeon Minimote"
    if (buttonName.contains("Hue Dimmer")) return "Hue Dimmer"
    if (buttonName.contains("Cube Controller")) return "Cube Controller"
    if (buttonName.contains("Inovelli") && buttonName.contains("Red Series")) return "Inovelli Red"
    return buttonName
}

def getSpecText(currentButton) {
    currentButton = (currentButton == null ? state.currentButton : currentButton)
    if (state.buttonType == "Lutron Pico") {
        switch (state.currentButton) {
            case 1: return "Top Button"; break
            case 2: return "Bottom Button"; break
            case 3: return "Middle Button"; break
            case 4: return "Up Button"; break
            case 5: return "Down Button"; break
        }
    }

    if (state.buttonType == "Hue Dimmer") {
        switch (state.currentButton) {
            case 1: return "On Button"; break
            case 2: return "Up Button"; break
            case 3: return "Down Button"; break
            case 4: return "Off Button"; break
        }
    }

    if (state.buttonType == "Cube Controller") {
        switch (state.currentButton) {
            case 1: return "Shake Cube"; break
            case 2: return "Flip Cube 90 Degrees"; break
            case 3: return "Flip Cube 180 Degrees"; break
            case 4: return "Slide Cube"; break
            case 5: return "Knock Cube"; break
            case 6: return "Rotate Cube Right"; break
            case 7: return "Rotate Cube Left"; break
        }
    }
    if (state.buttonType == "Aeon Minimote") {
        switch (state.currentButton) {
            case 1: return "Top Left Button"; break
            case 2: return "Top Right Button"; break
            case 3: return "Lower Left Button"; break
            case 4: return "Lower Right"; break
        }
    }
    if (state.buttonType.contains("WD100+ Dimmer")) {
        switch (state.currentButton) {
            case 1: return "Double-Tap Upper Paddle"; break
            case 2: return "Double-Tap Lower Paddle"; break
            case 3: return "Triple-Tap Upper Paddle"; break
            case 4: return "Triple-Tap Lower Paddle"; break
            case 5: return "Press & Hold Upper Paddle\n(See user guide for quirks)"; break
            case 6: return "Press & Hold Lower Paddle\n(See user guide for quirks)"; break
            case 7: return "Single Tap Upper Paddle\n(See user guide for quirks)"; break
            case 8: return "Single Tap Lower Paddle\n(See user guide for quirks)"; break
        }
    }
    if (state.buttonType.contains("WS100+ Switch")) {
        switch (state.currentButton) {
            case 1: return "Double-Tap Upper Paddle"; break
            case 2: return "Double-Tap Lower Paddle"; break
            case 3: return "Triple-Tap Upper Paddle"; break
            case 4: return "Triple-Tap Lower Paddle"; break
            case 5: return "Press & Hold Upper Paddle"; break
            case 6: return "Press & Hold Lower Paddle"; break
            case 7: return "Single Tap Upper Paddle"; break
            case 8: return "Single Tap Lower Paddle"; break
        }
    }

    if (state.buttonType.contains("WD200+ Dimmer")) {
        switch (state.currentButton) {
            case 1: return "Double-Tap Upper Paddle"; break
            case 2: return "Double-Tap Lower Paddle"; break
            case 3: return "Triple-Tap Upper Paddle"; break
            case 4: return "Triple-Tap Lower Paddle"; break
            case 5: return "Press & Hold Upper Paddle\n(See user guide for quirks)"; break
            case 6: return "Press & Hold Lower Paddle\n(See user guide for quirks)"; break
            case 7: return "Single Tap Upper Paddle\n(See user guide for quirks)"; break
            case 8: return "Single Tap Lower Paddle\n(See user guide for quirks)"; break
            case 9: return "4X-Tap Upper Paddle"; break
            case 10: return "4X-Tap Lower Paddle"; break
            case 11: return "5X-Tap Upper Paddle"; break
            case 12: return "5X-Tap Lower Paddle"; break
        }
    }

    if (state.buttonType.contains("WS200+ Switch")) {
        switch (state.currentButton) {
            case 1: return "Double-Tap Upper Paddle"; break
            case 2: return "Double-Tap Lower Paddle"; break
            case 3: return "Triple-Tap Upper Paddle"; break
            case 4: return "Triple-Tap Lower Paddle"; break
            case 5: return "Press & Hold Upper Paddle"; break
            case 6: return "Press & Hold Lower Paddle"; break
            case 7: return "Single Tap Upper Paddle"; break
            case 8: return "Single Tap Lower Paddle"; break
            case 9: return "4X-Tap Upper Paddle"; break
            case 10: return "4X-Tap Lower Paddle"; break
            case 11: return "5X-Tap Upper Paddle"; break
            case 12: return "5X-Tap Lower Paddle"; break
        }
    }

    if (state.buttonType.contains("Inovelli")) {
        switch (state.currentButton) {
            case 1: return "NOT OPERATIONAL - DO NOT USE"; break
            case 2: return "2X Tap Upper Paddle = Pushed\n2X Tap Lower Paddle = Held"; break
            case 3: return "3X Tap Upper Paddle = Pushed\n3X Tap Lower Paddle = Held"; break
            case 4: return "4X Tap Upper Paddle = Pushed\n4X Tap Lower Paddle = Held"; break
            case 5: return "5X Tap Upper Paddle = Pushed\n5X Tap Lower Paddle = Held"; break
            case 6: return "Hold Upper Paddle = Pushed\nHold Lower Paddle = Held"; break
            case 7: if (state.buttonType.contains("Red")) {
                return "1x Tap Config Button"; break;
            }
        }
    }

    if (state.buttonType.contains("ZRC-90")) {
        switch (state.currentButton) {
            case 1: return "Tap or Hold Button 1"; break
            case 2: return "Tap or Hold Button 2"; break
            case 3: return "Tap or Hold Button 3"; break
            case 4: return "Tap or Hold Button 4"; break
            case 5: return "Tap or Hold Button 5"; break
            case 6: return "Tap or Hold Button 6"; break
            case 7: return "Tap or Hold Button 7"; break
            case 8: return "Tap or Hold Button 8"; break
            case 9: return "2X Tap Button 1\nHold Not Available"; break
            case 10: return "2X Tap Button 2\nHold Not Available"; break
            case 11: return "2X Tap Button 3\nHold Not Available"; break
            case 12: return "2X Tap Button 4\nHold Not Available"; break
            case 13: return "2X Tap Button 5\nHold Not Available"; break
            case 14: return "2X Tap Button 6\nHold Not Available"; break
            case 15: return "2X Tap Button 7\nHold Not Available"; break
            case 16: return "2X Tap Button 8\nHold Not Available"; break
        }
    }

    if (state.buttonType.contains("Zen27")) {
        switch (currentButton) {
            case 1: return "1 x up"; break
            case 2: return "1 x down"; break
            case 3: return "2 x up"; break
            case 4: return "2 x down"; break
            case 5: return "3 x up"; break
            case 6: return "3 x down"; break
            case 7: return "4 x up"; break
            case 8: return "4 x down"; break
            case 9: return "5 x up"; break
            case 10: return "5 x down"; break
        }
    }

    if (state.buttonType == "Ikea Button") {
        if (state.buttonCount == 5) {
            switch (state.currentButton) {
                case 1: return "Up Button"; break
                case 2: return "Right Button"; break
                case 3: return "Down Button"; break
                case 4: return "Left Button"; break
                case 5: return "Middle Button"; break
            }
        }
        if (state.buttonCount == 2) {
            switch (state.currentButton) {
                case 1: return "Up Button"; break
                case 2: return "Down Button"; break
            }
        }
    }
    return " **Not Configured** "
}