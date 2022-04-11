# DryerReminder

<img src="https://github.com/Mmohler1/DryerReminder/blob/main/Design/ProjectPictures/20220410_174452%20(2).jpg" alt="diagram" width="50%">

###### A Raspberry Pi 3 with a Sense HAT attached to a toy dryer.

## What is the DryerReminder?
The Dryer Reminder is a Raspberry Pi 3 project that a user can attach to their dryer to let them know when it has stopped so that they can collect their laundry. The device can do this using the attached Sense Hat’s accelerometer, which is an instrument that computers can communicate with to read movement. The device then checks for a lack of motion using the accelerometer and if it reads that for long enough then it will determine the dryer is off. The Raspberry Pi with have this process started over a RESTful Service and WebSocket, which communicates with an Android Application. The app was developed with the Samsung Galaxy 8+ in mind and when the process has stopped, the user will get a notification set off from the Pi’s response.

Dryers will often tell their users how long it will take before they can collect their laundry. However, the timer on these dryers are often relative as they change to match the time the appliance thinks it will take for the laundry to get dry. In some cases, this means a load of laundry that had displayed it would take 50 minutes could take 90 minutes. In another load the dryer may even stop earlier, thinking the laundry is dry when it still needs to be on for longer. Some users will set a timer on their phone and guess the time the dryer will take, but this inconsistent strategy leaves many to ignore it and just forget about their laundry. 

If it isn’t already obvious, the above problem has bothered me for many years. It is a running gag in my family that if our dryer says it has 10 minutes left, we gasp and say it will be done in 25, followed by repeating that joke until it finally stops. Not to mention how often I just ignored the timer I set on my phone because of how unlikely it was the dryer stopped this time. I would often image myself with a solution that sent me a notification to my phone when the dryer stops. I really wanted somebody to come up with something and it wasn’t until I had to start thinking about a project for my senior capstone that I figured that person had to be me. 


## High level-functional and non-function requirements

The high-level functional requirement for The Raspberry Pi was simple. An accelerometer needed to be used determine when the dryer had stopped moving. There also needed to be steps put into place to prevent the device from giving a false positive. That way the user is not told the dryer has stopped when the device is still moving. 

The android application had more functional requirements since the user would be interacting with that more then the Pi itself. A notification being set off when the device gets the ok sign from the Raspberry Pi is the most important feature in the application. The mobile application itself also had multiple pages to not only start the device but determine other settings as well. These pages and were Home, Dryer, Settings, Notify, Adjust, and Calibrate. Home is the page the user starts at when they open the application. The Dryer page is what the user uses to start the dryer process on the Pi. The Settings page contains the pages for Calibrate, Adjust, and Reminder. Calibrate is used to calibrate the accelerometer when it is not moving so that the Pi can use it as comparison when the dryer is moving. Adjust controls the sensitivity of the device so the user can set it based on their needs. Finally, notify is a page that allows the user to set multiple notifications after the first one so that they do not forget their laundry. 

The non-functional requirement for the Dryer Reminder was security. Since the Pi will use an API to communicate with itself and the android application, security is a must. Many hackers have used unsecure IOT devices to break into the security of other devices and I did not want my device to be one of those. So, I decided to go with HTTP Authentication that uses a single Bearer token. If the device that is trying to connect to the Pi does not have the same bearer token, then it refuses connection to that device. 

## Technologies used and why?

I used many technologies when developing the Dryer Reminder. Not only was it important to get the hardware and software right for the Pi and mobile application, but also the tools used to develop these applications. Most the decision made to use them came down to three factors. My experience with them, how simple were they to learn if I did not use them, and if they could be freely used.

### Hardware

<b>Raspberry Pi 3 Model B</b> – I was a major fan of the Raspberry Pi devices and hade multiple available to me. I always wanted to develop something that used a Raspberry Pi and this project seemed to be a good choice. 












The design for the entire project can be found here
https://github.com/Mmohler1/DryerReminder/tree/main/Design

The wireframes I made for the android app are also here
https://github.com/Mmohler1/DryerReminder/tree/main/Design/Wireframe

Project Proposal, Requirements, and User Stories
https://github.com/Mmohler1/DryerReminder/tree/main/Design/Project%20Proposal

![Image of Home Wireframe](https://github.com/Mmohler1/DryerReminder/blob/main/Design/Android%20Sitemap.png)

Here is how every page of the android application relates to each other.
