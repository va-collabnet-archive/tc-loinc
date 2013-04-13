package com.apelon.akcds.loinc.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.EConceptUtility;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Relations;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.Property;

/**
 * @author Daniel Armbrust
 * 
 */
public class PT_Relations extends BPT_Relations
{
	public PT_Relations()
	{
		super("LOINC");
		addProperty("MAP_TO");
		
		//Used for tree building
		addProperty(new Property("Multiaxial Child Of", null, null, EConceptUtility.isARelUuid_));
	}
}
