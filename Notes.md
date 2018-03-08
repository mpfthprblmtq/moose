# Moose

## Notes

* Menu actions
  * File
    * Exit
  * Open
    * Open File
    * Open folder
  * Help
    * About
    * Version

* Buttons
  * Clear Table
  * Save


## Code

#### Double click
    public void mouseClicked(MouseEvent event) {
      if (event.getClickCount() == 2) {
        System.out.println("double clicked");
      }
    }

#### Playing mp3 file
    Desktop.getDesktop().open(File file);
