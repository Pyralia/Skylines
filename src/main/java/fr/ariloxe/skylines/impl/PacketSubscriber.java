package fr.ariloxe.skylines.impl;

import fr.ariloxe.skylines.Skylines;
import fr.ariloxe.skylines.packets.Packet;
import fr.ariloxe.skylines.packets.handler.PacketHandler;
import fr.ariloxe.skylines.packets.listener.PacketListenerData;
import lombok.AllArgsConstructor;
import redis.clients.jedis.JedisPubSub;

/**
 * @author Ariloxe
 */

@AllArgsConstructor
public class PacketSubscriber extends JedisPubSub {

    private final Skylines skylines;

    @Override
    public void onMessage(String channel, String message) {
        System.out.println("test1");

        if(!channel.equalsIgnoreCase(skylines.getChannel()))
            return;

        System.out.println("test2");

        try {
            String[] args = message.split(";");
            String id = args[0];
            if(!skylines.getIdToType().containsKey(id)) {
                throw new IllegalStateException("A packet with that ID does not exist");
            }

            Packet packet = skylines.getGSON().fromJson(args[1], skylines.getIdToType().get(id));
            if(packet != null) {
                for(PacketListenerData data : skylines.getPacketListeners()) {
                    if (data.getMethod().getAnnotation(PacketHandler.class).id().equalsIgnoreCase(id))
                        data.getMethod().invoke(data.getInstance(), packet);

                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
