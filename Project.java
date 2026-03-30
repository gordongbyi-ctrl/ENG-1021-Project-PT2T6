import org.firmata4j.firmata.*;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import java.io.IOException;
import java.time.Duration;
import java.util.Scanner;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class Project {
    public static void main(String[] args) throws IOException, InterruptedException {
        // Setting up the board
        String myPort = "COM7"; // modify for your own computer & setup.
        IODevice myGroveBoard = new FirmataDevice(myPort);
        try {
            myGroveBoard.start(); // start comms with board;
            System.out.println("Board started.");
            myGroveBoard.ensureInitializationIsDone();

        } catch (Exception ex) {
            System.out.println("couldn't connect to board.");
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("What is your sex assigned at birth? Enter 1 for Male. F otherwise.");
        double sex = scanner.nextInt();
        double sex_baseline;
        if (sex == 1) {
            sex_baseline = 3.7;
        } else {
            sex_baseline = 2.7;
        }
        System.out.println("What is your body weight in pounds?");
        double weight_lbs = scanner.nextInt();
        double weight_kg = weight_lbs * 0.4536;
        System.out.println("What is your typical daily climate? (Temperate, warm, or hot? Rate 1-3");
        double temperate = scanner.nextDouble();
        double climate_extra_L;
        if (temperate == 1) {
            climate_extra_L = 0;
        }
        if (temperate == 2) {
            climate_extra_L = 0.3;
        }
        if (temperate == 3) {
            climate_extra_L = 0.7;
        } else {
            climate_extra_L = 0.3;
        }
        System.out.println("How many minutes of exercise do you do daily?");
        double exercise_minutes = scanner.nextDouble();
        System.out.println("What time do you wake up at?");
        System.out.println("Insert as an hour as an integer. Then type out the minutes.");
        double wake_hour = scanner.nextDouble();
        double wake_min = scanner.nextDouble();
        double wake_s = (wake_hour * 3.6 * (10 ^ 6)) + (wake_min * 60000);
        // LocalTime wake_time = LocalTime.of(wake_hour, wake_min);
        System.out.println("Enter the time you go to bed, in 24 hour format.");
        double bed_hour = scanner.nextDouble();
        double bed_min = scanner.nextDouble();
        double bed_s = (bed_hour * 3.6 * (10 ^ 6)) + (bed_min * 60000);
        // LocalTime bed_time = LocalTime.of(bed_hour, bed_min);

            /* Baseline formulas
            Weight calculation is grouped up, since it's just one more line anyway.
            Sex baseline is "precalculated" in prior if statement */
        double weight_baseline_L = 0.03 * weight_kg;
        // Max of whichever is greater out of sex or weight baseline.
        double total_baseline_L = Math.max(weight_baseline_L, sex_baseline);
        double drinks_baseline_L = 0.8 * total_baseline_L; // Other 20% comes from food.

        // Adjustment formulas
        double activity_extra_L = 0.35 * (exercise_minutes / 30);
        // Climate is done beforehand.

        // Final formula
        double target_drinks_l = drinks_baseline_L + activity_extra_L + climate_extra_L;

        // Time distribution formulas
        double awake_s = bed_s - wake_s;
        double interval_s = awake_s / 8;
        double serving_volume_L = (double) (target_drinks_l / 8);

        // Time distribution loop
        while (System.currentTimeMillis() < awake_s) {
            if (System.currentTimeMillis() >= bed_s) {
                break;
            }
            System.out.println("Drink " + serving_volume_L + "mL of water now!");
            for (int i = 1; i <= 10; i++) {
                var myLED = myGroveBoard.getPin(4);
                myLED.setMode(Pin.Mode.OUTPUT);
                // LED D4 on.
                myLED.setValue(1);
                // Pause for half a second.
                try {
                    Thread.sleep(500);
                } catch (Exception ex) {
                    System.out.println("sleep error.");
                }
                // LED D4 off.
                myLED.setValue(0);
                TimeUnit.SECONDS.sleep((long) interval_s);
            }
        }
    }
}
