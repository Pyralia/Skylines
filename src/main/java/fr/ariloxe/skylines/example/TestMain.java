package fr.ariloxe.skylines.example;

import fr.ariloxe.skylines.Skylines;
import fr.ariloxe.skylines.impl.Creditentials;

/**
 * @author Ariloxe
 */
public class TestMain {

    public static void main(final String[] args) {
        Skylines skylines = new Skylines(new Creditentials("", 667, ""));

        skylines.registerPacket(TestPacket.class);
        skylines.registerListener(new TestListener());
        try{
            Thread.sleep(100);
            skylines.sendPacket(new TestPacket(true, "Ceci est actuellement un test: "));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
