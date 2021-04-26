package xyz.mamotromico.kbot;

import javax.script.ScriptEngine;

public final class KREPL {
    
    private static KREPL INSTANCE;
    private ScriptEngine engine;
    
    private KREPL() {
    
    }
    
    public static KREPL getInstance() {
    	if (INSTANCE == null) {
    	    INSTANCE = new KREPL();
	}
    	return INSTANCE;
    }
    
}
