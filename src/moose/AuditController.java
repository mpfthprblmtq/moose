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
public class AuditController {
    
    // some ivars
    int auditCount;
    ArrayList<File> albums = new ArrayList<>();
    File auditFolder;
    File currentDir;
    
    // Arraylist for files found
    

    ArrayList<ArrayList<String>> auditFilePathList = new ArrayList<>(Arrays.asList(
            new ArrayList<>(),      // id3
            new ArrayList<>(),      // filenames
            new ArrayList<>()));    // cover art
    
    // logger object
    Logger logger = Main.logger;
    
    // some constants
    final int ID3 = 0;
    final int FILENAMES = 1;
    final int COVER = 2;
    
    public AuditController() {
        
    }
    
    public void setAuditFolder(File auditFolder) {
        this.auditFolder = auditFolder;
    }
    
}
