package fr.mqrtin.utility.module.modules.hidden;

import fr.mqrtin.utility.Main;
import fr.mqrtin.utility.event.EventTarget;
import fr.mqrtin.utility.event.events.LeftClickMouseEvent;
import fr.mqrtin.utility.event.events.RightClickMouseEvent;
import fr.mqrtin.utility.module.ModuleCategory;
import fr.mqrtin.utility.module.impl.Module;

import java.util.ArrayList;
import java.util.HashMap;

public class CPSCounter extends Module{

    private final ArrayList<Long> leftClicks = new ArrayList<>();
    private final ArrayList<Long> rightClicks = new ArrayList<>();

    public CPSCounter() {
        super("CPSCounter", ModuleCategory.HIDDEN, false);
        setForceEnabled(true);
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @EventTarget
    public void onLeftClick(LeftClickMouseEvent event){
        leftClicks.add(System.currentTimeMillis());
        leftClicks.removeIf(click -> click < System.currentTimeMillis() - 1000);
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent event){
        rightClicks.add(System.currentTimeMillis());
        rightClicks.removeIf(click -> click < System.currentTimeMillis() - 1000);
    }

    public static int getLeftCPS(){
        CPSCounter cpsCounter = (CPSCounter) Main.moduleManager.modules.get(CPSCounter.class);
        cpsCounter.leftClicks.removeIf(click -> click < System.currentTimeMillis() - 1000);

        return cpsCounter.leftClicks.size();
    }

    public static int getRightCPS() {
        CPSCounter cpsCounter = (CPSCounter) Main.moduleManager.modules.get(CPSCounter.class);
        cpsCounter.rightClicks.removeIf(click -> click < System.currentTimeMillis() - 1000);

        return cpsCounter.rightClicks.size();
    }

}
