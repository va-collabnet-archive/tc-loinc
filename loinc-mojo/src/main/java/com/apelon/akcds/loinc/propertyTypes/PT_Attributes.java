package com.apelon.akcds.loinc.propertyTypes;


/**
 * Fields to treat as attributes
 * @author Daniel Armbrust
 */
public class PT_Attributes extends PropertyType
{
	public PT_Attributes(String uuidRoot)
	{
		super("Attribute Types", uuidRoot);
		
		addPropertyName("DT_LAST_CH", 0, 1);  //replaced with DATE_LAST_CHANGED in 2.38
		addPropertyName("DATE_LAST_CHANGED", 2, 0);
		addPropertyName("CHNG_TYPE");
		addPropertyName("COMMENTS");
		addDisabledPropertyName("ANSWERLIST", 0, 1);  	//deleted in 2.38
		addDisabledPropertyName("SCOPE", 0, 1);			//deleted in 2.38
		addDisabledPropertyName("IPCC_UNITS", 0, 1);	//deleted in 2.38
		addPropertyName("REFERENCE", 0, 1);				//deleted in 2.38
		addPropertyName("MOLAR_MASS");
		addPropertyName("CLASSTYPE");
		addPropertyName("FORMULA");
		addPropertyName("SPECIES");
		addPropertyName("EXMPL_ANSWERS");
		addPropertyName("CODE_TABLE");
		addPropertyName("SETROOT", 0, 1);				//deleted in 2.38
		addDisabledPropertyName("PANELELEMENTS", 0, 1);	//deleted in 2.38
		addPropertyName("SURVEY_QUEST_TEXT");
		addPropertyName("SURVEY_QUEST_SRC");
		addPropertyName("UNITSREQUIRED");
		addPropertyName("SUBMITTED_UNITS");
		addPropertyName("ORDER_OBS");
		addPropertyName("CDISC_COMMON_TESTS");
		addPropertyName("HL7_FIELD_SUBFIELD_ID");
		addPropertyName("EXTERNAL_COPYRIGHT_NOTICE");
		addPropertyName("EXAMPLE_UNITS");
		addPropertyName("INPC_PERCENTAGE", 0, 1);		//deleted in 2.38
		addPropertyName("HL7_V2_DATATYPE");
		addPropertyName("HL7_V3_DATATYPE");
		addPropertyName("CURATED_RANGE_AND_UNITS");
		addPropertyName("DOCUMENT_SECTION");
		addPropertyName("DEFINITION_DESCRIPTION_HELP", 0, 1);	//deleted in 2.38
		addPropertyName("EXAMPLE_UCUM_UNITS");
		addPropertyName("EXAMPLE_SI_UCUM_UNITS");
		addPropertyName("STATUS_REASON");
		addPropertyName("STATUS_TEXT");
		addPropertyName("CHANGE_REASON_PUBLIC");
		addPropertyName("COMMON_TEST_RANK");
		addPropertyName("COMMON_ORDER_RANK", 2, 0);			//added in 2.38
		addPropertyName("STATUS");
		addPropertyName("COMMON_SI_TEST_RANK", 3, 0);				//added in 2.40 (or maybe 2.39, 2.39 is untested)
		
		//from multiaxial
		addPropertyName("SEQUENCE");
		addPropertyName("IMMEDIATE_PARENT");
	}
}
