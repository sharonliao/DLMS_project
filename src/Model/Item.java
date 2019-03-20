package Model;

public class Item {

    private String itemID;
    private String itemName;
    private int quantity;

    public Item(String itemId, String itemName, int quantity) {
        this.itemID = itemId;
        this.itemName = itemName;
        this.quantity = quantity;

    }

    public String getItemId(){
        return itemID;
    }

    public String getItemName(){
        return itemName;
    }

    public int getQuantity(){
        return quantity;
    }

    public void setItemId(String Id){
        itemID = Id;
    }

    public void setItemName(String name){
        itemName = name;
    }

    public void setQuatity(int quantity){
        this.quantity = quantity;
    }
    public void returnOneItem(){
        this.quantity++;
    }
    public void lendOneItem(){
        this.quantity--;
    }

    public String toString(){
        String itemInfo = itemID + ", "+itemName+", "+quantity;
        return itemInfo;
    }
}
