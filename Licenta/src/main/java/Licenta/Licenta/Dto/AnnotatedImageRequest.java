package Licenta.Licenta.Dto;

public class AnnotatedImageRequest {

    private String annotatedImage; // base64 JPEG (cu sau fără prefix data:image/jpeg;base64,)

    public String getAnnotatedImage() {
        return annotatedImage;
    }

    public void setAnnotatedImage(String annotatedImage) {
        this.annotatedImage = annotatedImage;
    }
}
