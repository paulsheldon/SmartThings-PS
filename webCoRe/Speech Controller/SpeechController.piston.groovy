
/**************************************************************/
/* Speech Controller                                          */
/**************************************************************/
/* Author     : Paul Sheldon                                  */
/* Created    : 22/12/2018, 00:30:13                          */
/* Modified   : 24/12/2018, 13:33:53                          */
/* Build      : 33                                            */
/* UI version : v0.3.109.20181207                             */
/**************************************************************/
 
define
device speakerList = Office Speaker;
device activeSpeaker; /* Office Speaker */
integer activeLevel; /* */
boolean forceSpeech = {$args.forceSpeech};
string speechEnabled = 'Speech is now Enabled';
string speechDisabled = 'Speech is now Disabled';
string speechText = {if($args.speechText,$args.speechText,speechEnabled)};
end define;
 
execute
if
(
System Speak's switch is on
or
{forceSpeech} is true
)
and
{text(speechText)} is not {text(null)}
then
switch ({forceSpeech})
case {true}:
for each (activeSpeaker in {speakerList})
do
with
{activeSpeaker}
do
Set variable {activeLevel} = {activeSpeaker}'s level;
Set level to 60%;
Speak "{speechText}";
Set level to {activeLevel}%;
end with;
end for each;
case {false}:
with
{activeSpeaker}
do
Speak "{speechText}";
end with;
end switch;
else if
System Speak's switch changes to off
then
with
{speakerList}
do
Speak "{speechDisabled}";
Wait 10 seconds;
Speak text "{speechDisabled}";
end with;
else
with
{speakerList}
do
Speak "{speechEnabled}";
end with;
end if;
end execute;
