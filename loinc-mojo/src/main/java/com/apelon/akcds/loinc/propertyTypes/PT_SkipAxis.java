package com.apelon.akcds.loinc.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Skip;


/**
 * Properties which have special handling during the conversion, and should not be loaded
 * the same way that other properties are handled.
 * @author Daniel Armbrust
 */
public class PT_SkipAxis extends BPT_Skip
{
	public PT_SkipAxis(String uuidRoot)
	{
		super("LOINC Axis", uuidRoot);
		//axis 1 - 6
		addProperty("COMPONENT");
		addProperty("PROPERTY");
		addProperty("TIME_ASPCT");
		addProperty("SYSTEM");
		addProperty("SCALE_TYP");
		addProperty("METHOD_TYP");

	}
}
