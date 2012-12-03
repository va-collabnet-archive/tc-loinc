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

		addDisabledPropertyName("RELAT_NMS", 0, 1);  	//deleted in 2.38
		addPropertyName("CONSUMER_NAME");
		addPropertyName("EXACT_CMP_SY", 0, 1);			//deleted in 2.38
		addPropertyName("ACSSYM");
		addPropertyName("BASE_NAME");
		addPropertyName("RELATEDNAMES2");
		addPropertyName("SHORTNAME");
		addPropertyName("LONG_COMMON_NAME");
		
		//from multiaxial
		addPropertyName("CODE_TEXT");
	}
}
