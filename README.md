# DryerReminder

<img src="https://github.com/Mmohler1/DryerReminder/blob/main/Design/PiLogo.jpg" alt="diagram" width="20%">
<img src="https://github.com/Mmohler1/DryerReminder/blob/main/Design/Android-Logo-500x313.png" alt="diagram" width="20%">

## What is the DryerReminder?
The Dryer Reminder is a Raspberry Pi 3 project that a user can attach to their dryer to let them know when it has stopped so that they can collect their laundry. The device can do this using the attached Sense Hat’s accelerometer, which is an instrument that computers can communicate with to read movement. The device then checks for a lack of motion using the accelerometer and if it reads that for long enough then it will determine the dryer is off. The Raspberry Pi with have this process started over a RESTful Service and WebSocket, which communicates with an Android Application. The app was developed with the Samsung Galaxy 8+ in mind and when the process has stopped, the user will get a notification set off from the Pi’s response.

Dryers will often tell their users how long it will take before they can collect their laundry. However, the timer on these dryers are often relative as they change to match the time the appliance thinks it will take for the laundry to get dry. In some cases, this means a load of laundry that said would take 50 minutes could take an hour and a half. In another load the dryer may even stop earlier, thinking the laundry is dry when it still needs to be on for longer. Some users will set a timer on their phone and guess the time the dryer will take, but this inconsistent strategy leaves many to ignore it and just forget about their laundry. 

If it isn’t already obvious, the above problem has bothered me for many years. It a running gag in my family that if our dryer says it has 10 minutes left, we gasp and say it will be done in 25, followed by repeating that joke until it finally stops. Not to mention how often I just ignored the timer I set on my phone because of how unlikely it was the dryer stopped this time. I would often image myself with a solution that sent me a notification to my phone when the dryer stops. I really wanted somebody to come up with something and it wasn’t until I had to start thinking about a project for my senior capstone that I figured that person had to be me. 

https://github.com/Mmohler1/DryerReminder/blob/main/Python%20Code/Pi/DryerCheck.py 




<img src="https://github.com/Mmohler1/DryerReminder/blob/main/Design/Logical.png" alt="diagram" width="60%">
Architecture of the project and what software it will use.
 


The design for the entire project can be found here
https://github.com/Mmohler1/DryerReminder/tree/main/Design

The wireframes I made for the android app are also here
https://github.com/Mmohler1/DryerReminder/tree/main/Design/Wireframe

Project Proposal, Requirements, and User Stories
https://github.com/Mmohler1/DryerReminder/tree/main/Design/Project%20Proposal

![Image of Home Wireframe](https://github.com/Mmohler1/DryerReminder/blob/main/Design/Android%20Sitemap.png)

Here is how every page of the android application relates to each other.
