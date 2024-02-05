package opencv.photo.db;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;

public class App {

    private static boolean exitApplication = false;

    public static void main(String[] args) throws Exception {
        // Create a frame grabber for the default camera (usually the built-in webcam)
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();

        // Create a canvas frame for display
        CanvasFrame canvasFrame = new CanvasFrame("Webcam Capture", CanvasFrame.getDefaultGamma() / grabber.getGamma());

        // Create a converter
        OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();

        // Add a KeyListener to detect Enter key press
        canvasFrame.getCanvas().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER -> saveImage(grabber, converterToMat);
                    case KeyEvent.VK_ESCAPE -> exitApplication = true;
                }
            }


            @Override
            public void keyReleased(KeyEvent e) {
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
//            opencv_imgproc.resize(matFrame, matFrame, new Size(640, 480));

            // Display the converted Mat in the canvas frame
            canvasFrame.showImage(converterToMat.convert(matFrame));

            // Break the loop if the canvas frame is closed
        } while (canvasFrame.isVisible());

        // Release resources
        grabber.stop();
        canvasFrame.dispose();
    }

    private static void drawText(Mat image, String text, Point position, Scalar color) {
        opencv_imgproc.putText(image, text, position, opencv_imgproc.FONT_HERSHEY_SIMPLEX, 1.0, color, 2, opencv_imgproc.LINE_AA, false);
    }

    private static void saveImage(OpenCVFrameGrabber grabber, OpenCVFrameConverter.ToMat converterToMat){

        try {
            // Capture a frame from the webcam
            org.bytedeco.javacv.Frame frame = grabber.grab();
            // Convert the frame to Mat
            Mat matFrame = converterToMat.convertToMat(frame);

            saveImageToFile(matFrame);
            saveImageToDatabase(matFrame);

            // Draw text on the Mat frame
            drawText(matFrame, "Image saved!", new Point(20, 50), new Scalar(0, 255, 0, 0));

        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }


    }

    private static void saveImageToFile(Mat matFrame) {
        try {

            // Save the current frame as a PNG file
            String outputPath = "captured_image.png";
            opencv_imgcodecs.imwrite(outputPath, matFrame);
            System.out.println("Image saved at: " + outputPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveImageToDatabase(Mat matFrame) {
        try {
            byte[] imageData = new byte[0];
            // Convert the IplImage to a byte array
            opencv_imgcodecs.imencode(".png", matFrame, imageData);

            // Establish a JDBC connection (modify connection URL, username, and password)
            try {
                ConnectionFactory.insertImage("image/png", imageData);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            System.out.println("Image saved to database.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
