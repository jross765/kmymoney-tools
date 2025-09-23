package org.kmymoney.tools.xml.get.list;

import java.io.File;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetTagList extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetTagList.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String               kmmFileName = null;
  private static Helper.TagListMode   mode        = null; 
  private static String               name        = null;
  
  private static boolean scriptMode = false; // ::TODO

  public static void main( String[] args )
  {
    try
    {
      GetTagList tool = new GetTagList ();
      tool.execute(args);
    }
    catch (CouldNotExecuteException exc) 
    {
      System.err.println("Execution exception. Aborting.");
      exc.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  protected void init() throws Exception
  {
    // cmdtyID = UUID.randomUUID();

//    cfg = new PropertiesConfiguration(System.getProperty("config"));
//    getConfigSettings(cfg);

    // Options
    // The essential ones
    Option optFile = Option.builder("f")
      .required()
      .hasArg()
      .argName("file")
      .desc("KMyMoney file")
      .longOpt("kmymoney-file")
      .build();
      
    Option optMode = Option.builder("m")
      .required()
      .hasArg()
      .argName("Mode")
      .desc("Mode")
      .longOpt("mode")
      .build();
    	    	      
    Option optName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Tag name (part of)")
      .longOpt("name")
      .build();
    	      
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optName);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    KMyMoneyFileImpl kmmFile = new KMyMoneyFileImpl(new File(kmmFileName));
    
    Collection<KMyMoneyTag> cmdtyList = null; 
    if ( mode == Helper.TagListMode.ALL )
        cmdtyList = kmmFile.getTags();
    else if ( mode == Helper.TagListMode.NAME )
    	cmdtyList = kmmFile.getTagsByName(name, true);

    if ( cmdtyList.size() == 0 ) 
    {
    	System.err.println("Found no tag with that type.");
    	throw new NoEntryFoundException();
    }

    System.err.println("Found " + cmdtyList.size() + " tags.");
    for ( KMyMoneyTag cmdty : cmdtyList )
    {
    	System.out.println(cmdty.toString());	
    }
  }

  // -----------------------------------------------------------------

  @Override
  protected void parseCommandLineArgs(String[] args) throws InvalidCommandLineArgsException
  {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmdLine = null;
    try
    {
      cmdLine = parser.parse(options, args);
    }
    catch (ParseException exc)
    {
      System.err.println("Parsing options failed. Reason: " + exc.getMessage());
      throw new InvalidCommandLineArgsException();
    }

    // ---

    // <kmymoney-file>
    try
    {
      kmmFileName = cmdLine.getOptionValue("kmymoney-file");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <kmymoney-file>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("KMyMoney file:      '" + kmmFileName + "'");
    
    // <mode>
    try
    {
      mode = Helper.TagListMode.valueOf(cmdLine.getOptionValue("mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <mode>");
      throw new InvalidCommandLineArgsException();
    }

    // <name>
    if ( cmdLine.hasOption( "name" ) )
    {
    	if ( mode != Helper.TagListMode.NAME )
    	{
            System.err.println("Error: <name> must only be set with <mode> = '" + Helper.TagListMode.NAME + "'");
            throw new InvalidCommandLineArgsException();
    	}
    	
        try
        {
        	name = cmdLine.getOptionValue("name");
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <name>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    else
    {
    	if ( mode == Helper.TagListMode.NAME )
    	{
            System.err.println("Error: <name> must be set with <mode> = '" + Helper.TagListMode.NAME + "'");
            throw new InvalidCommandLineArgsException();
    	}
    	
    	name = null;
    }
    
    if ( ! scriptMode )
      System.err.println("Name:              " + name);
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GetTagList", options );
    
    System.out.println("");
    System.out.println("Valid values for <mode>:");
    for ( Helper.TagListMode elt : Helper.TagListMode.values() )
      System.out.println(" - " + elt);
  }
}
