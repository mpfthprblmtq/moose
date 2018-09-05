# Moose
A music management system capable of reading and modifying dynamic id3 tags.

Current version:  1.0.1  

[View project on Github](https://www.github.com/mpfthprblmtq/moose)

[View Backlog (Pivotal Tracker)](https://www.pivotaltracker.com/n/projects/2194861)

---

## Building the project

Compile the source code using the "package-for-deploy" target in build.xml.  This project was built in NetBeans (don't judge), so it's much easier to build the target by creating a project in NetBeans and just right clicking on build.xml in the "Files" tab, selecting Run Target -> Other Targets -> package-for-deploy.

If you're building from command line with Apache ant, go to the project folder, and the command is `ant -f / package-for-deploy`

Building the project will create an executable jar in the deploy folder.

(For the moment, I'll keep a current .jar file updated in the `deploy` folder)

## Packaging the project (Into native MacOSX app)

Once built, you can run the terminal-commands file to package the app into a .pkg file, which can be used to install the app.

---

## Changelog

### 1.0.2 or 1.1.0

**Release Date:** UPCOMING

- Settings menu
  - Minor code improvements

## 1.0.1

**Release Date:** 3 September 2018

- Added logging functionality to log errors and events to Application support folder
  - Created custom Logger class


## 1.0.0

**Release Date:** 1 September 2018

- Initial release, basic functionality included.
  - Ability to read/write id3tags on mp3files
  - Some automatic functionalities (auto add cover art, sorting)
  - One god-class (will definitely reformat this in future release)
