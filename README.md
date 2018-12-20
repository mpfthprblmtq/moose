# Moose
A music management system capable of reading and modifying dynamic id3 tags.

![Generic badge](https://img.shields.io/badge/version-1.1.3-brightgreen.svg)
[![GitHub](https://img.shields.io/github/license/mashape/apistatus.svg)]()

[View project on Github](https://www.github.com/mpfthprblmtq/moose)

[View Backlog (Pivotal Tracker)](https://www.pivotaltracker.com/n/projects/2194861)

[Download pkg file (OSX)](https://www.prblmtq.com/projects/moose/download)

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

### 1.1.3 (The Tracks update)

**Release Date:** Upcoming

- Macro for just adding track numbers and disk numbers
- Fix for sorting tracks (1/10, 2/10, ... ,10/10 instead of 1/10, 10/10, 2/10, ...)
- Manual track sorting, which will also set the filenames' numbers
- Made a shortcut to google images for when the cover art isn't found
- Probably will separate this changelog into a separate file, we'll see how it goes

### 1.1.2 (The "Random features" update)

**Release Date:** 20 December 2018

- Improved console logging on front end
- Added a "More Info" panel
- Cover art search now checks the parent file if the album is a multi-disk album
- Made it smart
  - Ability to determine certain id3tags based on the filename and file location alone
- Bugfixes:
  - Random VectorEnumerationExceptions are no longer a thing
  - GUI doesn't skew anymore when adding a long-titled file into the UI
  - Tracknum and Disknum doesn't get set to "/0" when there's nothing in those fields
  - Fixed find and replace functionality not setting anything except the first row in the table
- Entered moose in a Eukanuba dog/moose show
  - He was the only moose :(
  - He placed 2nd for training but 1st for lovability

### 1.1.1 (The Refactoring update and/or the "All 1's update" and/or the Cleanup update)

**Release Date:** 3 December 2018

- Refactoring classes to match an MVC framework (Model View Controller)
- General cleanup
  - Update README
  - Package project into smaller packages (views, controllers, utils, etc.)
- Tamed moose
  - Taught it sit, stay, roll over, and attack

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
- Capturing moose, it got out of its pen and bit people
  - A Møøse once bit my sister... No realli!

### 1.0.2 (The Settings update)

**Release Date:** 19 September 2018

- Settings menu
  - Log Files interaction ability
  - Setting library location
  - Genres list (functionality slated for future release)
- Better logging on front end
- Minor code improvements
- Gave moose legs to walk with
  -  Frolicking optional

## 1.0.1

**Release Date:** 3 September 2018

- Added logging functionality to log errors and events to Application support folder
  - Created custom Logger class
- Added antlers to moose


## 1.0.0

**Release Date:** 1 September 2018

- Initial release, basic functionality included.
  - Ability to read/write id3tags on mp3files
  - Some automatic functionalities (auto add cover art, sorting)
  - One god-class (will definitely reformat this in future release)
- Created moose
