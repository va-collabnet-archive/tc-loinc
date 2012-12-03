package com.apelon.akcds.loinc.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Skip;


/**
 * Properties which have special handling during the conversion, and should not be loaded
 * the same way that other properties are handled.
 * @author Daniel Armbrust
 */
public class PT_SkipOther extends BPT_Skip
{
	public PT_SkipOther(String uuidRoot)
	{
		super("Skip Other", uuidRoot);
		
		//Not Loaded
		addPropertyName("SOURCE");
		addDisabledPropertyName("FINAL", 0, 1);	//deleted in 2.38
	}
}
