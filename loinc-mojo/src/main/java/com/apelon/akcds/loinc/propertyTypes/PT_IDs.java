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
		addPropertyName("LOINC_NUM");
		addPropertyName("NAACCR_ID");
		
		//Abbrev Codes used by axis and class
		addPropertyName("ABBREVIATION");
		
		//From multi-axial class
		addPropertyName("PATH_TO_ROOT");
		addPropertyName("CODE");
		
	}
}
