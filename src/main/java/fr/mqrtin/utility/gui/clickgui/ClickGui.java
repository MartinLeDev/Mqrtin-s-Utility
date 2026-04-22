
package fr.mqrtin.utility.gui.clickgui;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.mqrtin.utility.Main;
import fr.mqrtin.utility.module.ModuleCategory;
import fr.mqrtin.utility.module.impl.Module;
import fr.mqrtin.utility.gui.clickgui.components.CategoryComponent;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClickGui extends GuiScreen {
    private static ClickGui instance;
    private final File configFile = new File("./config/Utility/", "clickgui.txt");
    private final ShowCategoryComponent categoriesComponentInstance;
    private final ArrayList<CategoryComponent> categoryList;

    public ArrayList<CategoryComponent> getCategoryList() {
        return categoryList;
    }

    public ClickGui() {
        instance = this;

        // Créer le répertoire de config s'il n'existe pas
        configFile.getParentFile().mkdirs();

        HashMap<ModuleCategory, List<Module>> categories = new HashMap<>();

        for (ModuleCategory value : ModuleCategory.values()) {
            categories.put(value, new ArrayList<>());
        }


        Set<Module> registered = new HashSet<>();
        Main.moduleManager.modules.forEach((moduleClass, moduleInstance) -> {
            if (!moduleInstance.isHidden()) {
                categories.get(moduleInstance.getCategory()).add(moduleInstance);
            }
            registered.add(moduleInstance);
        });

        this.categoriesComponentInstance = new ShowCategoryComponent();

        this.categoryList = new ArrayList<>();
        AtomicInteger topOffset = new AtomicInteger(5);

        Comparator<Module> comparator = Comparator.comparing(m -> m.getModuleName().toLowerCase());
        categories.forEach((ModuleCategory category, List<Module> modules) -> {
            if(modules.isEmpty()) return;
            modules.sort(comparator);

            CategoryComponent moduleCategory = new CategoryComponent(category.name(), modules, category);
            moduleCategory.setY(topOffset.getAndAdd(20));
            categoryList.add(moduleCategory);
        });
        categoryList.add(categoriesComponentInstance);

        for (Module module : Main.moduleManager.modules.values()) {
            if (!registered.contains(module)) {
                throw new RuntimeException(module.getClass().getName() + " is unregistered to click gui.");
            }
        }

        loadPositions();
    }

    public static ClickGui getInstance() {
        return instance;
    }

    public void initGui() {
        super.initGui();
    }

    public void drawScreen(int x, int y, float p) {
        drawRect(0, 0, this.width, this.height, 0x64000000);

        mc.fontRendererObj.drawStringWithShadow("Utility by Mqrtin", 4, this.height - 3 - mc.fontRendererObj.FONT_HEIGHT, 0xFFFFFF);

        for (CategoryComponent category : categoryList) {
            if(!(category instanceof ShowCategoryComponent) && !(this.categoriesComponentInstance.isCategoryShowed(category.category)))
                continue;
            category.render(this.fontRendererObj);
            category.handleDrag(x, y);

            for (Component module : category.getModules()) {
                module.update(x, y);
            }
        }

        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            int scrollDir = wheel > 0 ? 1 : -1;
            for (CategoryComponent category : categoryList) {
                category.onScroll(x, y, scrollDir);
            }
        }
    }

    public void mouseClicked(int x, int y, int mouseButton) {
        Iterator<CategoryComponent> btnCat = categoryList.iterator();
        while (true) {
            CategoryComponent category;
            do {
                do {
                    if (!btnCat.hasNext()) {
                        return;
                    }

                    category = btnCat.next();
                    if (category.insideArea(x, y) && !category.isHovered(x, y) && !category.mousePressed(x, y) && mouseButton == 0) {
                        category.mousePressed(true);
                        category.xx = x - category.getX();
                        category.yy = y - category.getY();
                    }

                    if (category.mousePressed(x, y) && mouseButton == 0) {
                        category.setOpened(!category.isOpened());
                    }

                    if (category.isHovered(x, y) && mouseButton == 0) {
                        category.setPin(!category.isPin());
                    }
                } while (!category.isOpened());
            } while (category.getModules().isEmpty());

            for (Component c : category.getModules()) {
                c.mouseDown(x, y, mouseButton);
            }
        }

    }

    public void mouseReleased(int x, int y, int mouseButton) {
        Iterator<CategoryComponent> iterator = categoryList.iterator();

        CategoryComponent categoryComponent;
        while (iterator.hasNext()) {
            categoryComponent = iterator.next();
            if (mouseButton == 0) {
                categoryComponent.mousePressed(false);
            }
        }

        iterator = categoryList.iterator();

        while (true) {
            do {
                do {
                    if (!iterator.hasNext()) {
                        return;
                    }

                    categoryComponent = iterator.next();
                } while (!categoryComponent.isOpened());
            } while (categoryComponent.getModules().isEmpty());

            for (Component component : categoryComponent.getModules()) {
                component.mouseReleased(x, y, mouseButton);
            }
        }
    }

    public void keyTyped(char typedChar, int key) {
        if (key == 1) {
            this.mc.displayGuiScreen(null);
        } else {
            Iterator<CategoryComponent> btnCat = categoryList.iterator();

            while (true) {
                CategoryComponent cat;
                do {
                    do {
                        if (!btnCat.hasNext()) {
                            return;
                        }

                        cat = btnCat.next();
                    } while (!cat.isOpened());
                } while (cat.getModules().isEmpty());

                for (Component component : cat.getModules()) {
                    component.keyTyped(typedChar, key);
                }
            }
        }
    }

    public void onGuiClosed() {
        savePositions();
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    private void savePositions() {
        try {
            // Créer le répertoire s'il n'existe pas
            configFile.getParentFile().mkdirs();

            JsonObject json = new JsonObject();
            for (CategoryComponent cat : categoryList) {
                JsonObject pos = new JsonObject();
                pos.addProperty("x", cat.getX());
                pos.addProperty("y", cat.getY());
                pos.addProperty("open", cat.isOpened());

                // Pour ShowCategoryComponent, sauvegarder aussi l'état de chaque catégorie
                if (cat instanceof ShowCategoryComponent) {
                    JsonObject categoriesState = new JsonObject();
                    for (ModuleCategory category : ModuleCategory.values()) {
                        boolean isShowed = ((ShowCategoryComponent) cat).isCategoryShowed(category);
                        categoriesState.addProperty(category.name(), isShowed);
                    }
                    pos.add("categories", categoriesState);
                }

                json.add(cat.getName(), pos);
            }
            try (FileWriter writer = new FileWriter(configFile)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPositions() {
        if (!configFile.exists()) return;
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            for (CategoryComponent cat : categoryList) {
                if (json.has(cat.getName())) {
                    JsonObject pos = json.getAsJsonObject(cat.getName());
                    cat.setX(pos.get("x").getAsInt());
                    cat.setY(pos.get("y").getAsInt());
                    if (pos.has("open")) {
                        cat.setOpened(pos.get("open").getAsBoolean());
                    }

                    // Pour ShowCategoryComponent, charger aussi l'état de chaque catégorie
                    if (cat instanceof ShowCategoryComponent && pos.has("categories")) {
                        JsonObject categoriesState = pos.getAsJsonObject("categories");
                        for (ModuleCategory category : ModuleCategory.values()) {
                            if (categoriesState.has(category.name())) {
                                boolean isShowed = categoriesState.get(category.name()).getAsBoolean();
                                // Trouver le CustomModule pour cette catégorie et le set enabled
                                java.util.Optional<Module> moduleOpt = cat.modulesInCategory.stream()
                                    .filter(m -> m.getCategory() == category)
                                    .findFirst();
                                if (moduleOpt.isPresent()) {
                                    moduleOpt.get().setEnabled(isShowed);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
