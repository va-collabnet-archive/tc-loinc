package com.apelon.akcds.loinc.propertyTypes;


/**
 * Properties which have special handling during the conversion, and should not be loaded
 * the same way that other properties are handled.
 * @author Daniel Armbrust
 */
public class PT_SkipClass extends PT_Skip
{
	public PT_SkipClass(String uuidRoot)
	{
		super("LOINC Class", uuidRoot);
		
		//special handling
		addPropertyName("CLASS");
	}
}
