package ReplicaHost2;

<<<<<<< HEAD
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;
=======
public class DLMSImp {
>>>>>>> master

import Model.Item;

public class DLMSImp {

	public HashMap<String, Item> map;
	public HashMap<String, List<String>> UserBorrow;
	public Map<String, Queue<String>> WaitList;
	public HashMap<String, Item> temp;
	public Logger log;
	public String library;

	String answer = "";

	public DLMSImp(String library, Logger log) {
		this.library = library;
		this.log = log;
		map = new HashMap<>();
		temp = new HashMap<>();
		UserBorrow = new HashMap<>();
		WaitList = new HashMap<>();

		if (library.equals("CON")) {
			map.put("CON1111", new Item("CON1111", "Test for Concordia", 3));
			map.put("CON2222", new Item("CON2222", "Math", 5));
			map.put("CON3333", new Item("CON3333", "French", 1));

		} else if (library.equals("MCG")) {
			map.put("MCG1111", new Item("MCG1111", "Test for Mcgill", 8));
			map.put("MCG2222", new Item("MCG2222", "Math", 1));
			map.put("MCG3333", new Item("MCG3333", "French", 1));

		} else if (library.equals("MON")) {
			map.put("MON1111", new Item("MON1111", "Test for Mcgill", 5));
			map.put("MON2222", new Item("MON2222", "Math", 2));
			map.put("MON3333", new Item("MON3333", "French", 1));
		}

	}
	
	
	

}
