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
