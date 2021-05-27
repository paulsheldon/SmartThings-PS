# Welcome to my SmartThings Repository

Here you will find my created SmartApps and Device Handlers as listed below. 

*Please note I acknowledge all original developers of updated Smart Apps & Device Handlers where appropriate*

# Smart Apps

## ABC Advanced Button Controller - Updated 2020-05-05

# Updated Features
 - Added Inovelli Red Series Switch (may also support Black Series)
 - Added ZRC-90 Scene Controller 
 - Added Zen27
 - Added Ikea Button (5 buttons)
 - Added WS200+ Switch & Dimmer
 - Added Sonos Integration (Beta)
 - Updated Lightify Device Handler to allow Colour Temperature settings
 

This is an updated version of **ABC** by Stephan Hackett ([See original repository here](https://github.com/stephack/ABC)).

### Updated Features
 - Added support for **Philips Hue Dimmer**
 - Added option to control **Color Temperature**
 - Code improvements

# Device Handlers

## Hue Dimmer Switch

This is an updated version **Hue Dimmer Switch** by Stephen McLaughlin ([See original repository here](https://github.com/sticks18/Lightify-Bulb))

### Updated Features
 - Added support for **Smart App button numbers (1,2,3,4)**
 - Added option to switch between button names or numbers

## Lightify Bulb -ABC

This is an updated version **Lightify Bulb ** by Scott Gibson ([See original repository here](https://github.com/digitalgecko/mySmartThings)))
This has corrected colour temperature to work with ABC and a few other tweaks

## Zigbee Switch Power

This is an updated version **Zigbee Power Switch** by SmartThings ([See original repository here](https://github.com/SmartThingsCommunity/SmartThingsPublic))

### Updated Features
 - Added support for **Salus SP600 Smart Plug**

## Aqara Cube Controller (MFKZQ01LM).
It is based on code by @DroidSector & @ClassicGod and would not be possible without their work.

_To pair your Aqara Cube Controller with SmartThings HUB follow the instructions outlined here: https://community.smartthings.com/t/xiaomi-zigbee-outlet-steps-to-pair-any-xiaomi-zigbee-device/67582_

This DTH offers 3 modes of operation (that can be changed in the settings):

1. **Simple** (set by default)- designed for backwards compatibility with the previous DTH - presents only 7 buttons for basic gestures like shake, 90-degree flip, 180-degree flip, slide, knock, rotate right and rotate left.
2. **Advanced** - presents 36 buttons for maximum functionality. Buttons are assigned as follows:
 * buttons 1 to 6 - "push" event on face 0 to 5 activation (corresponds to face pointing up)
 * buttons 7 to 12 - "push" event on slide gesture with faces 0 to 5 pointing up
 * buttons 13 to 18 - "push" event on knock gesture with faces 0 to 5 pointing up
 * buttons 19 to 24 - "push" event on right rotation with faces 0 to 5 pointing up
 * buttons 25 to 30 - "push" event on left rotation with faces 0 to 5 pointing up 
 * buttons 31 to 36 - "push" event on shake gesture with faces 0 to 5 pointing up
3. **Combined** - with this DTH will present 43 buttons assigned as follows:
 * buttons 1 to 7 - basic actions just like in Simple mode
 * buttons 8 to 43 - actions from Advanced mode moved by 7 positions.

**I recommend using the Advanced mode**

DTH also offers emulated "Three-Axis" capability for easy usage in SmartApps like Mood Cube. It is dependent on the selected face and affected by the limitation mentioned below.

**_Due to limitations imposed by data sent by the hardware there are some things to keep in mind:_**

* The device sends orientation only on those gestures:
 * 90-degree flip
 * 180-degree flip
 * slide
 * knock
* Because of that orientation data for rotation and shake events is based on the last know orientation.
* Device does not send any data if a gesture is unrecognized - rotating the cube randomly in the air and placing it down will most likely not send any event.
* The DTH will correct the last known orientation and send missing flip/face activation events if needed as soon as it's got the orientation data from the device.
* Due to the above rotation and shake events can execute for wrong faces if the flip gestures are not performed correctly (like rotating the cube randomly in the air)

## Installation
To install the ABC Manager Smartapp 
* Login to the Smartthings IDE at https://graph.api.smartthings.com/
* Click My Locations and select your home location
* Click My SmartApps
* Click New SmartApp
* Fill out the Name, Namespace, Author and Description
* Click From Code tab
* Copy the raw code from the abc-manager.src file and paste in the From Code box
* Click Create
* Click Publish, For Me

Next, create the ABC Child Creator
* Click My SmartApps
* Click New SmartApp
* Fill out the Name, Namespace, Author and Description
* Click From Code tab
* Copy the raw code from the abc-child-creator.src file and paste in the From Code box
* Click Create
* DO NOT PUBLISH

The app will now be available to add to your app.
* Open the SmartThings app
* Click the hamburger menu and select SmartApps
* Click + to add an app
* Scroll down to custom apps and select ABC Manager
* Follow prompts to complete install

