package com.apelon.akcds.loinc.propertyTypes;


/**
 * Properties which have special handling during the conversion, and should not be loaded
 * the same way that other properties are handled.
 * @author Daniel Armbrust
 */
public abstract class PT_Skip extends PropertyType
{
	public PT_Skip(String description, String uuidRoot)
	{
		super(description, uuidRoot);
	}
}
