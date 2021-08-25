package dzwdz.chat_heads.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.mixinterface.GuiMessageOwnerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class ChatComponentMixin {
    @Shadow @Final private MinecraftClient minecraft;

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I",
                    ordinal = 0
            ),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V",
            index = 2
    )
    public float moveTheText(MatrixStack poseStack, OrderedText formattedCharSequence, float f, float y, int color) {
        ChatHeads.lastY = (int)y;
        ChatHeads.lastOpacity = (((color >> 24) + 256) % 256) / 255f; // haha yes
        return ChatHeads.CHAT_OFFSET;
    }

    @ModifyVariable(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/GuiMessage;getAddedTime()I"
            ),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V"
    )
    public ChatHudLine<?> captureGuiMessage(ChatHudLine<?> guiMessage) {
        ChatHeads.lastGuiMessage = guiMessage;
        return guiMessage;
    }

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I",
                    ordinal = 0
            ),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V"
    )
    public void render(MatrixStack matrixStack, int i, CallbackInfo ci) {
        PlayerListEntry owner = ((GuiMessageOwnerAccessor)ChatHeads.lastGuiMessage).chatheads$getOwner();
        if (owner != null) {
            RenderSystem.setShaderColor(1, 1, 1, ChatHeads.lastOpacity);
            RenderSystem.setShaderTexture(0, owner.getSkinTexture());
            // draw base layer
            DrawableHelper.drawTexture(matrixStack, 0, ChatHeads.lastY, 8, 8, 8.0F, 8, 8, 8, 64, 64);
            // draw hat
            DrawableHelper.drawTexture(matrixStack, 0, ChatHeads.lastY, 8, 8, 40.0F, 8, 8, 8, 64, 64);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/StringSplitter;componentStyleAtWidth(Lnet/minecraft/util/FormattedCharSequence;I)Lnet/minecraft/network/chat/Style;"
            ),
            method = "getClickedComponentStyleAt(DD)Lnet/minecraft/network/chat/Style;",
            index = 1
    )
    public int correctClickPosition(int x) {
        return x - ChatHeads.CHAT_OFFSET;
    }

    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;getWidth()I"
            ),
            method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V"
    )
    public int fixTextOverflow(ChatHud chatHud) {
        return ChatHud.getWidth(minecraft.options.chatWidth) - ChatHeads.CHAT_OFFSET;
    }

    @Inject(
            at = @At("HEAD"),
            method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V"
    )
    private void detectNewMessage(CallbackInfo ci) {
        ChatHeads.firstLine = true;
    }
}