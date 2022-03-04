package fr.ariloxe.skylines.example;

import fr.ariloxe.skylines.packets.handler.PacketHandler;
import fr.ariloxe.skylines.packets.listener.PacketListener;

/**
 * @author Ariloxe
 */
public class TestListener implements PacketListener {

    @PacketHandler(id = "test")
    public void onTest(TestPacket testPacket){
        System.out.println("packet lol:");
        System.out.println(testPacket.getMessageToPrintln() + testPacket.isTest);
    }


}
