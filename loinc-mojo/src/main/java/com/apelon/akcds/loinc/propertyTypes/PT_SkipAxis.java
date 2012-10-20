package com.apelon.akcds.loinc.propertyTypes;


/**
 * Properties which have special handling during the conversion, and should not be loaded
 * the same way that other properties are handled.
 * @author Daniel Armbrust
 */
public class PT_SkipAxis extends PT_Skip
{
	public PT_SkipAxis(String uuidRoot)
	{
		super("Loinc Axis", uuidRoot);
		//axis 1 - 6
		addPropertyName("COMPONENT");
		addPropertyName("PROPERTY");
		addPropertyName("TIME_ASPCT");
		addPropertyName("SYSTEM");
		addPropertyName("SCALE_TYP");
		addPropertyName("METHOD_TYP");

	}
}
