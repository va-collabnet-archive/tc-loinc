package com.apelon.akcds.loinc.propertyTypes;


/**
 * Fields to treat as IDs
 * @author Daniel Armbrust
 */
public class PT_IDs extends PropertyType
{
	public PT_IDs(String uuidRoot)
	{
		super("ID Types", uuidRoot);
		addPropertyName("LOINC_NUM");
		addPropertyName("NAACCR_ID");
		
		//Abbrev Codes used by axis and class
		addPropertyName("ABBREVIATION");
		
		//From multi-axial class
		addPropertyName("PATH_TO_ROOT");
		addPropertyName("CODE");
		
	}
}
