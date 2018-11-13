# Moose
A music management system capable of reading and modifying dynamic id3 tags.

Current version:  1.0.1  

[View project on Github](https://www.github.com/mpfthprblmtq/moose)

[View Backlog (Pivotal Tracker)](https://www.pivotaltracker.com/n/projects/2194861)

[Download pkg file (OSX)](www.something.com)

Compatible with:
* OS X (Mac)
* Windows *(Coming soon to a future release near you)*

---

### Building the project

Compile the source code using the "package-for-deploy" target in build.xml.  This project was built in NetBeans (don't judge), so it's much easier to build the target by creating a project in NetBeans and just right clicking on build.xml in the "Files" tab, selecting Run Target -> Other Targets -> package-for-deploy.

If you're building from command line with Apache ant, go to the project folder, and the command is `ant -f / package-for-deploy`

Building the project will create an executable jar in the deploy folder.

(For the moment, I'll keep a current .jar file updated in the `deploy` folder)

### Packaging the project (Into native MacOSX app)

Once built, you can run the terminal-commands file to package the app into a .pkg file, which can be used to install the app.

---

## Changelog

### 1.1.1 (The Refactoring update and/or the "All 1's update" and/or the Cleanup update)

**Release Date:** UPCOMING

- Refactoring classes to match an MVC framework (Model View Controller)
- General cleanup

### 1.1.0 (The Auditing update)

**Release Date:** 11 November 2018

- Added Auditing functionality
  - Ability to select a master folder, then analyze all its subfolders
  - Checks for the following:
    1. All filenames match the standard `## TITLE`
    2. The folder has a specific cover art file for the album
    3. All files have pertaining id3tag information (title, artist, album, etc.)
- Added Cleanup functionality
  - Ability to select a master folder, then looks for all "unnecessary" files
  - Checks for the following:
    1. Any image file that's not the cover file
    2. Garbage Windows files (*Thumbs.db*, *folder.jpg*, etc.)
    3. Any audio file not an .mp3 (*.wav*, *.mp3.asd*, *.flac*, etc.)
    4. Any other file

### 1.0.2 (The Settings update)

**Release Date:** 19 September 2018

- Settings menu
  - Log Files interaction ability
  - Setting library location
  - Genres list (functionality slated for future release)
- Better logging on front end
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
