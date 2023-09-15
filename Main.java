import org.firmata4j.I2CDevice;
import org.firmata4j.IODevice;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.Pin;
import org.firmata4j.ssd1306.SSD1306;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        String myPort = "COM3"; //My port

        IODevice myBoard = new FirmataDevice(myPort); //My Board
        myBoard.start();
        myBoard.ensureInitializationIsDone();

        I2CDevice i2cObject = myBoard.getI2CDevice((byte) 0x3C);
        SSD1306 theOledObject = new SSD1306(i2cObject, SSD1306.Size.SSD1306_128_64);
        theOledObject.init();

        var sensor = myBoard.getPin(15); //moisture sensor
        sensor.setMode(Pin.Mode.ANALOG);

        var pump = myBoard.getPin(7); //water pump
        pump.setMode(Pin.Mode.OUTPUT);

        var button = myBoard.getPin(6); //button
        button.setMode(Pin.Mode.INPUT);

        ArrayList<Float> list = new ArrayList<Float>();
        ArrayList<Long> times = new ArrayList<Long>();

        long value = 0;
        while (button.getValue() == 0) {
            value = sensor.getValue();
            System.out.println("Sensor value: " + value);

            list.add((float) value);
            times.add(System.currentTimeMillis());

            theOledObject.getCanvas().write(String.valueOf(value));
            theOledObject.display();
            Thread.sleep(2000);
            theOledObject.clear();

            if (650 <= value) { //really dry
               // System.out.println("The plant is very dry let me water that for you");
                pump.setValue(1);
                Thread.sleep(5000); //wait for 5 seconds to let the water flow
                pump.setValue(0);

            } else if (600 <= value && value <= 649) { //a bit dry
               // System.out.println("The plant is a bit dry let me water that for you");
                pump.setValue(1);
                Thread.sleep(3000); //wait for 3 seconds to let the water flow
                pump.setValue(0);

            } else {//the plant is watered
                //System.out.println("The plant is watered");
                pump.setValue(0);
            }
        }

        // Set canvas dimensions
        StdDraw.setCanvasSize(800, 800);

        // Set x and y ranges
        StdDraw.setXscale(times.get(0), times.get(times.size()-1));
        StdDraw.setYscale(0,1000);

        // Plot the data points on the graph
        for (int i = 1; i < list.size(); i++) {
            StdDraw.line(times.get(i-1), list.get(i-1), times.get(i), list.get(i));
        }

        // Add labels to the graph
        StdDraw.text(times.get(times.size()-1), value, String.valueOf(value));
        StdDraw.text(times.get(times.size()-1), 1000, "Max Value: " + Collections.max(list));

        // Show the graph
        StdDraw.show();
    }
}
