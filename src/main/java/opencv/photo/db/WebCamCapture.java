package opencv.photo.db;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

public class WebCamCapture {

    private static boolean exitApplication = false;
    private static final String imageExtension = "png";

    public void run() throws org.bytedeco.javacv.FrameGrabber.Exception {
        // Create a frame grabber for the default camera (usually the built-in webcam)
        try (OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0)) {
            grabber.start();

            // Create a canvas frame for display
            CanvasFrame canvasFrame = new CanvasFrame("Webcam Capture", CanvasFrame.getDefaultGamma() / grabber.getGamma());

            // Create a converter
            try (OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat()) {

                // Add a KeyListener to detect Enter key press
                canvasFrame.getCanvas().addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_ENTER:
                                saveImage(grabber, converterToMat);
                                break;
                            case KeyEvent.VK_ESCAPE:
                                exitApplication = true;
                                break;
                        }
                    }
                });

                // Main loop to capture and display frames
                do {
                    // Break the loop if the exitApplication flag is set
                    if (exitApplication) {
                        break;
                    }
                    // Capture a frame from the webcam
                    org.bytedeco.javacv.Frame frame = grabber.grab();

                    // Convert the frame to Mat
                    Mat matFrame = converterToMat.convertToMat(frame);

                    // Apply any additional processing if needed
                    // For example, resize the frame
                    // opencv_imgproc.resize(matFrame, matFrame, new Size(640, 480));

                    // Display the converted Mat in the canvas frame
                    canvasFrame.showImage(converterToMat.convert(matFrame));

                    // Break the loop if the canvas frame is closed
                } while (canvasFrame.isVisible());
            }

            // Release resources
            grabber.stop();
            canvasFrame.dispose();
        }
    }

    private static void drawText(Mat image, String text, Point position, Scalar color) {
        opencv_imgproc.putText(image, text, position, opencv_imgproc.FONT_HERSHEY_SIMPLEX, 1.0, color, 2, opencv_imgproc.LINE_AA, false);
    }

    private static void saveImage(OpenCVFrameGrabber grabber, OpenCVFrameConverter.ToMat converterToMat) {

        try {
            // Capture a frame from the webcam
            org.bytedeco.javacv.Frame frame = grabber.grab();
            // Convert the frame to Mat
            Mat matFrame = converterToMat.convertToMat(frame);

            byte[] imageData = new byte[0];
            // Convert the IplImage to a byte array
            opencv_imgcodecs.imencode("." + imageExtension, matFrame, imageData);

            saveImageToFile("captured." + imageExtension, imageData);
            saveImageToDatabase("image/" + imageExtension, imageData);

            // Draw text on the Mat frame
            drawText(matFrame, "Image saved!", new Point(20, 50), new Scalar(0, 255, 0, 0));

        } catch (IOException | SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private static void saveImageToFile(final String filePath, byte[] imageData) throws IOException {
        // Save the current frame as a PNG file
        Path outputPath = Path.of(filePath);
        Files.write(outputPath, imageData);
        System.out.println("Image saved at: " + outputPath);
    }

    private static void saveImageToDatabase(final String mimeType, byte[] imageData) throws SQLException, IOException, ClassNotFoundException {
        // Establish a JDBC connection (modify connection URL, username, and password)
        ConnectionFactory.insertImage(mimeType, imageData);
        System.out.println("Image saved to database.");
    }

}
