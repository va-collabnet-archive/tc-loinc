package com.apelon.akcds.loinc;

import gov.va.oia.terminology.converters.sharedUtils.ConsoleUtil;
import gov.va.oia.terminology.converters.sharedUtils.EConceptUtility;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_ContentVersion.BaseContentVersion;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.Property;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.PropertyType;
import gov.va.oia.terminology.converters.sharedUtils.stats.ConverterUUID;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import com.apelon.akcds.loinc.propertyTypes.PT_Attributes;
import com.apelon.akcds.loinc.propertyTypes.PT_ContentVersion;
import com.apelon.akcds.loinc.propertyTypes.PT_Descriptions;
import com.apelon.akcds.loinc.propertyTypes.PT_IDs;
import com.apelon.akcds.loinc.propertyTypes.PT_Relations;
import com.apelon.akcds.loinc.propertyTypes.PT_SkipAxis;
import com.apelon.akcds.loinc.propertyTypes.PT_SkipClass;
import com.apelon.akcds.loinc.propertyTypes.PT_SkipOther;

/**
 * 
 * Loader code to convert Loinc into the workbench.
 * 
 * Paths are typically controlled by maven, however, the main() method has paths configured so that they 
 * match what maven does for test purposes.
 * 
 * @goal convert-loinc-to-jbin
 * 
 * @phase process-sources
 */
public class LoincToEConcepts extends AbstractMojo
{
	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;
	
	
	/**
	 * Location of the Loinc data.
	 * 
	 * @parameter
	 * @required
	 */
	private File loincDataFiles;
	
	/**
	 * Loader version number
	 * Use parent because project.version pulls in the version of the data file, which I don't want.
	 * 
	 * @parameter expression="${project.parent.version}"
	 * @required
	 */
	private String loaderVersion;
	
	/**
     * Content version number
     * 
     * @parameter expression="${project.version}"
     * @required
     */
    private String releaseVersion;
	
	private DataOutputStream dos_;
	private EConceptUtility conceptUtility_;

	private final String uuidRoot_ = "gov.va.med.term.loinc:";

	//Want a specific handle to these two - adhoc usage.
	private final PropertyType contentVersion_ = new PT_ContentVersion(uuidRoot_);
	
	//Need a handle to these too.
	private final PropertyType pt_SkipAxis_ = new PT_SkipAxis(uuidRoot_);
	private final PropertyType pt_SkipClass_ = new PT_SkipClass(uuidRoot_);
	
	private final ArrayList<PropertyType> propertyTypes_ = new ArrayList<PropertyType>();
	
	//Various caches for performance reasons
	private Hashtable<String, PropertyType> propertyToPropertyType_ = new Hashtable<String, PropertyType>();
	
	private int fieldCount_ = 0;
	private Hashtable<String, Integer> fieldMap_ = new Hashtable<String, Integer>();
	private Hashtable<Integer, String> fieldMapInverse_ = new Hashtable<Integer, String>();
	
	private final SimpleDateFormat sdf_ = new SimpleDateFormat("yyyyMMdd");
	
	Hashtable<UUID, EConcept> concepts_ = new Hashtable<UUID, EConcept>();
	
	private NameMap classMapping_;
	

	public LoincToEConcepts()
	{

	}
	
	/**
	 * Used for debug. Sets up the same paths that maven would use.... allow the code to be run standalone.
	 */
	public static void main(String[] args) throws Exception
	{
		LoincToEConcepts loincConverter = new LoincToEConcepts();
		loincConverter.outputDirectory = new File("../loinc-data/target/");
		loincConverter.loincDataFiles = new File("../loinc-data/target/generated-resources/src");
		loincConverter.execute();
	}
	
	private void initProperties()
	{
		//Can't init these till we know the data version
		propertyTypes_.add(new PT_IDs(uuidRoot_));
		propertyTypes_.add(new PT_Attributes(uuidRoot_));
		propertyTypes_.add(new PT_Descriptions(uuidRoot_));
		propertyTypes_.add(pt_SkipAxis_);
		propertyTypes_.add(pt_SkipClass_);
		PT_Relations r = new PT_Relations(uuidRoot_);
		//Create relations out of the skipAxis and SkipClass
		for (String  s : pt_SkipAxis_.getPropertyNames())
		{
			r.addProperty("Has_" + s);
		}
		for (String  s : pt_SkipClass_.getPropertyNames())
		{
			r.addProperty("Has_" + s);
		}
		propertyTypes_.add(r);
		propertyTypes_.add(new PT_SkipOther(uuidRoot_));

		propertyTypes_.add(contentVersion_);
	}

	public void execute() throws MojoExecutionException
	{
		ConsoleUtil.println("LOINC Processing Begins " + new Date().toString());
		BufferedReader dataReader = null;
		BufferedReader multiDataReader = null;
		try
		{
			//Set up the output
			if (!outputDirectory.exists())
			{
				outputDirectory.mkdirs();
			}
			
			if (!loincDataFiles.isDirectory())
			{
				throw new MojoExecutionException("LoincDataFiles must point to a directory containing the 3 required loinc data files");
			}
			
			
			File classMappingFile = null;
			File loincDataFile = null;
			File loincMultiDataFile = null;
			
			for (File f : loincDataFiles.listFiles())
			{
				if (f.getName().toLowerCase().startsWith("classmappings"))
				{
					classMappingFile = f;
				}
				else if (f.getName().toLowerCase().equals("loincdb.txt"))
				{
					loincDataFile = f;
				}
				else if (f.getName().toLowerCase().endsWith("multi-axial_hierarchy.csv"))
				{
					loincMultiDataFile = f;
				}
			}
			
			if (classMappingFile == null)
			{
				throw new MojoExecutionException("Could not find the class mappings file in " + loincDataFiles.getAbsolutePath());
			}
			else
			{
				ConsoleUtil.println("Using the class mapping file " + classMappingFile.getAbsolutePath());
			}
			if (loincDataFile == null)
			{
				throw new MojoExecutionException("Could not find the loinc data file in " + loincDataFiles.getAbsolutePath());
			}
			else
			{
				ConsoleUtil.println("Using the data file " + loincDataFile.getAbsolutePath());
			}
			if (loincMultiDataFile == null)
			{
				throw new MojoExecutionException("Could not find the multi-axial file in " + loincDataFiles.getAbsolutePath());
			}
			else
			{
				ConsoleUtil.println("Using the multi-axial file " + loincMultiDataFile.getAbsolutePath());
			}
			
			classMapping_ = new NameMap(classMappingFile);
			
			dataReader = new BufferedReader(new FileReader(loincDataFile));
			multiDataReader = new BufferedReader(new FileReader(loincMultiDataFile));
			
			//Line 1 of the file is version, line 2 is date.  Hope they are consistent.....
			String version = dataReader.readLine();
			String releaseDate = dataReader.readLine();
			
			if (version.contains("2.36"))
			{
				PropertyType.setSourceVersion(1);
			}
			else if (version.contains("2.38"))
			{
				PropertyType.setSourceVersion(2);
			}
			else if (version.contains("2.40"))
			{
				PropertyType.setSourceVersion(3);
			}
			else
			{
				ConsoleUtil.printErrorln("ERROR: UNTESTED VERSION - NO TESTED PROPERTY MAPPING EXISTS!");
				PropertyType.setSourceVersion(3);
			}

			initProperties();
			
			conceptUtility_ = new EConceptUtility(uuidRoot_);
			File binaryOutputFile = new File(outputDirectory, "loincEConcepts.jbin");
			
			dos_ = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(binaryOutputFile)));

			ConsoleUtil.println("Loading Metadata");
			
			//Set up a meta-data root concept
			UUID archRoot = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getPrimoridalUid();
			UUID metaDataRoot = ConverterUUID.nameUUIDFromBytes((uuidRoot_ + "metadata").getBytes());
			conceptUtility_.createAndStoreMetaDataConcept(metaDataRoot, "LOINC Metadata", archRoot, dos_);
			
			conceptUtility_.loadMetaDataItems(propertyTypes_, metaDataRoot, dos_);
			
			//Load up the propertyType map for speed, perform basic sanity check
			for (PropertyType pt : propertyTypes_)
			{
				for (String propertyName : pt.getPropertyNames())
				{
					if (propertyToPropertyType_.containsKey(propertyName))
					{
						ConsoleUtil.printErrorln("ERROR: Two different property types each contain " + propertyName);
					}
					propertyToPropertyType_.put(propertyName, pt);
				}
			}
			
			EConcept vaRefsetsConcept = conceptUtility_.createVARefsetRootConcept();
			vaRefsetsConcept.writeExternal(dos_);
			
			//write this at the end
			EConcept loincRefset = conceptUtility_.createConcept("All LOINC Concepts", vaRefsetsConcept.getPrimordialUuid());
			
			//Scan forward in the data file for the "cutoff" point
			int i = 0;
			while (true)
			{
				i++;
				String temp = dataReader.readLine();
				if (temp.equals("<----Clip Here for Data----->"))
				{
					break;
				}
				if (i > 500)
				{
					throw new Exception("Couldn't find '<----Clip Here for Data----->' constant.  Format must have changed.  Failing");
				}
			}
			
			//The next line of the file is the header.
			String headerLine = dataReader.readLine();
			String[] headerFields = getFields(headerLine);
			
			//validate that we are configured to map all properties properly
			checkForLeftoverPropertyTypes(headerFields);
			
			//Root
			EConcept rootConcept = conceptUtility_.createConcept("LOINC", "LOINC");
			
			conceptUtility_.addStringAnnotation(rootConcept, version, contentVersion_.getProperty("Source Version").getUUID(), false);
			conceptUtility_.addStringAnnotation(rootConcept, releaseDate, contentVersion_.getProperty("Release Date").getUUID(), false);
			conceptUtility_.addStringAnnotation(rootConcept, releaseVersion, BaseContentVersion.RELEASE.getProperty().getUUID(), false);
			conceptUtility_.addStringAnnotation(rootConcept, loaderVersion, BaseContentVersion.LOADER_VERSION.getProperty().getUUID(), false);
			
			concepts_.put(rootConcept.primordialUuid, rootConcept);
			
			//Build up the Class metadata
			
			EConcept classConcept = conceptUtility_.createConcept(pt_SkipClass_.getPropertyTypeUUID(), pt_SkipClass_.getPropertyTypeDescription(), 
			        rootConcept.primordialUuid);
			concepts_.put(classConcept.primordialUuid, classConcept);
			
			for (String property : pt_SkipClass_.getPropertyNames())
			{
				EConcept temp = conceptUtility_.createConcept(pt_SkipClass_.getProperty(property).getUUID(), property, classConcept.primordialUuid);
				concepts_.put(temp.primordialUuid, temp);
			}
			
			//And the axis metadata
			EConcept axisConcept = conceptUtility_.createConcept(pt_SkipAxis_.getPropertyTypeUUID(), pt_SkipAxis_.getPropertyTypeDescription(), rootConcept.primordialUuid);
			concepts_.put(axisConcept.primordialUuid, axisConcept);
			
			for (String property : pt_SkipAxis_.getPropertyNames())
			{
				EConcept temp = conceptUtility_.createConcept(pt_SkipAxis_.getProperty(property).getUUID(), property, axisConcept.primordialUuid);
				concepts_.put(temp.primordialUuid, temp);
			}
			
			ConsoleUtil.println("Metadata summary:");
			for (String s : conceptUtility_.getLoadStats().getSummary())
			{
				ConsoleUtil.println("  " + s);
			}
			conceptUtility_.clearLoadStats();
			
			//load the data
			ConsoleUtil.println("Reading data file into memory.");
			
			int dataRows = 0;
			{
				String line = dataReader.readLine();
				dataRows++;
				while (line != null)
				{
					if (line.length() > 0)
					{
						processDataLine(line);
					}
					line = dataReader.readLine();
					dataRows++;
					if (dataRows % 1000 == 0)
					{
						ConsoleUtil.showProgress();
					}
				}
			}
			dataReader.close();
			
			ConsoleUtil.println("Read " + dataRows + " data lines from file");
			
			ConsoleUtil.println("Processing multi-axial file");
			
			{
				String line = multiDataReader.readLine();
				//Line 1 is header - PATH_TO_ROOT,SEQUENCE,IMMEDIATE_PARENT,CODE,CODE_TEXT
				int lineCount = 0;
				line = multiDataReader.readLine();
				while (line != null)
				{
					lineCount++;
					if (line.length() > 0)
					{
						processMultiAxialData(rootConcept.getPrimordialUuid(), line);
					}
					line = multiDataReader.readLine();
					if (lineCount % 1000 == 0)
					{
						ConsoleUtil.showProgress();
					}
				}
				multiDataReader.close();
				ConsoleUtil.println("Read " + lineCount+ " data lines from file");
			}

			ConsoleUtil.println("Writing jbin file");

			int conCounter = 0;
			for (EConcept concept : concepts_.values())
			{
			    conceptUtility_.addRefsetMember(loincRefset, concept.getPrimordialUuid(), true, null);
				concept.writeExternal(dos_);
		        conCounter++;

		        if (conCounter % 10 == 0)
		        {
		            ConsoleUtil.showProgress();
		        }
		        if ((conCounter % 10000) == 0)
		        {
		            ConsoleUtil.println("Processed: " + conCounter + " - just completed " + concept.getDescriptions().get(0).getText());
		        }
			}
			
			loincRefset.writeExternal(dos_);

			ConsoleUtil.println("Data Load Summary:");
			for (String s : conceptUtility_.getLoadStats().getSummary())
			{
				ConsoleUtil.println("  " + s);
			}
			
			//this could be removed from final release.  Just added to help debug editor problems.
			ConsoleUtil.println("Dumping UUID Debug File");
			ConverterUUID.dump(new File(outputDirectory, "loincUuidDebugMap.txt"));
			
			ConsoleUtil.println("LOINC Processing Completes " + new Date().toString());
		}
		catch (Exception ex)
		{
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}
		finally
		{
			if (dos_ != null)
			{
				try
				{
					dos_.flush();
					dos_.close();
					dataReader.close();
					multiDataReader.close();
				}
				catch (IOException e)
				{
					throw new MojoExecutionException(e.getLocalizedMessage(), e);
				}
			}
		}
	}
	
	private void processDataLine(String line) throws ParseException, IOException, TerminologyException
	{
		String[] fields = getFields(line);
		
		Integer index = fieldMap_.get("DT_LAST_CH");
		if (index == null)
		{
			index = fieldMap_.get("DATE_LAST_CHANGED");  //They changed this in 2.38 release
		}
		String lastChanged = fields[index];
		long time = (lastChanged == null ? System.currentTimeMillis() : sdf_.parse(lastChanged).getTime());

		UUID statusUUID = mapStatus(fields[fieldMap_.get("STATUS")]);
		
		String code = fields[fieldMap_.get("LOINC_NUM")];
		
		String description = fields[fieldMap_.get("LONG_COMMON_NAME")];
		if (description == null)
		{
			description = fields[fieldMap_.get("SHORTNAME")];
		}
		if (description == null)
		{
			ConsoleUtil.printErrorln("no name for " + code);
			description = code;
		}
		EConcept concept = conceptUtility_.createConcept(buildUUID(code), description, time, statusUUID);
		
		for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++)
		{
			if (fields[fieldIndex] != null && fields[fieldIndex].length() > 0)
			{
				PropertyType pt = propertyToPropertyType_.get(fieldMapInverse_.get(fieldIndex));
				if (pt == null)
				{
					ConsoleUtil.printErrorln("ERROR: No property type mapping for the property " 
							+ fieldMapInverse_.get(fieldIndex) + ":" + fields[fieldIndex] );
					continue;
				}
				
				Property p = pt.getProperty(fieldMapInverse_.get(fieldIndex));
				
				if (pt instanceof PT_Attributes)
				{
					conceptUtility_.addStringAnnotation(concept, fields[fieldIndex], p.getUUID(), p.isDisabled());
				}
				else if (pt instanceof PT_Descriptions)
				{
					conceptUtility_.addDescription(concept, fields[fieldIndex], p.getUUID(), p.isDisabled());
				}
				else if (pt instanceof PT_IDs)
				{
					conceptUtility_.addAdditionalIds(concept, fields[fieldIndex], p.getUUID(), p.isDisabled());
				}
				else if (pt instanceof PT_SkipAxis)
				{
					//See if this class object exists yet.
					UUID potential = ConverterUUID.nameUUIDFromBytes((uuidRoot_ + 
							pt_SkipAxis_.getPropertyTypeDescription() + ":" +
							fieldMapInverse_.get(fieldIndex) + ":" + 
							fields[fieldIndex]).getBytes());
					
					EConcept axisConcept = concepts_.get(potential);
					if (axisConcept == null)
					{
						axisConcept = conceptUtility_.createConcept(potential, fields[fieldIndex]);
						conceptUtility_.addRelationship(axisConcept, pt_SkipAxis_.getProperty(fieldMapInverse_.get(fieldIndex)).getUUID(), null, null);
						concepts_.put(axisConcept.primordialUuid, axisConcept);
					}
					//We changed these from attributes to relations
					//conceptUtility_.addAnnotation(concept, axisConcept, pt_SkipAxis_.getPropertyUUID(fieldMapInverse_.get(fieldIndex)));
					String relTypeName = "Has_" + fieldMapInverse_.get(fieldIndex);
					PropertyType relType = propertyToPropertyType_.get(relTypeName);
					conceptUtility_.addRelationship(concept, axisConcept.getPrimordialUuid(), relType.getProperty(relTypeName).getUUID(), null);
				}
				else if (pt instanceof PT_SkipClass)
				{
					//See if this class object exists yet.
					UUID potential = ConverterUUID.nameUUIDFromBytes((uuidRoot_ + 
							pt_SkipClass_.getPropertyTypeDescription() + ":" +  
							fieldMapInverse_.get(fieldIndex) + ":" + 
							fields[fieldIndex]).getBytes());
					
					EConcept classConcept = concepts_.get(potential);
					if (classConcept == null)
					{

						classConcept = conceptUtility_.createConcept(potential, classMapping_.getMatchValue(fields[fieldIndex]));
						if (classMapping_.hasMatch(fields[fieldIndex]))
						{
							conceptUtility_.addAdditionalIds(classConcept, fields[fieldIndex], 
								propertyToPropertyType_.get("ABBREVIATION").getProperty("ABBREVIATION").getUUID(), false);
						}
						conceptUtility_.addRelationship(classConcept, pt_SkipClass_.getProperty(fieldMapInverse_.get(fieldIndex)).getUUID(), null, null);
						concepts_.put(classConcept.primordialUuid, classConcept);
					}
					//We changed these from attributes to relations
					//conceptUtility_.addAnnotation(concept, classConcept, pt_SkipClass_.getPropertyUUID(fieldMapInverse_.get(fieldIndex)));
					String relTypeName = "Has_" + fieldMapInverse_.get(fieldIndex);
					PropertyType relType = propertyToPropertyType_.get(relTypeName);
					conceptUtility_.addRelationship(concept, classConcept.getPrimordialUuid(), relType.getProperty(relTypeName).getUUID(), null);
				}
				else if (pt instanceof PT_Relations)
				{
					conceptUtility_.addRelationship(concept, buildUUID(fields[fieldIndex]), 
							pt.getProperty(fieldMapInverse_.get(fieldIndex)).getUUID(), null); 
				}
				else if (pt instanceof PT_SkipOther)
				{
					//noop
				}
				else
				{
					ConsoleUtil.printErrorln("oops - unexpected property type: " + pt);
				}
			}
		}
		
		concepts_.put(concept.primordialUuid, concept);
	}
	
	private void processMultiAxialData(UUID rootConcept, String line)
	{
		//PATH_TO_ROOT,SEQUENCE,IMMEDIATE_PARENT,CODE,CODE_TEXT
		//But, the format sucks.  Code_Text is surrounded by "..." and full of commas.... so it needs custom parsing.

		int startPos = 0;
		int endPos = line.indexOf(',');
		String pathString = line.substring(startPos, endPos);
		String[] pathToRoot = (pathString.length() > 0 ? pathString.split("\\.") : new String[] {});
		
		startPos = endPos + 1;
		endPos = line.indexOf(",", startPos);
		String sequence = line.substring(startPos, endPos);
		
		startPos = endPos + 1;
		endPos = line.indexOf(",", startPos);
		String immediateParentString = line.substring(startPos, endPos);
		
		UUID immediateParent = (immediateParentString == null || immediateParentString.length() == 0 ? rootConcept : buildUUID(immediateParentString));
		
		startPos = endPos + 1;
		endPos = line.indexOf(",", startPos);
		String code = line.substring(startPos, endPos);
		
		startPos = endPos + 1;
		endPos = line.indexOf(",", startPos);
		String codeText = line.substring(startPos);
		if (codeText.startsWith("\"") && codeText.endsWith("\""))
		{
			codeText = codeText.substring(1, codeText.length() - 1);
		}
		
		if (code.length() == 0 || codeText.length() == 0)
		{
			ConsoleUtil.printErrorln("missing code or text!");
		}
		
		UUID potential = buildUUID(code);
		
		EConcept concept = concepts_.get(potential);
		if (concept == null)
		{
			concept = conceptUtility_.createConcept(potential, codeText);
			if (sequence != null && sequence.length() > 0)
			{
				conceptUtility_.addStringAnnotation(concept, sequence, propertyToPropertyType_.get("SEQUENCE").getProperty("SEQUENCE").getUUID(), false);
			}
			
			if (immediateParentString != null && immediateParentString.length() > 0)
			{
				conceptUtility_.addStringAnnotation(concept, immediateParentString, propertyToPropertyType_.get("IMMEDIATE_PARENT").getProperty("IMMEDIATE_PARENT").getUUID(), false);
			}
			
			conceptUtility_.addDescription(concept, codeText, propertyToPropertyType_.get("CODE_TEXT").getProperty("CODE_TEXT").getUUID(), false);
			
			conceptUtility_.addRelationship(concept, immediateParent, propertyToPropertyType_.get("Multiaxial Child Of").getProperty("Multiaxial Child Of").getUUID(), null);
			
			if (pathString != null && pathString.length() > 0)
			{
				conceptUtility_.addStringAnnotation(concept, pathString, propertyToPropertyType_.get("PATH_TO_ROOT").getProperty("PATH_TO_ROOT").getUUID(), false);
			}
			conceptUtility_.addAdditionalIds(concept, code, propertyToPropertyType_.get("CODE").getProperty("CODE").getUUID(), false);
			
			concepts_.put(concept.primordialUuid, concept);
		}
		
		//Make sure everything in pathToRoot is linked.
		checkPath(concept, pathToRoot);
	}
	
	private void checkPath(EConcept concept, String[] pathToRoot)
	{
		//The passed in concept should have a relation to the item at the end of the root list.
		for (int i = (pathToRoot.length - 1); i >= 0; i--)
		{
			boolean found = false;
			UUID target = buildUUID(pathToRoot[i]);
			List<TkRelationship> rels = concept.getRelationships();
			if (rels != null)
			{
				for (TkRelationship rel : rels)
				{
					if (rel.getRelationshipSourceUuid().equals(concept.getPrimordialUuid())
							&& rel.getRelationshipTargetUuid().equals(target))
					{
						found = true;
						break;
					}
				}
			}
			if (!found)
			{
				conceptUtility_.addRelationship(concept, target, propertyToPropertyType_.get("Multiaxial Child Of").getProperty("Multiaxial Child Of").getUUID(), null);
			}
			concept = concepts_.get(target);
			if (concept == null)
			{
				ConsoleUtil.printErrorln("Missing concept! " + pathToRoot[i]);
				break;
			}
		}
	}
	
	private UUID mapStatus(String status) throws IOException, TerminologyException
	{
		if (status.equals("ACTIVE"))
		{
			return ArchitectonicAuxiliary.Concept.ACTIVE.getPrimoridalUid();
		}
		else if (status.equals("TRIAL"))
		{
			return ArchitectonicAuxiliary.Concept.TRIAL.getPrimoridalUid();
		}
		
		else if (status.equals("DISCOURAGED"))
		{
			return ArchitectonicAuxiliary.Concept.DISCOURAGED.getPrimoridalUid();
		}
		
		else if (status.equals("DEPRECATED"))
		{
			return ArchitectonicAuxiliary.Concept.DEPRECATED.getPrimoridalUid();
		}
		else
		{
			ConsoleUtil.printErrorln("No mapping for status: " + status);
			return ArchitectonicAuxiliary.Concept.CURRENT.getPrimoridalUid();
		}
		
	}
	
	private void checkForLeftoverPropertyTypes(String[] fileColumnNames) throws Exception
	{
		for (String name : fileColumnNames)
		{
			PropertyType pt = propertyToPropertyType_.get(name);
			if (pt == null)
			{
				ConsoleUtil.printErrorln("ERROR:  No mapping for property type: " + name);
			}
		}
	}

	/**
	 * Utility to help build UUIDs in a consistent manner.
	 */
	private UUID buildUUID(String uniqueIdentifier)
	{
		return ConverterUUID.nameUUIDFromBytes((uuidRoot_ + uniqueIdentifier).getBytes());
	}
	
	private String[] getFields(String line)
	{
		String[] temp = line.split("\\t");
		for (int i = 0; i < temp.length; i++)
		{
			if (temp[i].length() == 0)
			{
				temp[i] = null;
			}
			else if (temp[i].startsWith("\"") && temp[i].endsWith("\""))
			{
				temp[i] = temp[i].substring(1, temp[i].length() - 1);
			}
		}
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
			temp =  Arrays.copyOf(temp, fieldCount_);
		}
		else if (temp.length > fieldCount_)
		{
			throw new RuntimeException("Data error - to many fields found on line: " + line);
		}
		return temp;
	}
}
