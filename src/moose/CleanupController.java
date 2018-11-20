/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moose;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author pat
 */
public class CleanupController {
    
    // some ivars
    int cleanupCount;
    ArrayList<File> albums = new ArrayList<>();
    File cleanupFolder;
    File currentDir;
    
    ArrayList<ArrayList<String>> cleanupFilePathList = new ArrayList<>(Arrays.asList(
            new ArrayList<>(),      // mp3.asd
            new ArrayList<>(),      // flac
            new ArrayList<>(),      // wav
            new ArrayList<>(),      // zip
            new ArrayList<>(),      // image files
            new ArrayList<>(),      // windows files
            new ArrayList<>()));    // other files
    
    // some constants
    final int MP3ASD = 0;
    final int FLAC = 1;
    final int WAV = 2;
    final int ZIP = 3;
    final int IMG = 4;
    final int WINDOWS = 5;
    final int OTHER = 6;
    
    public CleanupController() {
        
    }
    
    public void setCleanupFolder(File cleanupFolder) {
        this.cleanupFolder = cleanupFolder;
    }
    
}
