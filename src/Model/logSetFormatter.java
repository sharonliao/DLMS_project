package Model;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class logSetFormatter extends Formatter{

	@Override
	public String format(LogRecord record) {
		// TODO Auto-generated method stub
		return "[" + new Date() + "]" + " [" + record.getLevel() + "] " + record.getMessage() + "\n";
	}

	

}
