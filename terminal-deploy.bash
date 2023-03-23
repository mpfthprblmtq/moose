echo
echo "88,dPYba,,adPYba,   ,adPPYba,   ,adPPYba,  ,adPPYba,  ,adPPYba,            "
echo "88P'   \"88\"    \"8a a8\"     \"8a a8\"     \"8a I8[    \"\" a8P_____88   "
echo "88      88      88 8b       d8 8b       d8  \`\"Y8ba,  8PP\"\"\"\"\"\"\"   "
echo "88      88      88 \"8a,   ,a8\" \"8a,   ,a8\" aa    ]8I \"8b,   ,aa       "
echo "88      88      88  \`\"YbbdP\"'   \`\"YbbdP\"'  \`\"YbbdP\"'  \`\"Ybbd8\"'"
echo
sleep 2

# go to where the bash file was executed from (should be the main project directory)
APP_PATH="`dirname \"$0\"`"
cd $APP_PATH || exit

###################################################################################################
#   Print out the versions of the building tools to use
###################################################################################################
echo "###############################################################"
echo "# BUILD TOOL VERSIONS                                         #"
echo "###############################################################"
echo
echo "Java Version:"
java --version
echo
echo "Javapackager Version:"
jdk="/Users/mpfthprblmtq/Library/Java/JavaVirtualMachines/corretto-17.0.3/Contents/Home/bin"
"$jdk"/jpackage --version
echo
echo

###################################################################################################
#   Print out the version of the app we're publishing (found in the source code)
###################################################################################################
echo "###############################################################"
echo "# APP VERSION                                                 #"
echo "###############################################################"
echo
# get the version using a grep from SettingsController.java
version=$(grep -Eoi '[0-9]+\.[0-9]+\.[0-9]+' src/main/java/com/mpfthprblmtq/moose/controllers/SettingsController.java)
echo "New version number:   ${version}"
echo
echo

###################################################################################################
#   Figuring out which type of installer to make
###################################################################################################
echo "###############################################################"
echo "# PACKAGING PROJECT                                           #"
echo "###############################################################"
echo
echo "How would like the app packaged?"
echo "   (1): .pkg"
echo "   (2): .dmg"
echo
printf " [1/2] : "
read -r packageType
if [[ $packageType == "1" ]]
then
  packageType="pkg"
elif [[ $packageType == "2" ]]
then
  packageType="dmg"
else
  echo
  echo "Unknown response, exiting..."
  exit
fi
echo
echo "Packaging the application into .$packageType file..."
echo
sleep 2

###################################################################################################
#   Moving some resources around
###################################################################################################
# start from scratch
rm -rf deploy
mkdir deploy
# grab the jar from the build folder
printf "Copying jar file to deploy folder..."
cp "$APP_PATH"/out/artifacts/moose_jar/moose.jar "$APP_PATH"/deploy
cd "$APP_PATH"/deploy || exit
echo "Done."
# create resource directory for jpackage
printf "Creating resource directory..."
mkdir resources
cp "$APP_PATH"/src/main/resources/img/build-resources/moose-app-icon.icns "$APP_PATH"/deploy/resources/moose-app-icon.icns
cp "$APP_PATH"/src/main/resources/img/build-resources/moose180-spaced.png "$APP_PATH"/deploy/resources/moose-background.png
cp "$APP_PATH"/src/main/resources/img/build-resources/moose180-spaced.png "$APP_PATH"/deploy/resources/moose-background-darkAqua.png
cp "$APP_PATH"/src/main/resources/img/build-resources/moose-volume.icns "$APP_PATH"/deploy/resources/moose-volume.icns
echo "Done."
# create file association properties files
printf "Creating file extension properties files..."
mkdir properties
echo "mime-type=audio/mpeg\nextension=mp3\ndescription=MP3 Files" > properties/FAmp3.properties
echo "arguments=fileToOpen" > properties/FAmp3Launcher.properties
echo "Done."

###################################################################################################
#   Actually building the package
###################################################################################################
printf "Building installation package..."
$jdk/jpackage \
  --resource-dir "$APP_PATH"/deploy/resources \
  --type "$packageType" \
  --input . \
  --main-jar moose.jar \
  --name Moose \
  --app-version "$version" \
  --icon resources/moose-app-icon.icns \
  --description "Moose" \
  --vendor "PRBLMTQ" \
  --copyright "Copyright 2018-2023 PRBLMTQ - Pat Ripley" \
  --file-associations properties/FAmp3.properties \
  --add-launcher MP3Launcher=properties/FAmp3Launcher.properties \
  --verbose
echo "Done."
sleep 1
echo
# verify app built
installer=$APP_PATH/deploy/Moose-"$version".$packageType
if [ -f "$installer" ]
then
  cp Moose-*.$packageType moose-"$version"-installer.$packageType
  echo "Done with .$packageType!"
else
  echo
  echo "Error while building app!"
  echo
  exit
fi
echo
echo

###################################################################################################
#   Clean up
###################################################################################################
echo "###############################################################"
echo "# CLEANING UP                                                 #"
echo "###############################################################"
echo
printf "Cleaning up..."
rm moose.jar
rm Moose-"$version"."$packageType"
rm -rf resources
rm -rf properties
echo "Done."
echo
echo
echo "###############################################################"
echo "# PROJECT BUILT AND PACKAGED SUCCESSFULLY                     #"
echo "###############################################################"
echo
ls -l

###################################################################################################
#   Open the installer file from here if we want
###################################################################################################
echo
printf "Would you like to open the .%s file? [Y/N]: " $packageType
read -r openFile
if [[ $openFile == "y" || $openFile == "Y" ]]
then
  open moose-"$version"-installer.$packageType
fi
echo "      _/\_       __/\__                    "
echo "      ) . (_    _) .' (                    "
echo "      \`) '.(   ) .'  (\`                  "
echo "       \`-._\(_ )/__(~\`                   "
echo "           (ovo)-.__.--._                  "
echo "           )             \`-.______        "
echo "  thanks  /                       \`---._  "
echo "    :)   ( ,/ /)                        \  "
echo "          \`''\/-.                        |"
echo "                 \                       | "
echo "                 |                       | "
echo