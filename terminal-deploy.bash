echo "                                                                                "
echo "                                   &&&&& &&&&&&&&&&             /&&&&*&&&&&&&&&&"
echo "                                   &&&&&&&&&&&&&&&&             &&&&&&&&&&&&&&&&"
echo "                                   &&&&&&&&&&&&&&&&             &&&&&&&&&&&&&&&&"
echo "                                   &&&&&&&&&&&&&&&&             &&&&&&&&&&&&&&&&"
echo "                                   &&&&&&&&&&&&&&&&             &&&&&&&&&&&&&&&&"
echo "                                   &&&&&&&&&&&&&&&&&&         &&&&&&&&&&&&&&&&&&"
echo "                                    &&&&&&&&&&&&&&&&&&&&&***********&&&&&&&&&&&&"
echo "                                      &&&&&&&&&&&&&&&&&&&&******,,******%&&&&&  "
echo "                                     ,**************************************,   "
echo "                                   ******************************************** "
echo "                *************************************************************** "
echo "       ************************************************************************ "
echo "    *************************************************************************   "
echo "   *******************************************************************          "
echo "     ****************************************************************           "
echo "     **********************************************************  ***            "
echo "     ****************************************************** **    ,             "
echo "     *****************************************************                      "
echo "    .**************************************************,                        "
echo "   *************************************************,,,                         "
echo "***********************************************,,,,,,,,                         "
echo "************,,,,,,,,,                  ********,,,,,,,,                         "
echo "*********,,,,,,,,,                     ******* ,,,,,,,                          "
echo "*******  ,,,,,,                        ******* ,,,,,,,                          "
echo "******   ,,,,,,                        ******  ,,,,,,                           "
echo "******   ,,,,,                         ******  ,,,,,,                           "
echo "*****    ,,,,,                         ******  ,,,,,                            "
echo "*****    ,,,,                          *****   ,,,,,                            "
echo ",,,,,    ,,,,                          ,,,,,   ,,,,,                            "
echo
sleep 3

# go to where the bash file was executed from (should be the main project directory)
APP_PATH="`dirname \"$0\"`"
cd $APP_PATH || exit

###################################################################################################
#   Print out the versions of the building tools to use
###################################################################################################
echo "#########################################################"
echo "# BUILD TOOL VERSIONS                                   #"
echo "#########################################################"
echo
echo "Java Version:"
java -version
echo
echo "Javapackager Version:"
jdk="/Library/Java/JavaVirtualMachines/jdk-10.0.2.jdk/Contents/Home/bin"
"$jdk"/javapackager -version
echo

###################################################################################################
#   Print out the version of the app we're publishing (found in the source code)
###################################################################################################
echo "#########################################################"
echo "# APP VERSION                                           #"
echo "#########################################################"
echo
# get the old version using grep from Settings.java
version=$(grep -Eoi '[0-9]+.[0-9]+.[0-9]+' src/moose/controllers/SettingsController.java)
echo "New version number:   ${version}"
echo
echo

###################################################################################################
#   Packaging the app
###################################################################################################
echo "#########################################################"
echo "# PACKAGING PROJECT                                     #"
echo "#########################################################"
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
sleep 3
# start from scratch
rm -rf deploy
mkdir deploy
printf "Copying jar file to deploy folder..."
cp "$APP_PATH"/out/artifacts/moose_jar/moose.jar "$APP_PATH"/deploy
cd "$APP_PATH"/deploy || exit
echo "Done."
printf "Copying app icon to deploy folder..."
cp "$APP_PATH"/src/resources/moose.icns "$APP_PATH"/deploy
cd "$APP_PATH"/deploy || exit
echo "Done."
echo
mkdir -p package/macosx
cp moose.icns package/macosx
$jdk/javapackager -deploy -native $packageType -name Moose \
   -BappVersion=$version -Bicon=package/macosx/moose.icns \
   -srcdir . -srcfiles moose.jar -appclass moose.Moose \
   -outdir out -v
cp out/Moose-*.$packageType moose-"$version"-installer.$packageType
ls -l
echo
echo "Done with .$packageType!"
echo
echo

###################################################################################################
#   Clean up
###################################################################################################
echo "#########################################################"
echo "# CLEANING UP                                           #"
echo "#########################################################"
echo
printf "Cleaning up..."
rm moose.icns
rm moose.jar
rm -rf out
rm -rf package
echo "Done!"
echo
echo
echo "#########################################################"
echo "# PROJECT BUILT AND PACKAGED SUCCESSFULLY               #"
echo "#########################################################"

###################################################################################################
#   Open the installer file if we want
###################################################################################################
echo
printf "Would you like to open the .%s file? [Y/N]: " $packageType
read -r openFile
if [[ $openFile == "y" || $openFile == "Y" ]]
then
  cd "$APP_PATH"/deploy || exit
  open moose-"$version"-installer.$packageType
fi
echo
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
echo "        .'    \`-'       MOOSE"
echo "      .'"
echo "     :_   -:      \\"
echo "     |/   / :_     ||'."
echo "     '._.'_/  \`.    |  )"
echo "               '|  /    \\"
echo "                | |     |"
echo "         THANK  |||     |"
echo "          YOU    \|"
echo