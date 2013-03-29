package com.apelon.akcds.loinc;

import gov.va.oia.terminology.converters.sharedUtils.ConsoleUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

/**
 * Reads in a file where key and value simply alternate, one per line.
 * Ignores lines starting with "#".
 * 
 * Matching is case insensitive.
 * 
 * @author Daniel Armbrust
 *
 */

public class NameMap
{
	private Hashtable<String, String> map_ = new Hashtable<String, String>();
	
	public NameMap(String mapFileName) throws IOException
	{
		ConsoleUtil.println("Using the class map file " + mapFileName);
		BufferedReader in = new BufferedReader(new InputStreamReader(NameMap.class.getResourceAsStream("/" + mapFileName)));
		
		String key = null;
		String value = null;
		for (String str = in.readLine(); str != null; str = in.readLine())
		{
			String temp = str.trim();
			if (temp.length() > 0 && !temp.startsWith("#"))
			{
				if (key == null)
				{
					key = temp;
				}
				else
				{
					value = temp;
				}
			}
			if (value != null)
			{
				String old = map_.put(key.toLowerCase(), value);
				if (old != null)
				{
					ConsoleUtil.printErrorln("Map file " + mapFileName + " has dupliate definition for " + key);
				}
				key = null;
				value = null;
			}
		}
		in.close();
	}
	
	public boolean hasMatch(String key)
	{
		return map_.containsKey(key.toLowerCase());
	}
	
	/**
	 * Returns the replacement value, or, if none, the value you passed in.
	 * @param key
	 * @return
	 */
	public String getMatchValue(String key)
	{
		String result = map_.get(key.toLowerCase());
		return (result == null ? key : result);
	}
}
