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
	public PT_Descriptions(String uuidRoot)
	{
		super( uuidRoot);

		addProperty("RELAT_NMS", null, 0, 1, true);  	//deleted in 2.38
		addProperty("CONSUMER_NAME");
		addProperty("EXACT_CMP_SY", 0, 1);			//deleted in 2.38
		addProperty("ACSSYM");
		addProperty("BASE_NAME");
		addProperty("RELATEDNAMES2");
		addProperty("SHORTNAME");
		addProperty("LONG_COMMON_NAME");
		
		//from multiaxial
		addProperty("CODE_TEXT");
	}
}
