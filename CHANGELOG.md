# Changelog

### 1.2.1 (The Navigation and Code Cleanup update)

**Release Date:** Upcoming

- Better keyboard navigation
- Code cleanup (Lint, standards, etc.)
- Maybe a feature or two?  Not sure yet.

### 1.2.0 (The "Holy moly there's new functionality" update)

**Release Date:** 27 September 2020

- Autocomplete on the Artist, Album, Album Artist, Year, and Genre fields
    - The Genre autocomplete feature sources data from the built-in genre list (located in Settings)
- Album Art Finder service
    - Uses Google's Custom Search Engine (CSE) to run a google image search based on a query provided by the user
    - Uses a CSE Id and API Key (provided by user in Settings) to generate a url and make a rest call
    - User can then choose an image to use as the cover image
    - *Note: This search is only capable of being run 100 times/day.  Each search in Moose runs 4 api calls, so a total of 25 api calls/day.  This might change later.*
- Made the Settings menu more reactive
    - Edited fields become bolded/green
    - On the "Save Settings" button being pressed, user gets instant feedback and it resets the gui
- Some smaller bug fixes/improvements

### 1.1.3 (The Random bugfixes/features update)

**Release Date:** 1 March 2019

- Macro for just adding just track numbers and disk numbers
- Autotagging now works for EPs part of a label
- Added a button next to the Save All button to clear the whole table
- The Find + Replace dialog now autofocuses on the find field
- Bugfixes: 
    - Fixed autotagging not setting the right track info, that was a mess
    - Fixed the issue where, when autotagging, the cell currently in focus won't update
- Separated this changelog into a separate file
- Sharpened antlers for battle
    - THE FIGHT FOR CANADIA WILL BE GLORIOUS
    - HAIL MOOSE

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
- Capturing moose, it got out of its pen and bit people - A Møøse once bit my sister... No realli!

### 1.0.2 (The Settings update)

**Release Date:** 19 September 2018

- Settings menu
    - Log Files interaction ability
    - Setting library location
    - Genres list (functionality slated for future release)
- Better logging on front end
- Minor code improvements
- Gave moose legs to walk with
    - Frolicking optional

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