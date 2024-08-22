package net.origamimarie.minecraft.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class CoordinatesHUD {

    public static void register() {
        // Look at InGameHud and DebugHud for additional reference and ideas.
        HudRenderCallback.EVENT.register((context, tickDeltaManager) -> {
            if (MinecraftClient.isHudEnabled()) {
                PlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null) {
                    context.getMatrices().push();
                    context.getMatrices().translate((float) (context.getScaledWindowWidth() / 2), (float) (context.getScaledWindowHeight() - 63), 0.0F);
                    TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
                    String coordinatesString = (int) player.getX() + " " + (int) player.getY() + " " + (int) player.getZ();
                    int halfCoordinateStringWidth = renderer.getWidth(coordinatesString) / 2;
                    context.drawText(renderer, coordinatesString, -halfCoordinateStringWidth, -4, 0xCCFFFFFF, true);
                    context.drawText(renderer, getCompassDirection(player), halfCoordinateStringWidth + 8, -4, 0xCCFFFF88, true);
                    context.getMatrices().pop();
                }
            }
        });
    }

    private static String getCompassDirection(PlayerEntity player) {
        int yaw = MathHelper.wrapDegrees((int)player.getYaw());
        if (yaw < -158) {
            return "N";
        } else if (yaw < -112) {
            return "NE";
        } else if (yaw < -67) {
            return "E";
        } else if (yaw < -22) {
            return "SE";
        } else if (yaw < 22) {
            return "S";
        } else if (yaw < 67) {
            return "SW";
        } else if (yaw < 112) {
            return "W";
        } else if (yaw < 158) {
            return "NW";
        } else {
            return "N";
        }
    }
}
