
/**************************************************************/
/* Security Lights - Front of House                           */
/**************************************************************/
/* Author     : Paul Sheldon                                  */
/* Created    : 30/12/2018, 12:11:29                          */
/* Modified   : 02/01/2019, 21:26:50                          */
/* Build      : 13                                            */
/* UI version : v0.3.109.20181207                             */
/**************************************************************/

settings
disable command optimization;
end settings;

define
device deviceSensors = Porch Motion;
device deviceLights = Porch Light;
string speechText; /* Front Lights Turned OFF */
boolean speechForce = false;
const integer plus30Mins = 30;
const integer minus30Mins = -30;
const integer timeDelay = 1;
const time timeOff = 23:59:00;
const integer maxLight = 100;
const integer minLight = 0;
const integer midLight = {(maxLight-minLight)/2};
end define;

execute
on events from
{deviceSensors}'s motion
do
switch ({deviceSensors}'s motion)
case 'inactive':
if
Time is between {plus30Mins} minutes past sunrise and {minus30Mins} minutes past sunset, but only in January, February, October, November, or December
and
Time is any, but only in March, April, May, June, July, August, or September
and
{deviceLights}'s level is different than {minLight}%
then
/* Turn off all Lights */
with
{deviceLights}
do
Cancel all pending tasks;
Set level to {minLight}%;
Turn off;
Set variable {speechText} = Front Lights Turned Off;
Execute piston "Home \ webCoRE \ Speech Controller" with arguments {speechText,speechForce};
end with;
end if;
case 'active':
if
Time is between {timeOff} and sunrise, but only in January, February, October, November, or December
and
{deviceLights}'s level is different than {midLight}%
then
with
{deviceLights}
do
Cancel all pending tasks;
Set level to {midLight}%;
Turn on;
Set variable {speechText} = Front Lights Turned ON;
Execute piston "Home \ webCoRE \ Speech Controller" with arguments {speechText,speechForce};
Wait 2 minutes;
Set level to {minLight}%;
Turn off;
Set variable {speechText} = Front Lights Turned OFF;
Execute piston "Home \ webCoRE \ Speech Controller" with arguments {speechText,speechForce};
end with;
else if
Time is between {minus30Mins} minutes past sunset and {timeOff}, but only in January, February, October, November, or December
and
{deviceLights}'s level is outside of range {maxLight}% and {midLight}%
then
/* Turn lights to 100%, wait 5 mins then turn to 50% */
with
{deviceLights}
do
Log info "Max then half";
Cancel all pending tasks;
Log info "Tasks Cancelled";
Turn on;
Set level to {maxLight}%;
Set variable {speechText} = Front Lights Turned to MAX;
Execute piston "Home \ webCoRE \ Speech Controller" with arguments {speechText,speechForce};
Log info "Wait 5 mins";
Wait 2 minutes;
Set level to {midLight}%;
Set variable {speechText} = Front Lights Turned to HALF;
Execute piston "Home \ webCoRE \ Speech Controller" with arguments {speechText,speechForce};
end with;
else
with
{deviceLights}
do
Cancel all pending tasks;
Set level to {midLight}%;
Turn on;
Set variable {speechText} = Front Lights Turned ON to Half;
Execute piston "Home \ webCoRE \ Speech Controller" with arguments {speechText,speechForce};
Wait 2 minutes;
Set level to {minLight}%;
Turn off;
Set variable {speechText} = Front Lights Turned OFF;
Execute piston "Home \ webCoRE \ Speech Controller" with arguments {speechText,speechForce};
end with;
end if;
end switch;
end on;
async if
Date & Time is between sunset and {timeOff}
and
{deviceLights}'s switch is off
then
with
{deviceLights}
do
Turn on;
end with;
end if;
end execute;
