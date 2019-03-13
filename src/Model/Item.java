package Model;

public class Item {

    private String itemID;
    private String itemName;
    private int quatity;

    public Item(String itemId, String itemName, int quantity) {
        this.itemID = itemId;
        this.itemName = itemName;
        this.quatity = quantity;

    }

    public String getItemId(){
        return itemID;
    }

    public String getItemName(){
        return itemName;
    }

    public int getQuatity(){
        return quatity;
    }

    public void setItemId(String Id){
        itemID = Id;
    }

    public void setItemName(String name){
        itemName = name;
    }

    public void setQuatity(int quantity){
        this.quatity = quantity;
    }
    public void returnOneItem(){
        this.quatity++;
    }
    public void lendOneItem(){
        this.quatity--;
    }

    public String toString(){
        String itemInfo = itemID + ", "+itemName+", "+quatity;
        return itemInfo;
    }
}
