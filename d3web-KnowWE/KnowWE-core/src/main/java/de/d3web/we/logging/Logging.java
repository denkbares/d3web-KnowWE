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
        this.logger = this.getLogger(this.getClass());
        this.addHandlerToLogger(logger, "Logging.log");
    }


    public static Logging getInstance() {
        return instance;
    }
    
    
    public void log(Class class1, Level level, String message) {
        log(class1, level, message, null, null);
    }
    
    
    public void log(Class class1, Level level, String message, String plugin) {
        log(class1, level, message, null, plugin);    
    }

    /**
     * gets the Logger of the specified class, logs a message with the specified level
     * and an optional user or plugin. If user is null, only the message and the plugin
     * will be logged, if plugin is null only the message and the user will be logged.
     * If both are null only the message will be logged.
     * It is recommended to add the class and the method to the message, as the call
     * to the logger comes from this class and this method.
     */
    public void log(Class class1, Level level, String message, String user,
			String plugin) {
    	
    	// build the "extended" message
    	if (user != null) {
		message = message + LINE_SEPARATOR + "user: " + user;	
    	} 
    	
    	if (plugin != null) {
    		message = message + LINE_SEPARATOR + "plugin: " + plugin;
    	}
    	
    	// log the message
    	getLogger(class1).log(level, message);   		
	}

    /**
     * gets the Logger of a specified class
     */
    public Logger getLogger(Class class1) {
        String name = class1.getCanonicalName();
        
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
        // only add the logger if its needed
        if (log.getHandlers().length == 0) {
            
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
    
    
    public void addHandlerToMap(String filename) {
    	
    	// if no FileHandler with the specified filename is in the map
    	// add a new one
        if (handlerMap.get(filename) == null) {
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
    

}