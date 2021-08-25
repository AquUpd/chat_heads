package dzwdz.chat_heads;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.network.PlayerListEntry;
import org.jetbrains.annotations.Nullable;

public class ChatHeads {
    @Nullable
    public static PlayerListEntry lastSender;
    @Nullable
    public static ChatHudLine<?> lastGuiMessage;

    public static int lastY = 0;
    public static float lastOpacity = 0;
    public static boolean firstLine = false;

    public static final int CHAT_OFFSET = 10;
}
