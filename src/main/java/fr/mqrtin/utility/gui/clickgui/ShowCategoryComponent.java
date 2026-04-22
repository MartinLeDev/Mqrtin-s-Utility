package fr.mqrtin.utility.gui.clickgui;

import fr.mqrtin.utility.module.ModuleCategory;
import fr.mqrtin.utility.module.impl.Module;
import fr.mqrtin.utility.gui.clickgui.components.CategoryComponent;

import java.util.ArrayList;
import java.util.Optional;

public class ShowCategoryComponent extends CategoryComponent {
    public ShowCategoryComponent(){
        super("Categories", new ArrayList<>(), null);
        initCategoryModules();
    }

    private void initCategoryModules() {
        modulesInCategory.clear();
        for (ModuleCategory value : ModuleCategory.values()) {
            if(value.equals(ModuleCategory.HIDDEN)) continue;
            modulesInCategory.add(new CustomModule(value));
        }
        updateModules();
    }

    public boolean isCategoryShowed(ModuleCategory category){
        Optional<Module> first = modulesInCategory.stream().filter(module -> module.getCategory() == category).findFirst();
        if(!first.isPresent()){
            return false;
        }
        Module module = first.get();

        return module.isEnabled();
    }

    @Override
    public void update() {
        super.update();
    }

    public static class CustomModule extends Module {
        private final ModuleCategory category;

        public CustomModule(ModuleCategory category){
            super(category.name(), category, false);
            this.category = category;
        }

        @Override
        public ModuleCategory getCategory() {
            return category;
        }
    }
}
