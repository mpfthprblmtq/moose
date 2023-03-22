# Changelog

## LATEST: 1.4.0 (The "Refactor + A Few Feature Improvements" Update)

**Release Date:** 23 March 2023 🍰

### Summary:

Probably the longest time between updates.  This update is mostly a refactor, since the code I wrote 5 years ago was mostly garbage. Now it's less garbage.  Here's what changed:

### Changelog:

**Features:**
- Added feature enabling in settings, where you can choose what features you want to disable/enable
- Made the Autotagging smarter for genres.  Now, when you autotag a track without a genre, Moose will try to infer the genre based on the genres of other songs by the same artist.  If it infers a genre for a track, the genre will be preceded with an info sign (ⓘ) with a tooltip that explains why it's there
- Added a loading icon on the main UI and Audit/Cleanup modal so you know it's doing stuff

**Bug Fixes:**
- Fixed the track number and disk number sorting, now instead of sorting like 1/12, 11/12, 12/12, 2/12, etc, it will sort as it should, 1/12, 2/12, 3/12, etc.
- Fixed an AlbumArtFinder bug where when you search for artists, sometimes the artist doesn't have a profile image, which results in weird UI behavior.  Figured I would just ignore those, since any artist I would be searching for would have an image.
- Fixed a couple random NullPointerException cases that would happen because of changes to the UI outside of the Event Dispatch Thread

**Code Enhancements:**
- Pretty much everything, lots of code cleanup, refactoring, reformatting, reformatting, redoing, rethinking my life choices

**The Moose kingdom has some unwelcome guests:**
- As the Elk battalion approaches the great Moose kingdom, the Moose realize one key flaw to any Elk strategy: the difference in antlers.
- While Elk antlers are more sharp and pointy for stabby stabby, Moose antlers are wider and rounder for slappy slappy.
- The Moose kingdom must use this information to their advantage if victory is expected on the battlefield.

---

## 1.3.0 (The "Hey Look, Even More Features and Better Autotagging" Update)

**Release Date:** 25 April 2021

### Summary:

Per the usual pattern, I decided to take a break from my other project to focus on yet another project.  So here's another release, which will probably be followed by another indeterminate amount of time until I decide to work on it again.  Here's what changed:

### Changelog:

**Features:**
- Filename Formatter
  - A new part of autotagging, Moose will attempt to automatically name the files based on the pattern "02 Play Pretend (ft. Ourchives)"
  - If the filename doesn't match that pattern, it will attempt to match it to that pattern using regex and any tag information that may exist on that file already.  For instance, if the filename is something like "01. Kasbo - Play Pretend (feat. Ourchives)", it will parse that down to the expected filename, which in this case would be "01 Play Pretend (ft. Ourchives)."  Note how it removed any unnecessary punctuation and changed "feat." to "ft."
  - If Moose is unable to determine the track number from the filename, a dialog will appear that will ask you to manually set the track number.
- Better editing/saving behavior
  - Before, if you renamed the file or changed the cover art, Moose would enact those changes immediately without the user hitting the save button.  This functionality changed to match everything else, where the user has to press save in order for any change to take place on that song.
- Smarter Audit behavior
  - When running an audit, you can just audit only the marked albums instead of being forced to go through all of them
- Added a "Show in Finder" context menu item
  - Opens where that song is in a finder window
- Renamed `Main.java` to `Moose.java`
  - Now it shows as "Moose" in the top bar rather than "Main," which didn't make much sense
- Multiple album artwork shows all album arts on the "Multiple fields panel"
  - If you select multiple rows on the table with differing album artworks, the multiple fields panel will show a conglomerate of those multiple album artworks
- Better keyboard navigability
  - CMD + A will now select all the rows in the table
- Option to remove comments when you autotag
  - Self explanatory, removes the comment from the song if you want it to
- Opening the More Info modal for multiple songs
  - Also self explanatory, now you can open a more info modal on multiple songs, and edit them like normal

**Bug Fixes:**
- Cover art wouldn't automatically populate in the case of an album having multiple disks
- If a file imported into Moose had a '/' character in it, Moose would replace it with a ':' (Darn you OSX)
- When you opened the More Info modal and made a change on the track or disk numbers, Moose would append a '/x' on the number
- Context menu cleanup

**Code Enhancements:**
- Reformatted a lot of the app, mostly to improve the UI <-> Controller <-> Service pattern
- Broke some of the major monolith files into some utility functions to clean up a bit

**The Moose kingdom has some unwelcome guests:**
- With the time of prosper after the great moose war, a new challenger approaches:  The Elk.
- The Moose kingdom must ready its defenses after seeing such a bountiful time of peace.

---

## 1.2.1 (The "Too many Bugfixes and Documentation" Update) (AKA The Halloween Update)

**Release Date:** 31 October 2020 🎃

**Features:**
- Improved the Album Art Finder Frame visuals
    - Added a progress bar so that you can see how long the search will take
    - Throw an error and display to the user if you try to search with no API key or CSE id set in your settings
    - Disabled the search button while a search is being performed so you can't spam click it
- Improved the Audit Frame visuals
    - Added a progress bar so that you can see the progress of the library scan for audit and cleanup
- Improved functionality of the More Info Frame
    - Made it save the currently editing track when using the right and left arrow buttons
- Improved the process for opening tracks in the table
    - Hidden files and folders don't show up in the file count in the status log
- Improved the album art auto add process
    - If you have a single image file in a directory, it'll rename that image file to cover.* and use that as the cover art
- Improved logistics of the main interface
    - When you hit "Clear All," it'll ask you if you're sure, including a "don't ask me again" checkbox
    - Disabled auto actions and other things if there's no tracks in the table
- Improved functionality of the Settings Frame
    - Even more reactability, and fixed some broken reactability
    - When you hit "Restore Defaults," it'll ask you which sections you want to default so you don't do them all each time

**Bug Fixes:**
- SO MANY BUGFIXES
  - When you submitted a new genre change through the Multiple Editor genre field, it would ask you if you want to add that genre to your list many times
  - You could pull in the same songs/albums multiple times, fixed it so there's no duplicate files in the table at any time
  - Changed verbage of "Preferences" to "Settings" in the menu bar
  - File Choosers now open to the library location by default if it's set, else it just opens your "user.home"
  - When you auto add track numbers, the Multiple Track Editor now updates to reflect that
  - The "Add Artwork" and "Remove Artwork" are at the top of the pop up menu when you click on the album art in the table
  - So many more tiny things that I couldn't possibly list out

**Code Enhancements:**
- Ran Lint analysis and fixed the vast majority of things it was complaining about
- Broke up the monolith Utils file into respective, use-case based Utils files

**The Moose kingdom is prospering:**
- With the success in the great war, the kingdom has seen a new age of flourishing wealth and happiness
- This can only mean one thing...

---

## 1.2.0 (The "Holy moly there's new functionality" update)

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

---

## 1.1.3 (The Random bugfixes/features update)

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

---

## 1.1.2 (The "Random features" update)

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

---

## 1.1.1 (The Refactoring update and/or the "All 1's update" and/or the Cleanup update)

**Release Date:** 3 December 2018

- Refactoring classes to match an MVC framework (Model View Controller)
- General cleanup   
    - Update README
    - Package project into smaller packages (views, controllers, utils, etc.)
- Tamed moose
    - Taught it sit, stay, roll over, and attack

---

## 1.1.0 (The Auditing update)

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

---

## 1.0.2 (The Settings update)

**Release Date:** 19 September 2018

- Settings menu
    - Log Files interaction ability
    - Setting library location
    - Genres list (functionality slated for future release)
- Better logging on front end
- Minor code improvements
- Gave moose legs to walk with
    - Frolicking optional

---

## 1.0.1

**Release Date:** 3 September 2018

- Added logging functionality to log errors and events to Application support folder
    - Created custom Logger class
- Added antlers to moose

---

## 1.0.0

**Release Date:** 1 September 2018

- Initial release, basic functionality included.
    - Ability to read/write id3tags on mp3files
    - Some automatic functionalities (auto add cover art, sorting)
    - One god-class (will definitely reformat this in future release)
- Created moose