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
$jdk/bin/javapackager -version
echo
echo

###################################################################################################
#   Set up version number
###################################################################################################
echo "##################################################"
echo "# VERSION CONFIGURATION                          #"
echo "##################################################"
echo
# get the old version
cd git/moose/
oldVersion=`sed -n '1p' version`
echo "Old version number: ${oldVersion}"
# get user input for new version
printf "Enter new version number: "
read -r newVersion
# clear out old version
> version
# put in new version in release file
echo "${newVersion}" >> version
echo "Put the new version in version file"
# put in new version in source code for hardened version
cd src/moose/objects/
sed -i '' "s/private String version = \"${oldVersion}\";/private String version = \"${newVersion}\";/g" Settings.java
echo "Put the new version in source code"
cd ../../..

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
ant -f /Users/pat/Git/moose package-for-deploy
echo "Done building app!"
echo
echo

###################################################################################################
#   Packaging the app
###################################################################################################
echo "##################################################"
echo "# PACKAGING PROJECT TO .PKG                      #"
echo "##################################################"
echo
echo "Packaging the application into .pkg file..."
sleep 3
echo
cp /Users/pat/git/moose/src/resources/moose.icns git/moose/deploy
cd /Users/pat/git/moose/deploy
mkdir -p package/macosx
cp moose.icns package/macosx
$jdk/bin/javapackager -deploy -native pkg -name Moose \
   -BappVersion=1.1.3 -Bicon=package/macosx/moose.icns \
   -srcdir . -srcfiles moose.jar -appclass moose.Main \
   -outdir out -v
cp out/Moose-*.pkg moose-installer.pkg
ls -l
echo
echo "Done packaging app!"

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
printf "Would you like to open the .pkg file? [Y/N]: "
read -r openFile
if [ "$openFile" == "y" ]
then
  cd deploy
  open moose-installer.pkg
else
    # do nothing, just exit
fi


