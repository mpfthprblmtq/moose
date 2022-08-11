/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.mpfthprblmtq.moose;

import com.mpfthprblmtq.moose.Moose;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author pat
 */
public class MooseTest {
    
    @BeforeAll
    public void setUp() {
    }
    
    @AfterAll
    public void tearDown() {
    }

    /**
     * Test of main method, of class Main.
     */
    @Test
    public void testMain() {
        String[] args = null;
        Moose.main(args);

        assertNotNull(Moose.getSettings());
        assertNotNull(Moose.getLogger());
    }
    
}
