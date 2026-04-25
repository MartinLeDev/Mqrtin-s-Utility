
package fr.mqrtin.utility.gui.clickgui.components;

import fr.mqrtin.utility.module.impl.property.Property;
import fr.mqrtin.utility.module.impl.property.properties.*;
import fr.mqrtin.utility.gui.clickgui.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;

public class SliderComponent implements Component {
    private final Property<?> property;
    private final ModuleComponent parentModule;
    private int offsetY;
    private int x;
    private int y;
    private boolean dragging = false;
    private double sliderWidth;
    private long increment = 0;
    private long decrement = 0;

    public SliderComponent(Property<?> property, ModuleComponent parentModule, int offsetY) {
        this.property = property;
        this.parentModule = parentModule;
        this.x = parentModule.category.getX() + parentModule.category.getWidth();
        this.y = parentModule.category.getY() + parentModule.offsetY;
        this.offsetY = offsetY;
    }

    public void draw(AtomicInteger offset) {
        Gui.drawRect(this.parentModule.category.getX() + 4, this.parentModule.category.getY() + this.offsetY + 11, this.parentModule.category.getX() + 4 + this.parentModule.category.getWidth() - 8, this.parentModule.category.getY() + this.offsetY + 15, -12302777);
        int sliderStart = this.parentModule.category.getX() + 4;
        int sliderEnd = this.parentModule.category.getX() + 4 + (int) this.sliderWidth;
        if (sliderEnd - sliderStart > 84) {
            sliderEnd = sliderStart + 84;
        }
        Gui.drawRect(sliderStart, this.parentModule.category.getY() + this.offsetY + 11, sliderEnd, this.parentModule.category.getY() + this.offsetY + 15, 0xFF00FF00);
        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(this.property.getName() + ": " + getValueString(), (float) ((int) ((float) (this.parentModule.category.getX() + 4) * 2.0F)), (float) ((int) ((float) (this.parentModule.category.getY() + this.offsetY + 3) * 2.0F)), -1);
        GL11.glPopMatrix();
    }

    private String getValueString() {
        if (property instanceof FloatProperty) {
            return String.valueOf(((FloatProperty) property).getValue());
        } else if (property instanceof IntProperty) {
            return String.valueOf(((IntProperty) property).getValue());
        } else if (property instanceof PercentProperty) {
            return String.valueOf(((PercentProperty) property).getValue()) + "%";
        }
        return "?";
    }

    private double getMinValue() {
        if (property instanceof FloatProperty) {
            return ((FloatProperty) property).getMinimum();
        } else if (property instanceof IntProperty) {
            return ((IntProperty) property).getMinimum();
        } else if (property instanceof PercentProperty) {
            return ((PercentProperty) property).getMinimum();
        }
        return 0;
    }

    private double getMaxValue() {
        if (property instanceof FloatProperty) {
            return ((FloatProperty) property).getMaximum();
        } else if (property instanceof IntProperty) {
            return ((IntProperty) property).getMaximum();
        } else if (property instanceof PercentProperty) {
            return ((PercentProperty) property).getMaximum();
        }
        return 100;
    }

    private double getCurrentValue() {
        if (property instanceof FloatProperty) {
            return ((FloatProperty) property).getValue();
        } else if (property instanceof IntProperty) {
            return ((IntProperty) property).getValue();
        } else if (property instanceof PercentProperty) {
            return ((PercentProperty) property).getValue();
        }
        return 0;
    }

    private void setCurrentValue(double value) {
        if (property instanceof FloatProperty) {
            ((FloatProperty) property).setValue((float) value);
        } else if (property instanceof IntProperty) {
            ((IntProperty) property).setValue((int) value);
        } else if (property instanceof PercentProperty) {
            ((PercentProperty) property).setValue((int) value);
        }
    }

    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    public void update(int mousePosX, int mousePosY) {
        this.y = this.parentModule.category.getY() + this.offsetY;
        this.x = this.parentModule.category.getX();

        double d = Math.min(this.parentModule.category.getWidth() - 8, Math.max(0, mousePosX - this.x));
        this.sliderWidth = (double) (this.parentModule.category.getWidth() - 8) *
                (getCurrentValue() - getMinValue()) /
                (getMaxValue() - getMinValue());

        if (this.dragging) {
            if (d == 0.0D) {
                setCurrentValue(getMinValue());
                this.parentModule.mod.verifyValue(this.property.getName());
            } else {
                double rawValue = d / (double) (this.parentModule.category.getWidth() - 8)
                        * (getMaxValue() - getMinValue())
                        + getMinValue();

                double increment = 1;
                if (property instanceof FloatProperty) {
                    increment = 0.1;
                }
                if (increment > 0) {
                    rawValue = Math.round(rawValue / increment) * increment;
                }
                double n = roundToPrecision(rawValue, 2);
                n = Math.max(getMinValue(), Math.min(getMaxValue(), n));
                setCurrentValue(n);
                this.parentModule.mod.verifyValue(this.property.getName());
            }
        }
    }


    private static double roundToPrecision(double v, int precision) {
        if (precision < 0) {
            return 0.0D;
        } else {
            BigDecimal bd = new BigDecimal(v);
            bd = bd.setScale(precision, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }
    }

    public void mouseDown(int x, int y, int button) {
        if (this.isTextHovered(x, y) && button == 0 && this.parentModule.panelExpand) {
            return;
        }

        if (this.isLeftHalfHovered(x, y) && this.parentModule.panelExpand) {
            if (button == 0) {
                this.dragging = true;
            }
        }

        if (this.isRightHalfHovered(x, y) && this.parentModule.panelExpand) {
            if (button == 0) {
                this.dragging = true;
            }
        }

    }

    public void mouseReleased(int x, int y, int button) {
        this.dragging = false;
        this.increment = 0;
        this.decrement = 0;
    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {

    }

    public boolean isTextHovered(int x, int y) {
        return x > this.x && x < this.x + this.parentModule.category.getWidth() && y > this.y && y < this.y + 8;
    }

    public boolean isLeftHalfHovered(int x, int y) {
        return x > this.x && x < this.x + this.parentModule.category.getWidth() / 2 + 1 && y > this.y + 8 && y < this.y + 16;
    }

    public boolean isRightHalfHovered(int x, int y) {
        return x > this.x + this.parentModule.category.getWidth() / 2 && x < this.x + this.parentModule.category.getWidth() && y > this.y + 8 && y < this.y + 16;
    }


    @Override
    public boolean isVisible() {
        return property.isVisible();
    }
}
