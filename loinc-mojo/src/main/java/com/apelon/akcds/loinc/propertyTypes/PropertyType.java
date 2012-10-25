package com.apelon.akcds.loinc.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.stats.ConverterUUID;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Abstract base class to help in mapping DTS property types into the workbench data model.
 * 
 * The main purpose of this structure is to keep the UUID generation sane across the various
 * places where UUIDs are needed in the workbench.
 *  
 * @author Daniel Armbrust
 */

public abstract class PropertyType
{
	private String propertyTypeDescription_;
	private Set<String> propertyNames_;
	private String uuidRoot_;
	private HashSet<String> disabledTypes_ = new HashSet<String>();
	protected static int srcVersion_ = 1;
	
	public static void setSourceVersion(int version)
	{
		srcVersion_ = version;
	}
	
	protected PropertyType(String propertyTypeDescription, String uuidRoot)
	{
		this.propertyNames_ = new HashSet<String>();
		this.propertyTypeDescription_ = propertyTypeDescription;
		this.uuidRoot_ = uuidRoot;
	}
	
	public UUID getPropertyUUID(String propertyName)
	{
		return ConverterUUID.nameUUIDFromBytes((uuidRoot_ + propertyTypeDescription_ + ":" + propertyName).getBytes());
	}
	
	public UUID getPropertyTypeUUID()
	{
		return ConverterUUID.nameUUIDFromBytes((uuidRoot_ + propertyTypeDescription_).getBytes());
	}
	
	public String getPropertyTypeDescription()
	{
		return propertyTypeDescription_;
	}
	
	public Set<String> getPropertyNames()
	{
		return propertyNames_;
	}
	
	public boolean containsProperty(String propertyName)
	{
		return propertyNames_.contains(propertyName);
	}
	
	/**
	 * Only adds the property if the version of the data file falls between min and max, inclusive.
	 * pass 0 in min or max to specify no min or no max, respectively
	 */
	protected void addPropertyName(String propertyName, int minVersion, int maxVersion)
	{
		if ((minVersion != 0 && srcVersion_ < minVersion) 
				|| (maxVersion != 0 && srcVersion_ > maxVersion)) 
		{
			return;
		}
		propertyNames_.add(propertyName);
	}
	
	public void addPropertyName(String propertyName)
	{
		propertyNames_.add(propertyName);
	}
	
	protected void addDisabledPropertyName(String propertyName)
	{
		propertyNames_.add(propertyName);
		disabledTypes_.add(propertyName);
	}
	
	/**
	 * Only adds the property if the version of the data file falls between min and max, inclusive.
	 * pass 0 in min or max to specify no min or no max, respectively
	 */
	protected void addDisabledPropertyName(String propertyName, int minVersion, int maxVersion)
	{
		if ((minVersion != 0 && srcVersion_ < minVersion) 
				|| (maxVersion != 0 && srcVersion_ > maxVersion)) 
		{
			return;
		}
		propertyNames_.add(propertyName);
		disabledTypes_.add(propertyName);
		
	}
	
	public boolean isDisabled(String propertyName)
	{
		return disabledTypes_.contains(propertyName);
	}
	
	
	/**
	 * Default impl just returns what they passed in.  Real implementing classes may choose to override this.
	 */
	public String getPropertyFriendlyName(String propertyName)
	{
		return propertyName;
	}
	
}
