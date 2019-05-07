# README

# term-project

## Overview

Athenia is a supplementary learning tool designed to help users who are studying a foreign languages. The application provides note taking functionality with built in "modules" and tagging system. The modules, including vocabulary, conjugation, general text, and so on, are automatically compiled from all of your notes into easy to study formats. As well, the application provides a review mode, which uses an algorithm that takes into account various factors, such as date last reviewed and difficulty rating, in order to provide the most important notes for you to review. Through Google Drive, the application allows a user's data to be stored between sessions, automatically backing it up to the user's own Google Drive. The application is currently in development, but most of its prominent features are functional and being improved.


## Section Overviews

### Front End (Mia and Jason)

### Data Structures (Makayla)

### Data Storage (Thomas)

#### Overview of Process

The primary challenge of working with the data storage was interaction with the Google Drive API. I have very little experience with API's, and no experience with the authentication process, so a lot of time was spent searching through dozens of tutorials and trying to get a general understanding of the ideas while getting the code to work. This also required a temporary deployment of the project in order to test user authentication on a client machine that was not the server machine, and then also allowing authentication on a localhost for development purposes.

After all of that was resolved, I focused on developing the data parser that converts between the Java object `Athenia`, which stores all of the user's data in program memory, and the user's SQL file. In order to do this, I would have to get the SQL file from the user's Google Drive (if it exists, or else create a new one) to be stored in server memory, and then create a connection to it in the program. After getting this functional, I was able to begin actually writing all of the SQL commands to translate the data. I get into more of the design decisions that made this a bit challenging, but the biggest factor I had to take into account was that our project is designed for extensibility. We ran into a few issues due to our design decisions on the data structures which, given more time, we would be careful to resolve, however the temporary fixes are fully functional, and storage of data into the SQL is working as expected.

The next important step was to actually ensure that the user's database was updating. The initial method (just to get it working) was to require logging out in order for it to update the database. When this was functional, I added the next major component to the code: an updater scheduler. Taking advantage of the `java.util.Timer` class, I created a utility class which allows creation and deletion of auto-updaters for a user. The general format is that, when a user logs in, they get added to the scheduler, and then every 30 seconds, their data gets backed up to the database (even if they close the website). When they log out, the scheduler gets deleted. 

After implementing the basic scheduler, I went on to add in some purging. At the moment, if a user became inactive, we would still be holding a few things in program and server memory: the user's Java data object, the Java file object pointing to the user's database, the database itself, and the scheduler, which would continue updating the user's database file despite inactivity. To resolve this, I added a feature so that, after 5 minutes, all of these were purged (including removing the file from server memory). Then, if the user became active again, the database would be reloaded from Google Drive, and the user's data would be available. Because of how the authentication process stores users, it does not require the user to reauthenticate unless they logout or have a completely new session, which allows these transitions to be seamless.

#### Design Decisions

 - We chose to back up the user's data to a file.
   - This is simply because we wanted a user's data to be preserved if the server had to restart. We don't want all of our user's to lose their data every time their is an update, so by storing it in a file, it will be preserved.

 - We chose to store the user's data on their own Google Drive (inside our AppData folder).
   - Many users are often concerned about their data being private, and so by storing their data on their own Google Drive, the user can be sure that no one else will have access to it besides the application itself.
   - While planning for possible future additions to the application, we realized that one might desire the feature of adding files or images to your notes. If this feature were to be added, it would be a mess to store all of that in a single server storage, so by offloading the storage to the users themselves, they would have their own data limits as set by Google Drive and file organization would be much easier.
   - I wanted the extra challenge of learning authentication. I didn't have any experience with it, and I saw that it could be relevant, so I just wanted to learn and have some experience with it.
   
 - We chose to store the user's data as a SQL file.
   - One feature that we foresaw was the desire to have a version history. One of the hardest parts of implementing the SQL for this project was that, in order to account for this possible desire, we only update the parts of the SQL file that have been changed. What this means is that the user's data file has a history of all of the updates that have been made to it, rather than creating a new SQL file every time the user's data needs to be backed up. While we could have used the JSON format for storing the data (and most likely would have been a lot easier to implement), updating the JSON with only the new updates would be a very difficult and probably unreasonable task.
   - The data seemed to naturally belong to a SQL database. As we were coming up with the initial ideas for this application, I immediately noticed how natural it would be to store the data as a relational database, and how it would allow for rigid structure while still allowing easy extensibility.
 
 - We chose to purge inactive users.
   - For obvious reasons, it is unreasonable to permanently store every instance of a user that we create as well as all of the database files on server memory. Even during development, before this was implemented, there were often over 25 database files just sitting around in server memory at a time that were never being used since the user's session had ended.
   - We decided to make the purge frequency only 5 minutes. While this is a somewhat arbitrary amount of time, the order of magnitude seemed reasonable in order to make it so that memory doesn't get too clogged up (like it might if we allow inactive users to remain for 24 hours), as well as to make sure that we aren't making constant requests to the Google Drive API, which might slow down the server a lot.
   
#### Code interactions
 - GoogleDriveApi
   - `isLoggedIn :: String userId -> boolean`
     - Checks if a given user is logged in.
   - `getUrl :: String state, String redirectUri -> String`
     - Generates a url for the user to be redirected to for authentication.
   - `createCredential :: String userId, String code, String redirectUri -> void`
     - Creates and stores a new credential for the given user after authentication.
   - `getDataBase :: String userId -> java.io.File`
     - Downloads the user's database file and returns a java File pointing to it in memory.
   - `setDataBase :: String userId, java.io.File dataBase -> void`
     - Updates the user's database file on Drive.
   - `deleteDataBaseFile :: String userId -> void`
     - Deletes a user's database file from server memory and removes it from local cache.
 - DatabaseParser
   - `getUser :: String userId -> Athenia`
     - Generates a new Athenia object for the given user.
   - `updateUser :: String userId -> void`
     - Update the user's database with their current Athenia data, and attempts to write it to their Google Drive file.
   - `hasUser :: String userId -> boolean`
     - Checks whether the given user has a locally stored `Athenia`.
   - `deleteUser :: String userId -> void`
     - Deletes user from local memory cache.
 - UpdaterScheduler
   - `create :: String userId -> void`
     - Creates a new scheduler for a user. If the user already has a scheduler, resets the lastUpdate to current time.
   - `delete :: String userId -> void`
     - Deletes a user's scheduler.
 
#### Current bugs
 - Google Drive API interaction
   - Can start causing errors if too many requests are made in a given span of time. For actual deployment, we would have to pay for a higher request rate or else fix the code to prevent the cap from being reached.
 - Database parser
   - A few methods, especially as related to getting and updating tags for a user, are a bit hacky due to the fact that a tag's id is just its name.
   - A few parts (such as user's meta data and rows of a conjugation table) need complete over-writing rather than updating. Specifically, for example, the user's meta data (such as username and email) are over-written no matter what, and if any single row of a conjugation table changes, then all of the rows are rewritten in the database.
   - The design for storing tags doesn't feel fully natural, although this is most likely just because we couldn't figure out fully how the data structures should work with respect to tagging. This is addressed in the data structure section.
 - Updater scheduler
   - No known bugs, although this was a recent development, and has not been tested with multiple users at once.
   
   

# Planning Information

## Who's working on what files?
- Jason
- Makayla
- Mia
  - resources folder files
- Thomas
  - GoogleDriveApi.java

## Project Specification, Mockup, and Design
 - [Specifications (Google Docs)](https://docs.google.com/document/d/11DEoQ-FvDwlQHYFCPXcI013ueQ5YGhWYVdIvYNJGd0E/edit?usp=sharing)
 - [Mockup (Balsamiq Cloud)](https://balsamiq.cloud/s7awshi/pmql4np)
 - [Design Presentation (Google Slides)](https://docs.google.com/presentation/d/1wG1z1jlDUaAITiRI3tXOKnVw3Rln7X5BTkGOY1YhBu8/edit?usp=sharing)

## Application Idea Descriptions

Note: any reference to "app" means a form of web app, not a phone app (although will be structured to allow extensibility into a phone app in the future).

### Language: 
 - Overview: self-note taking app — algorithm during review mode (for each section, pages need for most review)
 - PROS: 
   - cool algorithm — generates which pages to review
   - user-built experience (input supplied by user)
   - interface can be pretty basic
 - CONS: 
   - heavy content-based (guide)
   - languages
   - translation in both directions
   - data for human-based interaction
 - IDEAS: 
   - modularize your notes — entirely for language
     - conjugation modules
     - vocab modules
     - grammar modules
     - pronunciation modules?
   - google translate functionality?
   - one chapter == one page
   - sections: literature, vocabulary, grammar, auditory
   - time function (given a set amount of time, tells how much should spend)
   - machine learning — for user-built experience
   - comparator ranking method to determine which pages to review the most based on time, difficulty, and ability
   - “insert conjugation table” & “insert vocabulary”
   - each section of GUI has distinct aesthetic
   - demo: presented in English (intended for english audience for the demo)
### Hand-Me-Down:
 - Overview: connect students to pass down old or unused items
   - PROS:
     - demo
     - filtering (clothes, furniture, books, stationary)
     - sustainability oriented
     - promoting reuse of different things
   - CONS: 
     - content-based
     - security
     - sleaziness or deceit may be an issue
   - IDEAS: 
     - use google, facebook, id sign-in
     - filtering system - user sustained input
     - search functionality for specialization
     - request system?
     - database heavy (construction & management)
     - directness
     - targeted towards students
     - transaction process: “drop-off,” “pick-up,” “meet in middle”
       - campus-based (local) — central location for drop-off
     - distinct aesthetic
     - look at etsy for inspiration
   - PROCESS:
     - buyer - seller interaction: seller supplies content instead of buyer requests
     - feed of listings as well as feed of requests?
       - complete request, item in feed matches
     - cross-checking between feeds (independence between the two, but if there is a match — people are notified?)
     - handling multiple things from the same person?
       - exchange object listing what is being exchanged
       - purchase page (receipt)
     - post-transaction page (confirmation page)
### Anxiety: 
 - Overview: organized system (wikipedia?) — simple to use, user-contribution — authentication by those with authority, detailed / summarized explanations
   - PROS: 
     - people add own data
     - crowd-sourced
     - reward system for exploration and growth
       - Reward system also serves to show developers data on what’s working and what isn’t
       - sharing with friends
   - CONS: 
     - content-based (guide)
     - setting a level-of-quality
     - incentivization
     - marketing towards a very focused and specific audience
   - IDEAS: 
     - game (fidget game)
     - report functionality — survey-esque
       - flagging — cw’s
     - generic & specific
       - how to go to a specific restaurant and how to go to that specific restaurant in this region
     - ensure diversity in data
     - moderators — how is this defined though?
       - common pages: determined based by contributions
       - demo maybe unnecessary
     - forum aspect — social and interactive (meet friends with anxiety)
     - allowed sign-in but can opt out (allow anonymous sign-in)
       - sign-up with google: but hide information
       - allow for anonymity but not make it forced
       - wholesome avatars
     - reporting systems
   - USE-CASES:
     - trains, planes, and automobiles
     - neuro-divergence and other sorts of mental illnesses
       - directed towards guides instead of anxiety itself


## Strengths and Weaknesses

- Jason
 - Strengths
   - Commenting
   - I really like GUIs :) — front end?
   - Human-interaction?
   - Note-taking
 - Weaknesses
   - Screams a lot
   - Goes to bed late
   - Algorithms
   - Generics
   - Time / space complexities
   - Servers & databases
   - Back-end
- Mia
 - Strengths
   - Documentation
   - Front end
   - Organization
   - Data Structures
   - Nice
 - Weaknesses
   - Spelling
   - Goes to bed early
   - Nice
   - Servers
   - Back end
   - Anal
- Thomas
 - Strengths
   - Algorithms
   - Data structures
   - Code organization
   - Backend design
   - Design consistency
 - Weaknesses
   - Visual design
   - Human interaction
   - Leading discussion
   - Making decisions
   - Hardware
   - Probably too anal
   - Spelling
- Makayla
 - Strengths
   - Product Management
   - Design
   - Data Structures
   - Group Organization
 - Weaknesses
   - Backend 
   - UI Coding (enjoy but don’t have a ton of experience)

## TODO
### Deadlines
 - [x] Project Outline (March 4)
   - [x] List out 3 project ideas and descriptions
     - [x] Generate ideas
     - [x] Generate descriptions
   - [x] List out individual strengths and weaknesses
 - [ ] Project Specifications, Mockup, and Design (March 15)
   - [ ] Specifications
   - [ ] Mockup
   - [ ] Design Presentation
### Meetings
 - [x] Project Outline Meeting (March 3)
   - [x] Determine project ideas
   - [x] Discuss strengths and weaknesses
   - [x] Plan for future meetings
   - [x] Finalize details about meeting
 - [x] Project Specifications, Mockup, and Design Meeting 1 (March 11)
   - [x] Discuss design choices
   - [ ] ~~Decide on project descriptions~~
   - [ ] ~~Delegate labor~~
   - [x] Finalize details about meeting
 - [ ] Project Specifications, Mockup, and Design Meeting 1.5 (March 13)
   - [ ] Go over individual designs
   - [ ] Deside on group design
   - [ ] Finalize design
   - [ ] Draft specifications
 - [ ] Project Specifications, Mockup, and Design Meeting 2 (March 14)
   - [ ] Finalize design choices
   - [ ] Submit for March 15 deadline
### Other
 - [ ] Fill out TODO!
### Individual
 - Jason
   - [x] Be awesome!
 - Mia
   - [x] Be awesome!
 - Thomas
   - [x] Be awesome!
 - Makayla
   - [x] Be awesome!
   
   
   
### UPDATE -- May 5, 2019
(pre-demo day)
project features some bugs - specifically in frontend display
We used Google API for database management and gradle for back-back end management
Sign-in is through Google account
conjugation was mostly cut as a portion due to complications in display, but is established in backend
freenotes and vocab are set up as pages in frontend but "view notes" is buggy
