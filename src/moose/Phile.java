
package moose;

import java.io.File;

public class Phile {
    
    private File file;
    
    public Phile(File file) {
        this.file = file;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return this.file.getName();
    }
    
    
    
}
