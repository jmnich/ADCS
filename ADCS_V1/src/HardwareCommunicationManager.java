import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
/**
 * Created by Jakub on 15.10.2016.
 *
 * Encapsulates I2C communication with sensors and ESCs.
 */
public class HardwareCommunicationManager {
    private I2CBus      bus;
    private I2CDevice   gyro;
    private I2CDevice   acc_mag;
    private I2CDevice   barometer;
    private I2CDevice   reg1;
    private I2CDevice   reg2;
    private I2CDevice   reg3;
    private I2CDevice   reg4;
    private boolean     driverOperational;

    public HardwareCommunicationManager(){
        try {
            bus = I2CFactory.getInstance(I2CBus.BUS_1);
            gyro = bus.getDevice(0x6B);
            acc_mag = bus.getDevice(0x1D);
            barometer = bus.getDevice(0x5D);

            initAcc();
            initGyro();

            driverOperational = true;
        } catch(Exception e) {
            driverOperational = false;
            e.printStackTrace();
        }

    }

    public boolean driverIsOperational() {
        return driverOperational;
    }

    public static short mergeTwoBytes(byte bH, byte bL) {
        return (short) ((bH << 8) | (bL & 0xFF));
    }

    private void initAcc() {
        acc_mag.write(0x20, (byte)0b01010111);      // z,y,x axes enabled, 100Hz data rate
        acc_mag.write(0x23, (byte)0b00101000);      // +/- 8G scale, FS = 10 on DLHC, high res out
    }

    private void initGyro() {
        gyro.write(0x20, (byte)0b00001111);         // normal power mode, all axes enabled
        gyro.write(0x23, (byte)0b00110000);         // continuous update, 2000 deg/s full scale
    }



    public short[] readAcc() {
        short[] results = new short[3];             // stores x,y,z values

        if(driverIsOperational()) {
            // read data from accelerometer output registers
            byte xL = (byte) acc_mag.read(0x28);
            byte xH = (byte) acc_mag.read(0x29);
            byte yL = (byte) acc_mag.read(0x2A);
            byte yH = (byte) acc_mag.read(0x2B);
            byte zL = (byte) acc_mag.read(0x2C);
            byte zH = (byte) acc_mag.read(0x2D);
            // merge two-byte outputs into single variables
            results[0] = mergeTwoBytes(xH, xL);
            results[1] = mergeTwoBytes(yH, yL);
            results[2] = mergeTwoBytes(zH, zL);
        }
        else {
            results[0] = 0;
            results[1] = 0;
            results[2] = 0;
        }

        return results;
    }

    public short[] readGyro() {
        short[] results = new short[3];             // stores x,y,z values

        if(driverIsOperational()) {
            // read data from gyroscope output registers
            byte xL = (byte) gyro.read(0x28);
            byte xH = (byte) gyro.read(0x29);
            byte yL = (byte) gyro.read(0x2A);
            byte yH = (byte) gyro.read(0x2B);
            byte zL = (byte) gyro.read(0x2C);
            byte zH = (byte) gyro.read(0x2D);
            // merge two-byte outputs into single variables
            results[0] = mergeTwoBytes(xH, xL);
            results[1] = mergeTwoBytes(yH, yL);
            results[2] = mergeTwoBytes(zH, zL);
        }
        else {
            results[0] = 0;
            results[1] = 0;
            results[2] = 0;
        }

        return results;

    }
}
