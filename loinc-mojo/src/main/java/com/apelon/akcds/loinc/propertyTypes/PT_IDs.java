package com.apelon.akcds.loinc.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_IDs;


/**
 * Fields to treat as IDs
 * @author Daniel Armbrust
 */
public class PT_IDs extends BPT_IDs
{
	public PT_IDs(String uuidRoot)
	{
		super(uuidRoot);
		addProperty("LOINC_NUM");
		addProperty("NAACCR_ID");
		
		//Abbrev Codes used by axis and class
		addProperty("ABBREVIATION");
		
		//From multi-axial class
		addProperty("PATH_TO_ROOT");
		addProperty("CODE");
		
	}
}
