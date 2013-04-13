package com.apelon.akcds.loinc.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Skip;


/**
 * Properties which have special handling during the conversion, and should not be loaded
 * the same way that other properties are handled.
 * @author Daniel Armbrust
 */
public class PT_SkipClass extends BPT_Skip
{
	public PT_SkipClass()
	{
		super("LOINC Class");
		
		//special handling
		addProperty("CLASS");
	}
}
