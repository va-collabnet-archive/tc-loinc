package com.apelon.akcds.loinc.propertyTypes;


/**
 * Invented property with special handling for root node in workbench.
 * @author Daniel Armbrust
 */
public class PT_ContentVersion extends PropertyType
{
	public PT_ContentVersion(String uuidRoot)
	{
		super("Content Version", uuidRoot);
		addPropertyName("version");
		addPropertyName("releaseDate");
	}
}
