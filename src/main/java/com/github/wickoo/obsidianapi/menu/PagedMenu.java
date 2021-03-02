package com.github.wickoo.obsidianapi.menu;

public abstract class PagedMenu extends Menu {

    public void initInventory(MenuManager menuManager) { super.initInventory(menuManager); }

    public abstract void setBackButton();

    public abstract void setForwardButton();

    public abstract int getCurrentPage();

    public abstract int getMaxPage();

}

