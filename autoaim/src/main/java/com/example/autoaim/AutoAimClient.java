package com.example.autoaim;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class AutoAimClient implements ClientModInitializer {
    public static AutoAimConfig config;
    private static KeyBinding toggleKey;
    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        config = AutoAimConfig.load();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autoaim.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.autoaim"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autoaim.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.autoaim"));

        ClientTickEvents.END_CLIENT_TICK.register(AutoAimClient::onTick);
    }

    private static void onTick(MinecraftClient mc) {
        while (toggleKey.wasPressed()) {
            config.enabled = !config.enabled;
            config.save();
            if (mc.player != null) {
                mc.player.sendMessage(new LiteralText("Auto Aim: " + (config.enabled ? "ВКЛ" : "ВЫКЛ")), true);
            }
        }
        while (menuKey.wasPressed()) {
            if (mc.currentScreen == null) {
                mc.openScreen(new AutoAimScreen(null));
            }
        }

        if (!config.enabled || mc.player == null || mc.world == null || mc.currentScreen != null) return;

        LivingEntity target = findTarget(mc);
        if (target != null) aimAt(mc.player, target);
    }

    private static LivingEntity findTarget(MinecraftClient mc) {
        ClientPlayerEntity me = mc.player;
        LivingEntity best = null;
        double bestDist = config.range;

        for (Entity e : mc.world.getEntities()) {
            if (e == me || !(e instanceof LivingEntity) || !e.isAlive() || e.isSpectator()) continue;

            boolean ok;
            switch (config.target) {
                case PLAYERS: ok = e instanceof PlayerEntity; break;
                case MOBS: ok = e instanceof HostileEntity; break;
                default: ok = e instanceof PlayerEntity || e instanceof HostileEntity; break;
            }
            if (!ok) continue;

            double d = me.distanceTo(e);
            if (d <= bestDist && me.canSee(e)) {
                bestDist = d;
                best = (LivingEntity) e;
            }
        }
        return best;
    }

    private static void aimAt(ClientPlayerEntity me, LivingEntity t) {
        double dx = t.getX() - me.getX();
        double dy = t.getBodyY(0.5) - me.getEyeY();
        double dz = t.getZ() - me.getZ();
        double flat = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) (MathHelper.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        float targetPitch = (float) -(MathHelper.atan2(dy, flat) * 180.0 / Math.PI);

        float step = (float) config.speed;
        float dYaw = MathHelper.clamp(MathHelper.wrapDegrees(targetYaw - me.yaw), -step, step);
        float dPitch = MathHelper.clamp(targetPitch - me.pitch, -step, step);

        me.yaw += dYaw;
        me.pitch = MathHelper.clamp(me.pitch + dPitch, -90.0F, 90.0F);
    }
}
