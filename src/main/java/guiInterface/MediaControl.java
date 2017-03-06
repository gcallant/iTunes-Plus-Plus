package guiInterface;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;

/**
 * Created by Ryan on 2/25/2017.
 *
 * Code is taken from https://docs.oracle.com/javafx/2/media/playercontrol.htm
 * Thanks to
 */
public class MediaControl extends BorderPane {
    private Controller controller;
    private MediaPlayer mp;
    private boolean atEndOfMedia = false;
    private Duration duration;
    private Slider timeSlider;
    private Label playTime;
    private Pane mvPane;

    public MediaControl(){
        Runtime.getRuntime().addShutdownHook(new MediaControlShutdownHook(this));

        setStyle("-fx-background-color: #bfc2c7;");
        mvPane = new Pane() {                };
        mvPane.setStyle("-fx-background-color: black;");
        setCenter(mvPane);
//Add mediaBar
        HBox mediaBar = new HBox();
        mediaBar.setAlignment(Pos.CENTER);
        mediaBar.setPadding(new Insets(5, 10, 5, 10));
        BorderPane.setAlignment(mediaBar, Pos.CENTER);
        setBottom(mediaBar);
// Add spacer
        Label spacer = new Label("   ");
        mediaBar.getChildren().add(spacer);
// Add Time label
        Label timeLabel = new Label("Time: ");
        mediaBar.getChildren().add(timeLabel);

// Add time slider
        timeSlider = new Slider();
        HBox.setHgrow(timeSlider, Priority.ALWAYS);
        timeSlider.setMinWidth(50);
        timeSlider.setMaxWidth(Double.MAX_VALUE);
        mediaBar.getChildren().add(timeSlider);

// Add Play label
        playTime = new Label();
        playTime.setPrefWidth(130);
        playTime.setMinWidth(50);
        mediaBar.getChildren().add(playTime);
    }

//-----------------------------------------------------------------------------
// PUBLIC
//-----------------------------------------------------------------------------
    public void play(){
        Status status = mp.getStatus();

        if (status == Status.UNKNOWN  || status == Status.HALTED)
        {
            // don't do anything in these states
            return;
        }

        if ( status == Status.PAUSED
                || status == Status.READY
                || status == Status.STOPPED)
        {
            // rewind the movie if we're sitting at the end
            if (atEndOfMedia) {
                if(controller != null){
                    controller.playNextSong();
                } else {
                    System.out.println("End of Media");
                    mp.seek(mp.getStartTime());
                    atEndOfMedia = false;
                }
            }
            mp.play();
        }
    }

    public void stop(){
        mp.pause();
        mp.seek(mp.getStartTime());
        updateValues();
    }

    public void pause(){
        mp.pause();
    }

    public boolean isLoaded(String path){
        if(mp == null) return false;
        String path1 = new File(path).getAbsolutePath().replace("file:", "").replace("\\", "/").replace(" ", "%20");
        String path2 = mp.getMedia().getSource().replace("file:/", "").replace("\\", "/").replace(" ", "%20");
//        System.out.println("path: " + path1);
//        System.out.println("mp: " + path2);
        return path1.equals(path2);
    }

    public void setMediaPlayer(final MediaPlayer mediaPlayer){
        if(mp != null){
            mp.stop();
            mp.dispose();
            mvPane.getChildren().removeAll(new MediaView(mp));
        }
        mp = mediaPlayer;
        MediaView mediaView = new MediaView(mp);
        mvPane.getChildren().add(mediaView);

        // Show play time
        mp.currentTimeProperty().addListener(new InvalidationListener()
        {
            public void invalidated(Observable ov) {
                updateValues();
            }
        });
        // Set on ready
        mp.setOnReady(new Runnable() {
            public void run() {
                duration = mp.getMedia().getDuration();
                updateValues();
            }
        });
        mp.setCycleCount(1);// Song doesn't repeat
        // Set on End
        mp.setOnEndOfMedia(new Runnable() {
            public void run() {
                atEndOfMedia = true;
            }
        });
        // Set time slider
        timeSlider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (timeSlider.isValueChanging()) {
                    // multiply duration by percentage calculated by slider position
                    mp.seek(duration.multiply(timeSlider.getValue() / 100.0));
                }
            }
        });
    }

    public void dispose(){
        if(mp != null){
            mp.dispose();
        }
    }

    public void setController(Controller controller){
        this.controller = controller;
    }

//-----------------------------------------------------------------------------
//PRIVATE
//-----------------------------------------------------------------------------
    private void updateValues() {
        if (playTime != null && timeSlider != null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    Duration currentTime = mp.getCurrentTime();
                    playTime.setText(formatTime(currentTime, duration));
                    timeSlider.setDisable(duration.isUnknown());
                    if (!timeSlider.isDisabled()
                            && duration.greaterThan(Duration.ZERO)
                            && !timeSlider.isValueChanging()) {
                        timeSlider.setValue(currentTime.divide(duration.toMillis()).toMillis()
                                * 100.0);
                    }
                }
            });
        }
    }

    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int)Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60
                - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int)Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 -
                    durationMinutes * 60;
            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds,durationMinutes,
                        durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d",elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }
}

class MediaControlShutdownHook extends Thread{
    private MediaControl mc;

    public MediaControlShutdownHook(MediaControl mc){
        this.mc = mc;
    }

    public void run(){
        mc.dispose();
    }
}