/*
   Proj:   Moose
   File:   Utils.java
   Desc:   Helper class with a smattering of methods to do some neat stuff

   Copyright Pat Ripley 2018
 */

// package
package moose.utilities;

// imports
import java.util.Arrays;

import moose.Moose;
import moose.utilities.logger.Logger;

public class MiscUtils {

    // logger
    static Logger logger = Moose.logger;
    
    /**
     * Checks if int[] contains a certain int
     * 
     * @param arr, the array of ints to check
     * @param key, the key to check for
     * @return the result of the check
     */
    public static boolean intArrayContains(int[] arr, int key) {
        return Arrays.stream(arr).anyMatch(i -> i == key);
    }

    /**
     * Checks if a byte array is the same throughout an array
     * @param bytes, the byte array to check
     * @param arr,   the array of byte arrays
     * @return the result of the check
     */
    public static boolean checkIfSame(byte[] bytes, byte[][] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (!Arrays.equals(arr[i], bytes)) {
                return false;
            }
        }
        return true;
    }
}
