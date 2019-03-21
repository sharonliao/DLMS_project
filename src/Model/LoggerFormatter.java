package Model;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LoggerFormatter extends Formatter{

	@Override
	public String format(LogRecord record) {
		// TODO Auto-generated method stub
		//StringBuilder strBuilder = new StringBuilder();
		//String[] mess = message.split(":");
		
		return "[ " + new Date() + " ] " + " [ " + record.getLevel() + " ] " + record.getMessage() + "\n";
	}
	

}
