package eecs;

import com.sun.scenario.effect.impl.prism.PrTexture;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import jssc.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.abs;
import static jssc.SerialPort.MASK_RXCHAR;


public class swMain extends Application {
    public static void main(String args[]){ launch(args);}

    @Override
    public void start(Stage stage ) throws SerialPortException {
        stage.setTitle("Major Project");
        StackPane root = new StackPane();

        Label tlabel = new Label("Temperature");
        tlabel.setFont(new Font(40));
        tlabel.setTranslateY(-200);
        Label hlabel = new Label("Humidity");
        hlabel.setFont(new Font(40));
        hlabel.setTranslateY(-100);


        Button mbutton = new Button("Manual Fan");
        mbutton.setAlignment(Pos.CENTER);
        mbutton.setTranslateX(50);
        Button abutton = new Button("Auto Fan");
        abutton.setAlignment(Pos.CENTER);
        abutton.setTranslateX(-50);
        root.getChildren().addAll(tlabel,hlabel,mbutton, abutton);
        updateUI(tlabel,hlabel,mbutton,abutton,root);
        Scene scene = new Scene(root, 500, 500);
        stage.setScene(scene);

        stage.show();
    }

    private void updateUI(Label tlabel, Label hlabel, Button mbutton, Button abutton, StackPane root) throws SerialPortException {

        Thread thread = new Thread(new Runnable() {
            public int currentTemperature = 25;
            public int currentHumidity = 70;
            public final int MAXTEMP = 30;
            public final int MAXHUMID = 70;
            public int BUZZER = 0;
            public boolean PRINT = true;
            public boolean tvsh = true;
            public boolean isFanOn = false;
            public boolean isAutoPilotOn = false;
            public void run() {

                try {
                    SerialPort sp = new SerialPort("COM4");
                    OutputStream outputStream = new SerialOutput(sp);
                    sp.openPort();
                    sp.setParams(
                            SerialPort.BAUDRATE_9600,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                    sp.setEventsMask(MASK_RXCHAR);

                    if (BUZZER == 1){
                        outputStream.write(3);
                        BUZZER = 0;
                    }

                    sp.addEventListener((SerialPortEvent serialPortEvent) -> {
                        if (serialPortEvent.isRXCHAR()) {

                            try {

                                byte[] b = sp.readBytes();
                                if (b == null || b.length <= 0) {
                                    return;
                                }
                                int value = b[0] & 0xff;

                                if (tvsh == true) {

                                    if ((abs(currentTemperature - value)) < 5) {
                                        sp.removeEventListener();
                                        if((currentTemperature>MAXTEMP)&&isAutoPilotOn) {
                                            System.out.println("I get here");
                                            outputStream.write(1);
                                        }
//                                        }else if(!isFanOn && isAutoPilotOn && currentTemperature < MAXTEMP){
//                                            outputStream.write(2);
//                                            System.out.println("YOUR DUMB");}
                                        sp.closePort();
                                        currentTemperature = value;


                                        tvsh = false;
                                        run();
                                    } else {
                                        sp.removeEventListener();
                                        sp.closePort();
                                        tvsh = false;
                                        run();
                                    }
                                } else {
                                    if((abs(currentHumidity - value) < 50)) {
                                        sp.removeEventListener();
                                        if((currentHumidity>MAXHUMID)&&isAutoPilotOn){
                                            outputStream.write(1);
                                        } else if((!isFanOn && isAutoPilotOn && currentTemperature < MAXHUMID)){
                                            outputStream.write(2);
                                            }
                                        sp.closePort();
                                        tvsh = true;
                                        currentHumidity = value;
                                        run();
                                    } else{
                                        sp.removeEventListener();
                                        sp.closePort();
                                        tvsh = true;
                                        run();
                                    }



                                }

                                //System.out.println(currentTemperature);
                            } catch (SerialPortException | IOException ex) {
                                Logger.getLogger(swMain.class.getName())
                                        .log(Level.SEVERE, null, ex);
                            }

                        }
                    });
                    mbutton.setOnMousePressed(value -> {
                        try {
                            if(isFanOn){
                                outputStream.write(2);
                                isFanOn = false;
                            } else{
                                outputStream.write(1);
                                isFanOn = true;
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    });
                    abutton.setOnMousePressed(value -> {
                            if(isFanOn){
                                System.out.println("Cannot override manual controls.");
                            } else{
                                if(isAutoPilotOn){
                                    System.out.println("Autopilot off");
                                    isAutoPilotOn=false;
                                } else {isAutoPilotOn=true;
                                    System.out.println("Autopilot on");
                                    BUZZER = 1;
                                }
                            }
                    });


                } catch (SerialPortException | IOException ex) {
                    Logger.getLogger(swMain.class.getName())
                            .log(Level.SEVERE, null, ex);
                    System.out.println("SerialPortException: " + ex.toString());

                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        tlabel.textProperty().set(Integer.toString(currentHumidity)+ "% humidity");
                        hlabel.textProperty().set(Integer.toString(currentTemperature)+ " degrees Celsius");


                        if(PRINT){
                            Timeline timeline = new Timeline(
                                    new KeyFrame(Duration.seconds(30), e -> {
                                        WritableImage image = root.snapshot(new SnapshotParameters(), null);
                                        File file = new File("D:\\sathi\\Documents\\Google Drive\\MajorProject.png");
                                        try {
                                            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                                        } catch (IOException f) {
                                            f.printStackTrace();
                                        } PRINT = true;
                                        System.out.println("Export Sent");

                                    })
                            );
                            timeline.play();
                            PRINT = false;
                        }


                    }
                });
            }
        });
        thread.start();
    }




}
