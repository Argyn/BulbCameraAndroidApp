package navigation_drawer;

/**
 * Created by argyn on 25/07/2014.
 */
public class DrawerItem {

    private String itemName;
    private int imageResourceID;
    private boolean active;

    public DrawerItem(String itemName, int imageResourceID) {
        this.itemName = itemName;
        this.imageResourceID = imageResourceID;
        active = false;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getImageResourceID() {
        return imageResourceID;
    }

    public void setImageResourceID(int imageResourceID) {
        this.imageResourceID = imageResourceID;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
