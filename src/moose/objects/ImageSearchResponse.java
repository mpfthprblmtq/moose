/*
   Proj:   Moose
   File:   ImageSearchResponse.java
   Desc:   Pojo for the response we get back from the Google CSE API call

   Copyright Pat Ripley 2018
 */

// package
package moose.objects;

// imports
import java.awt.image.BufferedImage;

// class ImageSearchResponse
public class ImageSearchResponse {

    private String link;
    private String mime;
    private Image image;
    private BufferedImage bImage;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public BufferedImage getBImage() {
        return bImage;
    }

    public void setBImage(BufferedImage bImage) {
        this.bImage = bImage;
    }

    public static class Image {

        private int height;

        public void setHeight(int height) {
            this.height = height;
        }

        public int getHeight() {
            return this.height;
        }
    }
    
    
}
