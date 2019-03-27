package ReplicaHost3;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class Book implements Cloneable{
	String itemID;
	String itemName;
	volatile Quantity quantity;

	public String getitemID() {
		return itemID;
	}

	public String getBookName() {
		return itemName;
	}

	public Quantity getQuantity() {
		return quantity;
	}

	public void setQuantity(Quantity quantity) {
		quantity_updater.set(this, quantity);
	}

	public Book(String itemID, String itemName, int quantity) {
		super();
		this.itemID = itemID;
		this.itemName = itemName;
		this.quantity = new Quantity();
		this.quantity.setQuantity(quantity);
	}
	
	private AtomicReferenceFieldUpdater<Book, Quantity> quantity_updater =
			AtomicReferenceFieldUpdater.newUpdater(Book.class, Quantity.class, "quantity");

	@Override
	public String toString() {
		return "Book [bookno=" + itemID + ", bookName=" + itemName + ", quantity=" + quantity + "]";
	}
	@Override
	public Book clone()
    {
		Book clone = null;
		try{ 
            clone = (Book) super.clone(); 
 
        }catch(CloneNotSupportedException e){ 
            throw new RuntimeException(e); // won't happen 
        }
         
        return clone; 
    }
}