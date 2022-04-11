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

I used many technologies when developing the Dryer Reminder. Not only was it important to get the hardware and software right for the Pi and mobile application, but also the tools used to develop these applications. Most the decision made to use them came down to three factors. My experience with them, how simple were they to learn if I had no experience, and if they could be freely used.

### Hardware

<b>Raspberry Pi 3 Model B</b> – I am a major fan of the Raspberry Pi devices and hade multiple in my inventory, ready to use. I always wanted to develop something that used a Raspberry Pi and this project seemed to be a good choice. 

<b>Sense HAT</b> – The Sense HAT was an easy pick for an accelerometer to work with. I tested my idea with other accelerometers and while they worked too, no libraries were publicly available for me to add to the project. The Sense HAT not only had a free library, but it was much easier to setup and use.

<b>Samsung Galaxy S8+</b> - This phone was chosen as the only android device I developed for as it is my current phone. I had made some tutorial applications with it in the past, so I had some experience with it as well.

### Languages

<b>Python</b> – When it comes to developing on the Raspberry Pi the two most common languages are C and Python. Out of the two I had the most experience with Python, especially working with imbedded hardware. While C runs faster, speed is not a factor for this project and since I have plans to make this open source the readability of Python made it the language of choice.  

<b>Java</b> – Android development was going to be new for me, so I knew I wanted to work with a language that I understand the most. I have the most experience with Java as a language and have barely investigated Kotilin, so I did not want to learn a new language and architecture at the same time. The default language on my IDE was JDK 11 so I decided to stick with that for the project.

### RESTful Service and Client

<b>Flask</b> – The research I had done with deploying a RESTful service on the Raspberry Pi had Flask come out as the number one pick. It was fairly easy for me to setup a service that I could communicate with using PostMan so it was an easy decision to make. It also supported Basic HTTP authentication, which I needed for my security. 

<b>OkHttp</b> – Since I was new with android development, I wanted to find a library that I could practice with on my desktop first. Thankfully, I was able to setup the security and connect to the service on the Raspberry Pi easily and was even able to copy and paste my Java code from my Spring Boot Application to my Android Application

### Integrated Development Environment (IDE)

<b>Android Studio</b> – My little experience I had developing for Android was on Android Studio. I did not want to spend more time learning a different IDE when exploring Androids architecture. 

<b>Thonny</b> – I have had the most experience developing python applications using Thonny and wanted to stick with it for most of my project. I did decide to turn to another IDE for the bulk of development with the Pi Application, but much of the early code on the Pi and testing was done with Thonny since it is far less taxing on the Pi.

<b>VSCode</b> – A very powerful IDE that made the refactoring of my prototype to the finish product much simpler. Near the end of development 

## New Technologies

While I had some experience with many of the technologies above, it was a new experience working with them the way that I did. Most of the new technologies like Flask and Okhttp were used because they were not only popular, but easy to learn. This made it easier to find information on the technology when debugging issues. It was necessary because I had no experience with a REST API between two devices that are not running on the same machine. I had never run them on both a Raspberry Pi and Android Device before the Dryer Reminder was being developed.

Much of the decisions I made on Android were recommended in the documentation by Google themselves. Gradle, activity fragments, workers and more were all used by the official tutorials that Android has available. I choose Android itself because it is what I have used personally for the past few years and had very some experience with making copycat applications with tutorials, but nothing I personally made myself. I see a lot of use in android development as phone applications are the new standard. So having experience under my belt for developing for one now will be useful in the future.
  
While I had experience with Raspberry Pi’s before, I did not have any using it with imbedded hardware.  I had even practiced over the summer with a breadboard and python applications to get an idea of how they work and can be wired up. This did not end up being needed in the final product, but it was useful for experimenting if an accelerometer could even read the movement coming from the dryer. It even allowed me to develop a very early protype for detecting motion that was essential for developing the project.

## Technical Approach

I had decided to use Object Oriented Programming principles when it came to developing for both the Android Application and the Raspberry Pi. I had plenty of experience using OOP when programing for Java based applications, but not with Python applications. I stuck with OOP for Python since I knew it would make my code more readable and easier to use when running my API. Logging was also a major part of both applications. Since my device runs off an API, it was important to include the date and time in the logs so I could sync them up if an error occurred. This way it would be easier to discover where the problem is. The logs print where they are at in both application, what they are doing, and what data they are processing. The Python code only uses debug for its logging levels, but the java application also takes advantage of info, warning, and error levels.

Much of the documentation of the project was done before work began. A Sitemap was made up to showcase how users will navigate the website. A logical diagram was used to show how the application on both devices communicate with each other and what technologies were used during that process. Wireframes were setup to get an idea how the android pages would look, and more. All of these documents can be found [Here](https://github.com/Mmohler1/DryerReminder/tree/main/Design), but here is a sample of a few of them.

<img src="https://github.com/Mmohler1/DryerReminder/blob/main/Design/Android%20Sitemap.png" alt="diagram" width="75%">

###### Android Sitemap that details the path users will take to navigate the application
---

<img src="https://github.com/Mmohler1/DryerReminder/blob/main/Design/Logical.png" alt="diagram" width="70%">

###### Logical Diagram of the entire project
---
  
<img src="https://github.com/Mmohler1/DryerReminder/blob/main/Design/FlowchartNew.png" alt="diagram" width="60%">

###### Flowchart of how the Raspberry Pi determines the user's dryer has stopped

  
## Risks and Challenges

The biggest risk of this entire project was if an accelerometer could read the difference between the dryer when it is and is not moving. Before I had even started the project, I knew that if I was going to get this to work, I would have to see if an accelerometer, that is available for the Raspberry Pi, could detect motion from a dryer. I had made a simple protype made up using a breadboard, MPU6050, and a small LCD screen to read the movement. Using a camera from my phone, I had recorded the prototype while moving and not moving on the dryer. I then wrote down the highest and lowest values from each recording. It was then I knew my idea could work and started to decide on how I could implement it with code.

Another issue that I had avoided thanks to planning is that I decided to go with a Raspberry Pi 3 Model B because I had two of them available to use. Part way through the begging of the planning phases my device had broken, which would have left me in a jam trying to test technologies in the early phase. Not having my Raspberry Pi during critical moments where I needed to showcase my device would have been detrimental too. However, since I was prepared, I had a second one ready to use and even bought a third one, along with a second Sense HAT to avoid this problem if it happened again in the future.
  
A big risk for any project is losing all the work that one has made up to that point. Hardware failures, losing a laptop, hard drive malfunctions, etc. As any good programmer should do, I had kept my Python and Java code updated on my GitHub repository and committed any changes during and after my code sessions. I also keep all my school files, which include my documentation, on my OneDrive. This means that any failure on my laptop or desktop would mean I cannot easily access my files with another device. I have also uploaded my documentation to GitHub which would give me another place to get my documentation if necessary.
  
Out of all challenges for this entire project was how much time it would take to complete the entire thing. I was not only making a programing for the Raspberry Pi, but a responsive application on android with multiple pages and features. Senior year at university was nothing to scoff at and I had to do that with my part time job and an internship I started during the second half of development. I used Monday.com to keep track of my school assignments and my Dryer Reminder sprints. Notably I took the initiative during my two-week winter break and developed the entire application for the Raspberry Pi. This gave me plenty of time to develop my Android application and by mid-March I had completed 90% of my project. I had enough time available to me that I experiment with a WebSocket and had it finished before my Capstone Showcase. 
 
