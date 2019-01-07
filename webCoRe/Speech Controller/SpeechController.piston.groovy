
/**************************************************************/
/* Speech Controller                                          */
/**************************************************************/
/* Author     : Paul Sheldon                                  */
/* Created    : 06/01/2019, 21:26:31                          */
/* Modified   : 07/01/2019, 13:00:37                          */
/* Build      : 27                                            */
/* UI version : v0.3.109.20181207                             */
/**************************************************************/

define
device speakerList = Office Speaker;
device speaker; /* Office Speaker */
string speechText = {if($args.speechText,$args.speechTest,"")};
boolean speechOverride = {if($args.speechOverride,$args.speechOverride,false)};
end define;

execute
if
(
System Speech Toggle's switch is on
or
{speechOverride} is true
)
and
{speechText} is not {text(null)}
then
switch ({speechOverride})
case 'true':
for each (speaker in {speakerList})
do
with
{speaker}
do
Speak text "{speechText}";
end with;
end for each;
case 'false':
with
{speaker}
do
Speak text "{speechText}";
end with;
end switch;
end if;
on events from
System Speech Toggle's switch
do
with
Office Speaker
do
Speak text "{concat("Speech is now ",[$currentEventDevice:switch]=="on"? "enabled":"disabled")}";
end with;
end on;
end execute;
