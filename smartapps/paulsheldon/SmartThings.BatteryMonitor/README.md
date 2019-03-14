# BatteryMonitor SmartApp for SmartThings
===========

Copyright (c) 2014 [Brandon Gordon](https://github.com/notoriousbdg)

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
for the specific language governing permissions and limitations under the License.

##Overview

This SmartApp helps you monitor the status of your SmartThings devices with batteries.

##Install Procedure

1. Create new SmartApp at https://graph.api.smartthings.com/ide/apps.
2. Install the newly created SmartApp in the SmartThings mobile application.
3. Follow the prompts to configure.
4. Tap Status to view battery level for all devices.


##Revision History

2014-11-14  v0.0.1
* Initial release

2014-11-15  v0.0.2
* Moved status to main page
* Removed status page
* Improved formatting of status page
* Added low, medium, high thresholds
* Handle battery status strings of OK and Low

2014-11-15  v0.0.3
* Added push notifications

2014-11-20  v0.0.4
* Added error handling for batteries that return strings

2014-12-26  v0.0.5
* Move app metadata to a new about page
* Changed notifications to only send at specified time daily

The latest version of this file can be found at:
  https://github.com/notoriousbdg/SmartThings.BatteryMonitor
