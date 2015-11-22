package gov.nasa.jpf.vm.va;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;;
public class Log {
	static Logger logger = null;
	public static void init() {
		logger = Logger.getLogger("DataCollect");
    	logger.setLevel(Level.INFO); 
    	FileHandler fileHandler = null;
    	try {
    		 fileHandler = new FileHandler("/home/meng/Documents/1.log");
    	} catch(IOException e) {
    	}
    	fileHandler.setLevel(Level.INFO);
    	SimpleFormatter formatter = new SimpleFormatter();
    	fileHandler.setFormatter(formatter);
    	logger.addHandler(fileHandler); 
    	logger.setUseParentHandlers(false);  
    	logger.info("Begin Crawling, Good Luck!");
	}
	public static Logger getInstance() {
		if(logger == null) {
			System.out.println("init()");
			init();
		}
		return logger;
	}
}
