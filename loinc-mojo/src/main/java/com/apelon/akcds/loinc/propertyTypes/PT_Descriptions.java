package com.apelon.akcds.loinc.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Descriptions;


/**
 * Fields to treat as descriptions
 * 
 * @author Daniel Armbrust
 *
 */
public class PT_Descriptions extends BPT_Descriptions
{
	public PT_Descriptions()
	{
		super("LOINC");

		addProperty("CONSUMER_NAME", SYNONYM + 1);
		addProperty("EXACT_CMP_SY", null, null, 0, 1, false, SYNONYM + 1);		//deleted in 2.38
		addProperty("ACSSYM", SYNONYM + 1);
		addProperty("BASE_NAME", SYNONYM + 1);
		addProperty("SHORTNAME", SYNONYM);			//typically preferred synonym.
		addProperty("LONG_COMMON_NAME", FSN);		//this should be the FSN, unless missing, then work down the synonym hierarchy
		
		//from multiaxial
		addProperty("CODE_TEXT", FSN);
	}
}
