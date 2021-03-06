# RAM
RAM is a simple daily tasks manager created for learning purpose only. Allows to store tasks with additional data and display them as list.
## Description
### Requirements
- Minimum Android SDK version: 23
- Maximum Android SDK version: 31
- No permissions required
### Task types
**Meeting** creates time interval from its start till end where user is busy. Meetings cannot overlap each other. Meeting displays its start and end. Notifications notify before meeting starts.<br/>
**Deadline** is a task that should de done till its end. Unlike meeting, deadlines do not specify how much time allocated for task and therefore can overlap each other. Good example for deadline is "Finish project". Deadline displays its end only. Notifications notify before deadline expires. <br/>
**Task type can not be changed in existind task!**
### Additional data
Each task can have contact list and simple list.<br/> 
**Simple list** stores any text item user sees fit. It can be grocery list, not to forget list, list of attendees and so on. <br/>
**Contact list** stores contact items only. Each item belongs to one of three categories: email, phone, other. Clicking on item that does not belong to other category, opens on top default App for contact type with prepopulated data (like email address or phone number).<br/>
<img src="https://user-images.githubusercontent.com/98648558/152688330-36db5a30-f972-48ea-87d6-320d965dd8b6.png" width="40%">
<img src="https://user-images.githubusercontent.com/98648558/152688319-b0975fd0-042e-4b89-9d57-32255b854f00.png" width="40%">
### Screens
**Main list** displays list of tasks for current date and task type. Each item shows task description, time, and icon for additional data. Bottom menu allows to traverse dates.<br/>
**Task form** adds new task or modifies existing. Also it allows to access additional data attached to task.<br/>
<img src="https://user-images.githubusercontent.com/98648558/152689283-53fd3807-cc52-4123-82a0-7eeac91f438e.png" width="40%">
<img src="https://user-images.githubusercontent.com/98648558/152689298-e8ea7bdb-a8d3-47ce-a6d8-ded5dce5409d.png" width="40%">
### Language support
Fully supports English, Hebrew, Russian. Layouts support left to right and right to left text direction.
### App structure 
RAM UI has one activity that hosts one of two fragments. All views share one ViewModel. ViewModel implemented as facade for multipale subsystem classes (like DateHandler, Repositary, TaskHandler). Each subsystem class depends on its own interface only. 


