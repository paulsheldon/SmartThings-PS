
/**************************************************************/
/* Speech Controller                                          */
/**************************************************************/
/* Author     : Paul Sheldon                                  */
/* Created    : 22/12/2018, 00:30:13                          */
/* Modified   : 27/12/2018, 19:40:06                          */
/* Build      : 40                                            */
/* UI version : v0.3.109.20181207                             */
/**************************************************************/

define
device speakerList = Office Speaker;
device activeSpeaker; /* Office Speaker */
integer activeLevel; /* */
boolean forceSpeech = {if($args.forceSpeech,$args.forceSpeech,false)};
string speechEnabled = 'Speech is now Enabled';
string speechDisabled = 'Speech is now Disabled';
string speechText = {if($args.speechText,$args.speechText,"")};
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
end with;
else
with
{speakerList}
do
Speak "{speechEnabled}";
end with;
end if;
end execute;
