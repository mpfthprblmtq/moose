echo "                        /\`."
echo "                   /|   |  \`."
echo "             |\   | |   /    '"
echo "             |'.  | '.-'      |"
echo "             | \  |          .'"
echo "              \ \.'         .'"
echo "              '.          .'"
echo "   /\/\`.       \        .'"
echo "   \ \`. \      |     _.'"
echo "    \  \`.\    .'    .'"
echo "     \`-. \`..-'    .'/(        _.--_"
echo "        \`---\`-.  / / /-.__.--'     \`."
echo "            /  _  |                  \`._"
echo "          .'  /O\\"
echo "        .'    \`-'"
echo "      .'"
echo "     :_   -:      \\"
echo "     |/   / :_     ||'."
echo "     '._.'_/  \`.    |  )"
echo "               '|  /    \\"
echo "                | |     |"
echo "         MOOSE  |||     |"
echo "                 \|"
sleep 3

###################################################################################################
#   Print out the versions of the building tools to use
###################################################################################################
echo "##################################################"
echo "# BUILD TOOL VERSIONS                            #"
echo "##################################################"
echo
echo "Ant Version:"
ant -v
echo
echo "Java Version:"
java -version
echo
echo "Javapackager Version:"
jdk=$(/usr/libexec/java_home)
"$jdk"/bin/javapackager -version
echo

###################################################################################################
#   Set up version number
###################################################################################################
echo "##################################################"
echo "# VERSION CONFIGURATION                          #"
echo "##################################################"
echo
# go to where the bash file was executed from (should be the main project directory)
APP_PATH="`dirname \"$0\"`"
cd $APP_PATH || exit

# get the old version using grep from Settings.java
oldVersion=$(grep -Eoi '[0-9]+.[0-9]+.[0-9]+' src/moose/controllers/SettingsController.java)
echo "Current version number:   ${oldVersion}"
echo

# determine if it's a new version
printf "Is this a new version? [Y/N]: "
read -r changeVersion

if [ "${changeVersion}" == "y" ] || [ "${changeVersion}" == "Y" ]
then
  # get user input for new version
  printf "Enter new version number: "
  read -r newVersion
  echo
  # put in new version in source code for hardened version
  cd src/moose/controllers/ || exit
  sed -i '' "s/final String version = \"${oldVersion}\";/final String version = \"${newVersion}\";/g" SettingsController.java

  # verify new version in Settings.java
  verifyVersion=$(grep -Eoi '[0-9]+.[0-9]+.[0-9]+' SettingsController.java)
  if [ "$verifyVersion" != "$newVersion" ]
  then
    echo "Error when setting new version in source code!"
    exit
  fi
  appVersion=$newVersion
  echo "Put the new version in source code!"
  cd "$APP_PATH" || exit
else
  appVersion=$oldVersion
fi
echo
echo

###################################################################################################
#   Building the app
###################################################################################################
echo "##################################################"
echo "# PROJECT BUILD TO SINGLE JAR                    #"
echo "##################################################"
echo
echo "Building the app into single jar..."
sleep 3
echo
pwd
ant -f "$APP_PATH" package-for-deploy

# verify app built
builtJar=$APP_PATH/deploy/moose.jar
if [ -f "$builtJar" ]
then
  echo "$builtJar exists!"
else
  echo "Error while building app!"
  exit
fi
echo "Done building app!"
echo
echo

###################################################################################################
#   Packaging the app
###################################################################################################
echo "##################################################"
echo "# PACKAGING PROJECT                              #"
echo "##################################################"
echo
echo "How would like the app packaged?"
echo "   (1): .pkg"
echo "   (2): .dmg"
echo
printf " [1/2] : "
read -r packageType
if [ "$packageType" == "1" ]
then
  packageType="pkg"
elif [ "$packageType" == "2" ]
then
  packageType="dmg"
else
  echo
  echo "Unknown response, exiting..."
  exit
fi
echo
echo "Packaging the application into .$packageType file..."
sleep 3
echo
printf "Copying app icon to deploy folder..."
cp "$APP_PATH"/src/resources/moose.icns "$APP_PATH"/deploy
cd "$APP_PATH"/deploy || exit
echo "Done."
mkdir -p package/macosx
cp moose.icns package/macosx
$jdk/bin/javapackager -deploy -native $packageType -name Moose \
   -BappVersion=$appVersion -Bicon=package/macosx/moose.icns \
   -srcdir . -srcfiles moose.jar -appclass moose.Moose \
   -outdir out -v
cp out/Moose-*.$packageType moose-"$appVersion"-installer.$packageType
ls -l
echo
echo "Done with .$packageType!"
echo
echo
###################################################################################################
#   Clean up
###################################################################################################
echo "##################################################"
echo "# CLEANING UP                                    #"
echo "##################################################"
echo
echo "Cleaning up..."
rm moose.icns
rm moose.jar
rm -rf out
rm -rf package
cd ..
rm -rf dist
echo
echo "Done!"
echo
echo "##################################################"
echo "# PROJECT BUILT AND PACKAGED SUCCESSFULLY        #"
echo "##################################################"

echo
printf "Would you like to open the .%s file? [Y/N]: " $packageType
read -r openFile
if [ "$openFile" == "y" ] || [ "$openFile" == "Y" ]
then
  cd "$APP_PATH"/deploy || exit
  open moose-"$appVersion"-installer.$packageType
fi


