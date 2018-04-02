import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.media.AudioClip;

public class Timer implements Runnable {
    // internal fields
    private int hr;   // [0, 999]
    private int min;  // [0, 59]
    private int sec;  // [0, 59]
    private long totalSec;

    // share initialized TextFields from MainWindow in order to update the GUI countdown timer
    private TextField hrField;
    private TextField minField;
    private TextField secField;

    private final AudioClip alarmSound = new AudioClip(this.getClass().getResource("sound.wav").toString());

    // private Thread thread;
	private final Object LOCK = new Object();

    private volatile boolean stopped = true;
    private volatile boolean paused = true;

    public Timer(final TextField hrField, final TextField minField, final TextField secField) {
        this.setTime(0, 0, 0);
        this.hrField = hrField; this.minField = minField; this.secField = secField;
    }

    public boolean wasPaused() { return this.paused; }
    public boolean isStopped() { return this.stopped; }

    public boolean timesUp() { return alarmSound.isPlaying(); }

    public void setTime(int hr, int min, int sec) {
        this.hr = hr; this.min = min; this.sec = sec;
        totalSec = hr * 60 * 60 + min * 60 + sec;
    }

    // update the internal + GUI timer
    private void update() {
        synchronized (LOCK) {
            int totalSecInInt = new Long(totalSec).intValue();
            this.hr = totalSecInInt / 3600;
            this.min = (totalSecInInt / 60) % 60;
            this.sec = totalSecInInt % 60;

            Platform.runLater(() -> {
                this.hrField.setText(new Integer(hr).toString()); 
                this.minField.setText(new Integer(min).toString()); 
                this.secField.setText(new Integer(sec).toString());
            });
        }
    }

    // reset the internal + GUI timer
    public void reset() {
        synchronized (LOCK) {
            this.stop();

            this.setTime(0, 0, 0);
            Platform.runLater(() -> { 
                hrField.setText("00"); minField.setText("00"); secField.setText("00"); 
                hrField.setEditable(true); minField.setEditable(true); secField.setEditable(true);
            });
        }
    }

    // start the timer
    public void start() { this.stopped = false; this.paused = false; }

    // resume the timer
    public void resume() {
        synchronized (LOCK) {
            this.paused = false;

            LOCK.notify();
        }
    }

    // main task
    @Override
    public void run() {
        try {
            while (totalSec > 0) {
                // sleeping in segment can pause the timer more accurately
                // two operations -> 1000/2
                for (int i = 0; i < 750; i++) {
                    Thread.sleep(1);

                    synchronized (LOCK) {
                        while (paused)
                            LOCK.wait();
                    }
                }

                totalSec--;
                this.update();
            }

            // sound
            if (!alarmSound.isPlaying()) {
                alarmSound.setCycleCount(AudioClip.INDEFINITE);
                alarmSound.setRate(1.0);
                alarmSound.play();
            }

            // System.out.println("finish");
        }
        catch (InterruptedException e) {

        }
    }

    // pause the timer
    public void pause() {
        synchronized (LOCK) {
            this.paused = true;
        }
    }

    // stop the timer
    public void stop() { 
        this.stopped = true; this.paused = true; 

        if (alarmSound.isPlaying())
            alarmSound.stop();
    }

    // interrupt the timer thread
    // public void interrupt() {
    //     // if (this.thread.getState() == Thread.State.NEW || this.thread.getState() == Thread.State.TERMINATED)  // not yet started
    //     if (!this.thread.isAlive())
    //         return;

    //     this.thread.interrupt();
    // }
}
