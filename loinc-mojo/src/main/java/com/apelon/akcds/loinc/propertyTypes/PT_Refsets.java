package com.apelon.akcds.loinc.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Refsets;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.Property;

public class PT_Refsets extends BPT_Refsets
{
	public enum Refsets
	{
		ALL("All LOINC Concepts");

		private Property property;

		private Refsets(String niceName)
		{
			// Don't know the owner yet - will be autofilled when we add this to the parent, below.
			property = new Property(null, niceName);
		}

		public Property getProperty()
		{
			return property;
		}
	}

	public PT_Refsets()
	{
		super("LOINC");
		for (Refsets mm : Refsets.values())
		{
			addProperty(mm.getProperty());
		}
	}
}
