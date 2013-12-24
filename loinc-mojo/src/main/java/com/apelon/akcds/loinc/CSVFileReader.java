package com.apelon.akcds.loinc;

import gov.va.oia.terminology.converters.sharedUtils.ConsoleUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import org.apache.commons.io.input.BOMInputStream;
import au.com.bytecode.opencsv.CSVReader;

public class CSVFileReader extends LOINCReader
{
	String[] header;
	CSVReader reader;
	String version = null;
	String release = null;
	
	public CSVFileReader(File f) throws IOException
	{
		ConsoleUtil.println("Using the data file " + f.getAbsolutePath());
		//Their new format includes the (optional) UTF-8 BOM, which chokes java for stupid legacy reasons.
		reader = new CSVReader(new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(f)))));
		header = readLine();
		
		readReleaseNotes(f.getParentFile());
	}
	
	private void readReleaseNotes(File dataFolder) throws IOException
	{
		File relNotes = new File(dataFolder, "loinc_releasenotes.txt");
		if (relNotes.exists())
		{
			BufferedReader br = new BufferedReader(new FileReader(relNotes));
			String line = br.readLine();
			while (line != null)
			{
				if (line.contains("Version"))
				{
					String temp = line.substring(line.indexOf("Version") + "Version ".length());
					temp = temp.replace('|', ' ');
					version = temp.trim();
					
				}
				if (line.contains("Released"))
				{
					String temp = line.substring(line.indexOf("Released") + "Released ".length());
					temp = temp.replace('|', ' ');
					release = temp.trim();
					break;
				}
				line = br.readLine();
			}
			br.close();
		}
	}
	
	@Override
	public String getVersion()
	{
		return version;
	}

	@Override
	public String getReleaseDate()
	{
		return release;
	}

	@Override
	public String[] getHeader()
	{
		return header;
	}

	@Override
	public String[] readLine() throws IOException
	{
		String[] temp = reader.readNext();
		if (temp != null)
		{
			if (fieldCount_ == 0)
			{
				fieldCount_ = temp.length;
				int i = 0;
				for (String s : temp)
				{
					fieldMapInverse_.put(i, s);
					fieldMap_.put(s, i++);
				}
			}
			else if (temp.length < fieldCount_)
			{
				temp = Arrays.copyOf(temp, fieldCount_);
			}
			else if (temp.length > fieldCount_)
			{
				throw new RuntimeException("Data error - to many fields found on line: " + Arrays.toString(temp));
			}
		}
		return temp;
	}

	@Override
	public void close() throws IOException
	{
		reader.close();
	}

}
