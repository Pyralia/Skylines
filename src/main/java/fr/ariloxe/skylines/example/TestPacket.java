package fr.ariloxe.skylines.example;

import fr.ariloxe.skylines.packets.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Ariloxe
 */

@Getter
@AllArgsConstructor
public class TestPacket implements Packet {

    final boolean isTest;
    final String messageToPrintln;

}
