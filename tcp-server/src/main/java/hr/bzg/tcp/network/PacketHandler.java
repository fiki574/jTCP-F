package hr.bzg.tcp.network;

import static hr.bzg.tcp.network.PacketId.*;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PacketHandler {
	public PacketId packetId() default INVALID;
}