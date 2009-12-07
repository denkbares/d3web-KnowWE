/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.logging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author Florian Ziegler
 */
public class Logging {

    private static Logging instance = new Logging();
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private Logger logger;
    private Map<String, Logger> logMap = new HashMap<String, Logger>();
    private Map<String, FileHandler> handlerMap = new HashMap<String, FileHandler>();
    
    private Logging() {
		this.logger = this.getLogger();
	}


    public static Logging getInstance() {
        return instance;
    }
    
    
        
    public void log(Level level, String message) {
        log(level, message, null, null);
    }
    
    
    public void log(Level level, String message, String plugin) {
        log(level, message, null, plugin);    
    }
    
    /**
     * !!!!!!! Way better than the simple log method !!!!!!
     * Logs the correct class and method.
     * gets the Logger of the specified class, logs a message with the specified level
     * and an optional user or plugin. If user is null, only the message and the plugin
     * will be logged, if plugin is null only the message and the user will be logged.
     * If both are null only the message will be logged.
     */
    public void log(Level level, String message, String user,
			String plugin) {
    	
    	// build the "extended" message
    	if (user != null) {
		message = message + LINE_SEPARATOR + "user: " + user;	
    	} 
    	
    	if (plugin != null) {
    		message = message + LINE_SEPARATOR + "plugin: " + plugin;
    	}
    	
    	// log the message
		getLogger().logp(level, getClassName(),
				getCurrentMethodName(), message);
	}
    
    /**
     * logs a message with Level.SEVERE
     * @param message
     */
    public void severe(String message) {
    	log(Level.SEVERE, message);
    }

    /**
     * logs a message with WARNING
     * @param message
     */
    public void warning(String message) {
		log(Level.WARNING, message);
    }

    /**
     * logs a message with Level.INFO
     * @param message
     */
    public void info(String message) {
    	log(Level.INFO, message);
    }

    /**
     * logs a message with Level.CONFIG
     * @param message
     */
    public void config(String message) {
    	log(Level.CONFIG, message);
    }

    /**
     * logs a message with Level.FINE
     * @param message
     */
    public void fine(String message) {
		log(Level.FINE, message);
    }

    /**
     * logs a message with Level.FINER
     * @param message
     */
    public void finer(String message) {
    	log(Level.FINER, message);
    }

    /**
     * logs a message with Level.FINEST
     * @param message
     */
    public void finest(String message) {
    	log(Level.FINEST, message);
    }

    /**
     * gets the Logger of a specified class
     */
    public Logger getLogger() {
        String name = getClassName();
        
        // if the logger is already in the map
        if (logMap.containsKey(name)) {
            return logMap.get(name);
            
        // if not, create it and add it to the map    
        } else {
            Logger l = Logger.getLogger(name);
            logMap.put(name, l);
            return l;
        }

    }
       
    /**
     * adds a FileHandler with an output file named filename and a consoleHandler
     * with Level.SEVERE to the Logger log. Also disables parenthandlers.
     */
    public void addHandlerToLogger(Logger log, String filename) {
    	boolean alreadyHasHandler = false;
    	
    	// check if the Logger already has that FileHandler
    	if (handlerMap.containsKey(filename)) {
    		for (Handler h : log.getHandlers()) {
    			if (h.equals(handlerMap.get(filename))) {
    				alreadyHasHandler = true;
    			}
    		}
    	}
    	
        // only add the logger if its needed
        if (!alreadyHasHandler) {
            
            // if a FileHandler with the filename is already in the map
            if (handlerMap.containsKey(filename)) {
                log.addHandler(handlerMap.get(filename));
                
            // if not, create it
            } else {
                addHandlerToMap(filename);
                log.addHandler(handlerMap.get(filename));
            }
            
            // ConsoleHandler
            ConsoleHandler ch = new ConsoleHandler();
            ch.setLevel(Level.SEVERE);
            log.addHandler(ch);
            
            // only specified handlers will be used, no parent handler
            log.setUseParentHandlers(false);

        }
    }
    
    
    private void addHandlerToMap(String filename) {
    	
    	// if no FileHandler with the specified filename is in the map
    	// add a new one
        if (!handlerMap.containsKey(filename)) {
            FileHandler fh = null;
            try {
                fh = new FileHandler(filename);
            } catch (SecurityException e) {
                logger.log(Level.WARNING, "Security Exception with " + filename);
            } catch (IOException e) {
                logger.log(Level.WARNING, "IOException with " + filename);
            }
            
            // set the formatting of the filehandler
            fh.setFormatter(new SimpleFormatter());
            handlerMap.put(filename, fh);

        }
    }
    
    /**
     * returns the name of the current method via stack trace
     * @return
     */
	private String getCurrentMethodName() {

		String methodName = "";
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		
		for (int i = 2; i < stackTrace.length; i++) {
			if (!stackTrace[i].getMethodName().equals("log")) {
				methodName = stackTrace[i].getMethodName();

				break;
			}
		}
		return methodName;
	}
	
    /**
     * returns the name of the current class via stack trace
     * @return
     */
	private String getClassName() {

		String className = "";
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		
		for (int i = 2; i < stackTrace.length; i++) {
			if (!stackTrace[i].getClassName().equals(
					"de.d3web.we.logging.Logging")) {
				className = stackTrace[i].getClassName();
				break;
			}
		}
		return className;
	}
    

}