package ReplicaHost3;

public class Record {
	String itemID;
	String itemName;
	String time;
	String userID;
	public String status;
	
	public String getitemID() {
		return itemID;
	}


	public String getBookName() {
		return itemName;
	}
	
	public String getUserID() {
		return userID;
	}

	public String getTime() {
		return time;
	}
	
	public void setStatus() {
		this.status = "Returned";
	}
	
	public void rollbackStatus() {
		this.status = "Borrowed";
	}
	
	public String getStatus() {
		return status;
	}

	public Record(String itemID, String itemName, String time, String status) {
		super();
		this.itemID = itemID;
		this.itemName = itemName;
		this.time = time;
		this.status = status;
	}
	public Record(String userID, String time, String status) {
		super();
		this.userID = userID;
		this.time = time;
		this.status = status;
	}
	public String toString() {
		return "Record [itemID=" + itemID + ", time=" + time + ", status=" + status + "]";
	}
	
}