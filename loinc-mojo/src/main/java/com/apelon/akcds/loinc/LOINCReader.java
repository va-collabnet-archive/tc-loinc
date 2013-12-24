package com.apelon.akcds.loinc;

import java.io.IOException;
import java.util.Hashtable;

public abstract class LOINCReader
{
	public abstract String getVersion();
	public abstract String getReleaseDate();
	public abstract String[] getHeader();
	public abstract String[] readLine() throws IOException;
	public abstract void close() throws IOException;
	
	protected int fieldCount_ = 0;
	protected Hashtable<String, Integer> fieldMap_ = new Hashtable<String, Integer>();
	protected Hashtable<Integer, String> fieldMapInverse_ = new Hashtable<Integer, String>();
	
	public Hashtable<String, Integer> getFieldMap()
	{
		return fieldMap_;
	}
	
	public Hashtable<Integer, String> getFieldMapInverse()
	{
		return fieldMapInverse_;
	}
}
