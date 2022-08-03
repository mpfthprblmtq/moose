### Building and Packaging Moose

__Prerequisites__  
*This is the version I've tested with, it may work with more combinations*
* jpackage *(v. 17.0.3)* 


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
                        -srcdir . -srcfiles moose.jar -appclass moose.Moose \
                        -outdir out -v`, which packages the app into the specified package type (.pkg or .dmg) with the specified app version.
6. Cleans up any extra files.
7. Asks you politely if you'd like to run your newly created .pkg or .dmg in the `deploy` folder.