/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moose;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pat
 */
public class Main_Test {
    
    public Main_Test() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of main method, of class Main.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        Main.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of launchFrame method, of class Main.
     */
    @Test
    public void testLaunchFrame() {
        System.out.println("launchFrame");
        Main.launchFrame();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of launchSettingsFrame method, of class Main.
     */
    @Test
    public void testLaunchSettingsFrame() {
        System.out.println("launchSettingsFrame");
        Main.launchSettingsFrame();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of closeSettingsFrame method, of class Main.
     */
    @Test
    public void testCloseSettingsFrame() {
        System.out.println("closeSettingsFrame");
        Main.closeSettingsFrame();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of launchAuditFrame method, of class Main.
     */
    @Test
    public void testLaunchAuditFrame() {
        System.out.println("launchAuditFrame");
        Main.launchAuditFrame();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of closeAuditFrame method, of class Main.
     */
    @Test
    public void testCloseAuditFrame() {
        System.out.println("closeAuditFrame");
        Main.closeAuditFrame();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
