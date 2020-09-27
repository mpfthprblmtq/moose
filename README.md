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

Compile the source code using the "package-for-deploy" target in build.xml.  This project was built in NetBeans (because who likes to hardcode Swing GUIs), so it's much easier to build the target by creating a project in NetBeans and just right clicking on build.xml in the "Files" tab, selecting Run Target -> Other Targets -> package-for-deploy.

If you're building from command line with Apache ant, go to the project folder, and the command is `ant -f / package-for-deploy`

Building the project will create an executable jar in the deploy folder.

(For the moment, I'll keep a current .jar file updated in the `deploy` folder)

## Packaging the project (Into native MacOSX app)

Once built, you can run the terminal-commands file to package the app into a .pkg file, which can be used to install the app.

*Note: I use JavaPackager 10.0.2, I know it has issues with other versions*

---

## Running the project

To install Moose, run the .pkg you created in the previous section.  Once installed, you should be able to run the app.

*(I don't have a developer key yet to sign the app, but you'll just have to trust me for now)*

### Configuring the album art finder

I have some functionality built in to automatically use Google's CSE (Custom Search Engine) and Google's search API to search for images.  You will need to configure this in a few steps:
1.  Go to cse.google.com and create a new Custom Search Engine.  Name it whatever you want, like "Moose" for example.  Under "Sites to Search," just put www.google.com.
2.  On the next screen, click on the Control Panel button on the "Modify your search engine" field.
3.  You can give a description for your search engine here, as well as provide key words to search by.  I chose "spotify" and "open.spotify" since I want to prioritize spotify's images over others.
4.  Flip the "Image search" slider to ON.
5.  Take note of the Search Engine ID field, we'll need that later.

Now we need to create an API key so your searches are authenticated with Google.
1.  Go to https://console.developers.google.com/apis/credentials and click "+ Create Credentials" near the top.  This will generate a new API key.
2.  Name it whatever you want, like "Moose" for example.
3.  Under API Restrictions, select the "Restrict Key" radio button.  Under the new dropdown, choose "Custom Search API" and hit Save.
4.  Take note of your new API key, we'll need that later.

Now that we have both the CSE ID and your API key, we can go into Moose and set it up!
1.  Open Moose.
2.  Open the Settings window (CMD + ,)
3.  Go to the API Config tab.
4.  Put in your CSE ID and API key in the fields and hit Save.

You're ready to use Moose's built in album art finder!