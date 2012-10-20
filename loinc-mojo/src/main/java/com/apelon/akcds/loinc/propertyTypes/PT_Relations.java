package com.apelon.akcds.loinc.propertyTypes;

/**
 * @author Daniel Armbrust
 * 
 */
public class PT_Relations extends PropertyType
{
	public PT_Relations(String uuidRoot)
	{
		super("Relation Types", uuidRoot);
		addPropertyName("MAP_TO");
		
		//Used for tree building
		addPropertyName("Multiaxial Child Of");
	}
}
