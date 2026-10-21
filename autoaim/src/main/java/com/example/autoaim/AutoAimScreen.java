package com.example.autoaim;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class AutoAimScreen extends Screen {
    private final Screen parent;

    public AutoAimScreen(Screen parent) {
        super(new LiteralText("Auto Aim"));
        this.parent = parent;
    }

    private static AutoAimConfig cfg() {
        return AutoAimClient.config;
    }

    private static Text toggleText() {
        return new LiteralText("Auto Aim: " + (cfg().enabled ? "ВКЛ" : "ВЫКЛ"));
    }

    private static Text targetText() {
        return new LiteralText("Цель: " + cfg().target.label);
    }

    @Override
    protected void init() {
        int x = width / 2 - 100;
        int y = height / 4;

        addButton(new ButtonWidget(x, y, 200, 20, toggleText(), b -> {
            cfg().enabled = !cfg().enabled;
            b.setMessage(toggleText());
        }));

        addButton(new ValueSlider(x, y + 28, 200, 20,
                AutoAimConfig.MIN_RANGE, AutoAimConfig.MAX_RANGE, cfg().range,
                "Дальность: ", " бл.", v -> cfg().range = v));

        addButton(new ValueSlider(x, y + 56, 200, 20,
                AutoAimConfig.MIN_SPEED, AutoAimConfig.MAX_SPEED, cfg().speed,
                "Скорость: ", "°/тик", v -> cfg().speed = v));

        addButton(new ButtonWidget(x, y + 84, 200, 20, targetText(), b -> {
            cfg().target = cfg().target.next();
            b.setMessage(targetText());
        }));

        addButton(new ButtonWidget(x, y + 120, 200, 20, new LiteralText("Готово"), b -> onClose()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, title, width / 2, height / 4 - 20, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        cfg().save();
        client.openScreen(parent);
    }

    private static class ValueSlider extends SliderWidget {
        interface Setter {
            void set(double v);
        }

        private final double min, max;
        private final String prefix, suffix;
        private final Setter setter;

        ValueSlider(int x, int y, int w, int h, double min, double max, double current,
                    String prefix, String suffix, Setter setter) {
            super(x, y, w, h, LiteralText.EMPTY, (current - min) / (max - min));
            this.min = min;
            this.max = max;
            this.prefix = prefix;
            this.suffix = suffix;
            this.setter = setter;
            updateMessage();
        }

        private double current() {
            return min + value * (max - min);
        }

        @Override
        protected void updateMessage() {
            setMessage(new LiteralText(prefix + String.format("%.1f", current()) + suffix));
        }

        @Override
        protected void applyValue() {
            setter.set(current());
        }
    }
}
