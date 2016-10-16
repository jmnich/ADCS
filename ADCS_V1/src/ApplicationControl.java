import FFDENetwork.FFDEEvent;
import FFDENetwork.FFDEKernel;
import FFDENetwork.FFDEObserver;
import FFDENetwork.FFDEServer;

import java.util.Arrays;

/**
 * Created by Jakub on 10.10.2016.
 *
 * High level coordination and data handling.
 */
public class ApplicationControl implements FFDEObserver, Runnable{

    private HardwareCommunicationManager    hardCom;
    private FFDEServer      ffdeServer;
    private static String   mainLogID           =   "mainLog";
    private static String   accChannelID        =   "rawAcc";
    private static String   gyroChannelID       =   "rawGyro";
    private static String   magnetChannelID     =   "rawMagnet";
    private static String   barChannelID        =   "rawBar";
    private static String   angleChannelID      =   "filteredAngle";
    private static String   angVelChannelID     =   "filteredAngularVel";

    public ApplicationControl() {

        ffdeServer = new FFDEServer("ADCS", 6666, this);

        hardCom = new HardwareCommunicationManager();

        ffdeServer.publish(accChannelID);
        ffdeServer.publish(gyroChannelID);
        ffdeServer.publish(magnetChannelID);
        ffdeServer.publish(barChannelID);

        ffdeServer.publish(angleChannelID);
        ffdeServer.publish(angVelChannelID);

        ffdeServer.openPipeline(mainLogID);

        Thread th = new Thread(this);
        th.start();
    }

    @Override
    public void notifyFFDE(FFDEEvent event) {

    }

    @Override
    public void run() {
        ffdeServer.waitUntilNetworkIsReady();

        ffdeServer.sendThroughPipeline("mainLog", Arrays.asList("Time  " + String.valueOf(System.nanoTime()),
                "ADCS up"));
    }
}
