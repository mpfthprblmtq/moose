/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moose.controllers;

import java.io.File;
import moose.utilities.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author pat
 */
public class SongControllerTest {
    
    SongController songController;
    Logger logger = new Logger();
    
    @Before
    public void setup() {
        songController = new SongController();
        logger.setSystemOutToConsole();
    }

    @Test
    public void testAutoTag() {
    }

    @Test
    public void testGetTitleFromFile() {
    }

    @Test
    public void testGetAlbumFromFile() {
    }

    @Test
    public void testGetYearFromFile() {
        System.out.println("Testing getYearFromFile()");
        
        File dir = new File("[2018] albumname");
        File file = new File(dir.getPath() + "//" + "02 trackname.mp3");
        dir.mkdir();
        String regex = "\\d{4}";
        String year;
        
        // scenario 1
        System.out.print("Should pass : ");
        year = songController.getYearFromFile(file);
        System.out.println(year);
        Assert.assertTrue(year.matches(regex));
        
        // scenario 2
        File dir2 = new File("albumname");
        File file2 = new File(dir2.getPath() + "//" + "02 trackname.mp3");
        System.out.print("Should not pass : ");
        
        year = songController.getYearFromFile(file2);
        System.out.println(year);
        Assert.assertFalse(year.matches(regex));
        
        // cleanup
        dir.delete();
        dir2.delete();
        System.out.println("");
        
    }

    @Test
    public void testGetTracksFromFolder() {
    }

    @Test
    public void testGetTotalTracksFromFolder() {
    }

    @Test
    public void testGetNumberOfSongs() {
    }
    
    @After
    public void tearDown() {
        
    }

}
