import FFDENetwork.FFDEEvent;
import FFDENetwork.FFDEKernel;
import FFDENetwork.FFDEObserver;
import FFDENetwork.FFDEServer;

/**
 * Created by Jakub on 10.10.2016.
 *
 * High level coordination and data handling.
 */
public class ApplicationControl implements FFDEObserver, Runnable{

    private HardwareCommunicationManager hardCom;
    private FFDEServer ffdeServer;
    private FFDEKernel ffdeKernel;

    public ApplicationControl() {

        ffdeServer = new FFDEServer("ADCS", 6666, this);


        hardCom = new HardwareCommunicationManager();


        ffdeServer.publish("rawAcc");
        ffdeServer.publish("rawGyro");
        ffdeServer.publish("rawMagnet");
        ffdeServer.publish("rawBar");

        ffdeServer.publish("filteredAngle");
        ffdeServer.publish("filteredAngularVel");

        Thread th = new Thread(this);
        th.start();
    }

    @Override
    public void notifyFFDE(FFDEEvent event) {

    }

    @Override
    public void run() {
        ffdeServer.waitUntilNetworkIsReady();



    }
}
