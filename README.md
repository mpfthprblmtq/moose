# Moose
A music management system capable of reading and modifying dynamic id3 tags.

![Generic badge](https://img.shields.io/badge/version-1.1.3-brightgreen.svg)
[![GitHub](https://img.shields.io/github/license/mashape/apistatus.svg)]()

[View project on Github](https://www.github.com/mpfthprblmtq/moose)

[View Backlog (Github Project)](https://github.com/mpfthprblmtq/moose/projects/1)

[Download pkg file (OSX)](https://www.prblmtq.com/projects/moose/download)

Compatible with:
* OS X (Mac)
* ~~Windows~~ *(Coming soon to a future release near you)*

---

### Building and Packaging Moose

__Prerequisites__ *These are the versions I've tested with, it may work with more combinations*
* Apache Ant *(v. 1.10.8)*
* JavaPackager *(v. 10.0.2) (Comes with Java 10.0.2)* 


Run the `terminal-deploy.bash` file.  That's it.  There were more steps, like building the app into a single .jar, then 
moving some files around, then running a javapackager command, but I used my big brain noggin and put it all into one nice script.

The script does the following:
1. Displays a super cool ASCII art of a moose that I totally created myself (Totally).
2. Prints the current versions of the tools it uses.
3. Asks you if this deployment is a new version. ( [Y/N] )
    - If it is a new version, will display the current version of the app (sourced from `SettingsController.java`)
    - If it isn't a new version, skips any versioning
4. Runs the command `ant -f $APP_PATH package-for-deploy` which runs the package-for-deploy target in the build.xml file.  (`$APP_PATH` your project folder's path)
5. Runs the command `javapackager -deploy -native $packageType -name Moose \
                        -BappVersion=1.1.3 -Bicon=package/macosx/moose.icns \
                        -srcdir . -srcfiles moose.jar -appclass moose.Main \
                        -outdir out -v`, which packages the app into the specified package type (.pkg or .dmg) with the specified app version.
6. Cleans up any extra files.
7. Asks you politely if you'd like to run your newly created .pkg or .dmg in the `deploy` folder.

---

## Running the project

To install Moose, run the installer file you created in the previous section.  Once installed, you should be able to run the app.

[Or you could just download a fully working .dmg file from my website.](https://www.prblmtq.com/projects/moose/download)

*(I don't have a developer key yet to sign the app, so you'll just have to trust me for now)*

### Configuring the album art finder

I have some functionality built in to automatically use Google's CSE (Custom Search Engine) and Google's search API to search for images.  You will need to configure this in a few steps:
1.  Go to https://cse.google.com and create a new Custom Search Engine.  Name it whatever you want, like "Moose" for example.  Under "Sites to Search," just put www.google.com.
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