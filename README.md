# Moose
A music management system capable of reading and modifying dynamic id3 tags.

Current version:  1.0.1  

[View project on Github](https://www.github.com/mpfthprblmtq/moose)

[View Backlog (Pivotal Tracker)](https://www.pivotaltracker.com/n/projects/2194861)

---

## Building the project

Compile the source code using the "package-for-deploy" target in build.xml.  This project was built in NetBeans (don't judge), so it's much easier to build the target by just right clicking on build.xml in the "Files" tab and selecting Run Target.

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
