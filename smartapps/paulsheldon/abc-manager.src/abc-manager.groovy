/**
 *
 *  Child Creator - Advanced Button Controller
 *
 *  Author: Paul Sheldon (Original by Stephan Hackett)
 *
 * 6/20/17 - fixed missing subs for notifications
 * 1/07/18 - split smartApp into Parent/Child (IOS hanging on initial startup) - requires complete uninstall and reinstall of Parent and child SmartApps
 * 1/14/18a - updated version check code
 *
 * Code now maintained by Paul Sheldon *
 * 2019-02-05   - added support for Hue Dimmer & color temperature
 * 2019-09-25   - updated volume control, play/pause, next/previous track and mute/unmute for Sonos speakers - code provided by Gabor Szabados
 * 2020-01-02   - added support (beta) for fan control, Inovelli Red Series Switch & Dimmer (inc config button 7)
 * 2020-05-05   - added support WS200 Dimmer & Switch
 * 2021-07-02 - Tidy up code
 *
 **/

/**********************************************************************
 *  Define App Parameters
 *********************************************************************/
definition(
        name: "ABC Manager",
        namespace: "paulsheldon",
        author: "Paul Sheldon (Original by Stephan Hackett)",
        description: "Advanced Button Controller",
        category: "My Apps",
        iconUrl: "https://raw.githubusercontent.com/paulsheldon/SmartThings-PS/master/resources/abc/images/abcNew.png",
        iconX2Url: "https://raw.githubusercontent.com/paulsheldon/SmartThings-PS/master/resources/abc/images/abcNew.png",
        iconX3Url: "https://raw.githubusercontent.com/paulsheldon/SmartThings-PS/master/resources/abc/images/abcNew.png",
        singleInstance: true,
)

preferences {
    page(name: "pageMain", title: "ABC Mappings", install: true, uninstall: true)
    page(name: "pageAbout", title: "About ABC Manager")
}

/**********************************************************************
 *  Setup and Configuration Commands:
 *********************************************************************/
def installed() {
    log.debug "${app.label}: Installed"
}

def uninstalled() {
    log.debug "${app.label}: Uninstalled"
}

def updated() {
    log.debug "${app.label}: Updated"
    unsubscribe()
}

/**********************************************************************
 *  Page Definitions
 *********************************************************************/
def pageMain() {
    dynamicPage(name: "pageMain") {
        def childApps = getAllChildApps()
        def childVersion = "InitialSetup"
        if (childApps.size() > 0) {
            childVersion = childApps.first().version()
            log.debug "Using Child Version ${childApps.first().version()}"
        }
        section("Create a new button device mapping.") {
            app(name: "childApps", appName: "ABC Child Creator", namespace: "paulsheldon", title: "Create New Button Device Mapping", multiple: true)
        }
        section("Version Info, User's Guide") {
            href(name: "pageAbout",
                    title: "Advanced Button Controller \n" + childVersion,
                    description: "Tap to get Smart App Info and User's Guide.",
                    image: versionCheck(childVersion),
                    page: "pageAbout"
            )
        }
        remove("Uninstall ABC Controller", "WARNING!!", "This will remove the ENTIRE SmartApp, including all configs listed above.")
    }
}

def pageAbout() {
    dynamicPage(name: "pageAbout") {
        section("User's Guide - Advanced Button Controller") {
            paragraph "This smart app allows you to use a device with buttons including, but not limited to:\n\n" +
                    "  Aeon Labs Minimotes\n" +
                    "  HomeSeer HS-WD100+ switches**\n" +
                    "  HomeSeer HS-WS100+ switches\n" +
                    "  Lutron Picos***\n" +
                    "  Philips Hue Dimmer Switches****\n\n" +
                    "  Inovelli Switch/Dimmer Red Series\n\n" +
                    "It is a heavily modified version of @dalec's 'Button Controller Plus' which is in turn a version of @bravenel's 'Button Controller+'."
        }
        section("Latest changes:") {
            paragraph "Added Inovelli Red Series.\n\n"
            paragraph "Added Sonos Device ##.\n\n" +
                    "You can now use a Sonos device as the music player." +
                    "Incorporates Gabor Szabados @gszabados code."
            paragraph "Added Color Temperature ##.\n\n" +
                    "Updated to include Philips Hue Dimmers\nA device handler can be found at @paulsheldon " +
                    "https://github.com/paulsheldon/SmartThings-PS to allow dimmer to work in Smart Apps"
            paragraph "A complete revamp of the configuration flow. You can now tell at a glance, what has been configured for each button. " +
                    "The button configuration page has been collapsed by default for easier navigation."
            paragraph "The original apps were hardcoded to allow configuring 4 or 6 button devices. " +
                    "This app will automatically detect the number of buttons on your device or allow you to manually " +
                    "specify (only needed if device does not report correct number of buttons)."
            paragraph "Allows you to give your button device full speaker control including: Play/Pause, NextTrack, Mute, VolumeUp/Down. " +
                    "(***Standard Pico remotes can be converted to Audio Picos)\n\nThe additional control options have been highlighted below #."
        }
        section("Available Control Options:") {
            paragraph "	Switches - Toggle \n" +
                    "	Switches - Turn On \n" +
                    "	Switches - Turn Off \n" +
                    "	Dimmers - Toggle \n" +
                    "	Dimmers - Set Level (Group 1) \n" +
                    "	Dimmers - Set Level (Group 2) \n" +
                    "	Dimmers - Inc Level \n" +
                    "	Dimmers - Dec Level \n" +
                    "	Color Temperature - Inc Level \n" +
                    "	Color Temperature - Dec Level \n" +
                    "	Fans - Low, Medium, High, Off \n" +
                    "	Shades - Up, Down, or Stop \n" +
                    "	Locks - Unlock Only \n" +
                    "	Speaker - Play/Pause \n" +
                    "	Speaker - Next Track \n" +
                    "	Speaker - Previous Track \n" +
                    "	Speaker - Mute/Unmute \n" +
                    "	Speaker - Volume Up \n" +
                    "	Speaker - Volume Down \n" +
                    "	Set Modes \n" +
                    "	Run Routines \n" +
                    "	Sirens - Toggle \n" +
                    "	Push Notifications \n" +
                    "	SMS Notifications"
        }
        section("** Quirk for HS-WD100+ on Button 5 & 6:") {
            paragraph "Because a dimmer switch already uses Press&Hold to manually set the dimming level " +
                    "please be aware of this operational behavior. If you only want to manually change " +
                    "the dim level to the lights that are wired to the switch, you will automatically " +
                    "trigger the 5/6 button event as well. And the same is true in reverse. If you " +
                    "only want to trigger a 5/6 button event action with Press&Hold, you will be manually " +
                    "changing the dim level of the switch simultaneously as well.\n" +
                    "This quirk doesn't exist of course with the HS-HS100+ since it is not a dimmer."
        }
        section("*** Lutron Pico Requirements:") {
            paragraph "Lutron Picos are not natively supported by SmartThings. A Lutron SmartBridge Pro, a device running @njschwartz's python script (or node.js) and the Lutron Caseta Service Manager " +
                    "SmartApp are also required for this functionality!\nSearch the forums for details."
        }
        section("**** Philips Hue Dimmer:") {
            paragraph "Philips Hue Dimmers are not natively supported by SmartThings.\n" +
                    "A device handler that works with Smart Apps can be found on @paulsheldon\n https://github.com/paulsheldon/SmartThings-PS"
        }
    }
}

/**********************************************************************
 *  Image Check - used to check for new app versions
 *********************************************************************/
def versionCheck(childVersion) {
    log.debug "Looking for Version ${childVersion}"
    def params = [
            uri: "https://raw.githubusercontent.com/paulsheldon/SmartThings-PS/master/resources/abc/images/abc_${childVersion}.png",
    ]
    try {
        httpGet(params) { resp ->
            resp.headers.each {
                //log.debug "${it.name} : ${it.value}"
            }
            log.debug "ABC appears to be running the latest Version"
            return params.uri
        }
    } catch (e) {
        log.error "ABC does not appear to be the latest version: Please update from IDE ${childVersion}"
        return "https://raw.githubusercontent.com/paulsheldon/SmartThings-PS/master/resources/abc/images/update.png"
    }
}