package com.apelon.akcds.loinc.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Relations;

/**
 * @author Daniel Armbrust
 * 
 */
public class PT_Relations extends BPT_Relations
{
	public PT_Relations(String uuidRoot)
	{
		super(uuidRoot);
		addProperty("MAP_TO");
		
		//Used for tree building
		addProperty("Multiaxial Child Of");
	}
}
