This is an advanced DTH for Xiaomi Magic Cube Controller.
It is based on code by @DroidSector and wouldn't be possible without his work.

_To pair your Xiaomi Magic Cube Controller with SmartThings HUB follow the instructions outlined here: https://community.smartthings.com/t/xiaomi-zigbee-outlet-steps-to-pair-any-xiaomi-zigbee-device/67582_


This DTH offers 3 modes of operation (that can be changed in the settings):

1. **Simple** (set by default)- designed for backwards compatibility with previous DTH - presents only 7 buttons for basic gestures like shake, 90 degree flip, 180 degree flip, slide, knock, rotate right and rotate left.
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

DTH also offers emulated "Three Axis" capability for easy usage in SmartApps like Mood Cube. It is dependent on selected face and affected by limitation mentioned below.

**_Due to limitations imposed by data sent by the hardware there are some things to keep in mind:_**

* The device sends orientation only on those gestures:
 * 90 degree flip
 * 180 degree flip
 * slide
 * knock
* Because of that orientation data for rotation and shake events is based on last know orientation.
* Device does not send any data if gesture is unrecognized - rotating the cube randomly in the air and placing it down will most likely not send any event.
* The DTH will correct last known orientation and send missing flip/face activation events if needed as soon as it's gets the orientation data form the device.
* Due to the above rotation and shake events can execute for wrong faces if the flip gestures are not performed correctly (like rotating the cube randomly in the air)
