/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moose.controllers;

import java.io.File;

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
    
    @Before
    public void setup() {
        songController = new SongController();
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
        
        File dir1 = new File("[2018] albumname");
        File file1 = new File(dir1.getPath() + "//" + "02 trackname.mp3");
        dir1.mkdir();
        String regex = "\\d{4}";
        String year;
        
        // scenario 1
        System.out.print("Should pass : ");
        year = songController.autoTaggingService.getYearFromFile(file1);
        System.out.println(year);
        Assert.assertTrue(year.matches(regex));
        
        // scenario 2
        File dir2 = new File("albumname");
        File file2 = new File(dir2.getPath() + "//" + "02 trackname.mp3");
        System.out.print("Should not pass : ");
        
        year = songController.autoTaggingService.getYearFromFile(file2);
        System.out.println(year);
        Assert.assertFalse(year.matches(regex));

        // scenario 3
        File dir3 = new File("[201] albumname");
        File file3 = new File(dir3.getPath() + "//" + "02 trackname.mp3");
        System.out.print("Should not pass : ");

        year = songController.autoTaggingService.getYearFromFile(file3);
        System.out.println(year);
        Assert.assertFalse(year.matches(regex));

        // scenario 4
        year = songController.autoTaggingService.getYearFromFile(null);
        System.out.println("Should not pass : ");
        Assert.assertFalse(year.matches(regex));

        // cleanup
        dir1.delete();
        dir2.delete();
        dir3.delete();
        System.out.println("");
        
    }

    @Test
    public void testGetDisksFromFile() {
        System.out.println("Testing getDisksFromFile()");
        File file1 = new File("[2018] album name/CD1/03 trackname.mp3");
        File file2 = new File("[2018] album name/CD2/09 trackname.mp3");
        File file3 = new File("[2018] album name/CD13/04 tracknameeeee.mp3");
        File file4 = new File("[2018] album name/cheese/what.mp3");
        String regex = "\\d*/\\d*";
        String disks;

        System.out.print("Should pass : ");
        disks = songController.autoTaggingService.getDisksFromFile(file1);
        System.out.println(disks);
        Assert.assertTrue(disks.matches(regex));

        System.out.print("Should pass : ");
        disks = songController.autoTaggingService.getDisksFromFile(file2);
        System.out.println(disks);
        Assert.assertTrue(disks.matches(regex));

        System.out.print("Should pass : ");
        disks = songController.autoTaggingService.getDisksFromFile(file3);
        System.out.println(disks);
        Assert.assertTrue(disks.matches(regex));

        System.out.print("Shouldn't pass : ");
        disks = songController.autoTaggingService.getDisksFromFile(file4);
        System.out.println(disks);
        Assert.assertFalse(disks.matches(regex));
    }

    @After
    public void tearDown() {
        
    }

}
