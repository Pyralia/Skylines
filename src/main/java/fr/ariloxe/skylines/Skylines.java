package fr.ariloxe.skylines;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.istack.internal.NotNull;
import fr.ariloxe.skylines.impl.Creditentials;
import fr.ariloxe.skylines.packets.Packet;
import fr.ariloxe.skylines.packets.handler.PacketHandler;
import fr.ariloxe.skylines.packets.listener.PacketListener;
import fr.ariloxe.skylines.packets.listener.PacketListenerData;
import fr.ariloxe.skylines.impl.PacketSubscriber;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Skylines {

    private final Gson GSON = new GsonBuilder().create();

    private final String channel = "SkylinesChannel";
    private final JedisPool pool;
    private final Creditentials creditentials;

    private final List<PacketListenerData> packetListeners;
    private final PacketSubscriber packetSubscriber;
    private final boolean continueSub = true;

    private final Map<String, Class<? extends Packet>> idToType = new HashMap<>();
    private final Map<Class<? extends Packet>, String> typeToId = new HashMap<>();

    public JedisPool getConnection(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(12);

        return new JedisPool(jedisPoolConfig, creditentials.getHost(), creditentials.getPort(), 500, creditentials.getPassword(), false);
    }


    public Skylines(@NotNull Creditentials creditentials) {
        this.creditentials = creditentials;
        this.pool = getConnection();
        this.packetListeners = new ArrayList<>();
        this.packetSubscriber = new PacketSubscriber(this);
            while (continueSub) {
                JedisPool jedis = getConnection();
                try {
                    jedis.getResource().psubscribe(packetSubscriber, "*");
                } catch (Exception e) {
                    jedis.close();
                    e.printStackTrace();
                }

            }
    }

    public void sendPacket(Packet packet) {
        try(Jedis jedis = this.pool.getResource()) {
            final String object = GSON.toJson(packet);
            if(object == null) {
                throw new IllegalStateException("Packet cannot generate null serialized data");
            }

            final String id = typeToId.get(packet.getClass());
            if(id == null) {
                throw new IllegalStateException("Packet does not have an id");
            }

            jedis.publish(this.channel, id + ";" + object);
            System.out.println("Packet sended " + id + " " + object + " " + this.channel);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void registerPacket(Class<? extends Packet> clazz) {
        try {
            final String id = clazz.getSimpleName();
            if(idToType.containsKey(id) || typeToId.containsKey(clazz)) {
                throw new IllegalStateException("A packet with that ID has already been registered");
            }

            idToType.put(id, clazz);
            typeToId.put(clazz, id);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void registerListener(PacketListener packetListener) {
        for(Method method : packetListener.getClass().getDeclaredMethods()) {
            if(method.getDeclaredAnnotation(PacketHandler.class) != null) {
                Class<? extends Packet> packetClass = null;

                if(method.getParameters().length > 0) {
                    final Class<?> type = method.getParameters()[0].getType();
                    if(Packet.class.isAssignableFrom(type)) {
                        packetClass = (Class<? extends Packet>) type;
                    }
                }

                if(packetClass != null) {
                    this.packetListeners.add(new PacketListenerData(packetListener, method, packetClass));
                } else
                    System.out.println("Error while registering " + packetClass.getClass() + ": no methods founds");
            }
        }
    }
}