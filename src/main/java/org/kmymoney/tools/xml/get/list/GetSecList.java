package org.kmymoney.tools.xml.get.list;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetSecList extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetSecList.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String                kmmFileName = null;
  private static Helper.CmdtySecListSelMode    mode        = null; 
  private static KMMSecCurr.Type       type        = null;
  private static String                name        = null;
  
  private static boolean scriptMode = false; // ::TODO

  public static void main( String[] args )
  {
    try
    {
      GetSecList tool = new GetSecList ();
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
      .get();
      
    Option optMode = Option.builder("m")
      .required()
      .hasArg()
      .argName("Mode")
      .desc("Mode")
      .longOpt("mode")
      .get();
    	      
    Option optType = Option.builder("t")
      .hasArg()
      .argName("type")
      .desc("Security type")
      .longOpt("type")
      .get();
      
    Option optName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Security name (part of)")
      .longOpt("name")
      .get();
    	      
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optType);
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
    
    Collection<KMyMoneySecurity> secList = null; 
    if ( mode == Helper.CmdtySecListSelMode.ALL )
        secList = kmmFile.getSecurities();
    else if ( mode == Helper.CmdtySecListSelMode.TYPE )
    	secList = kmmFile.getSecuritiesByType(type);
    else if ( mode == Helper.CmdtySecListSelMode.NAME )
    	secList = kmmFile.getSecuritiesByName(name, true);

    if ( secList.size() == 0 ) 
    {
    	System.err.println("Found no security with that type.");
    	throw new NoEntryFoundException();
    }

	System.err.println("Found " + secList.size() + " security/ies.");
    for ( KMyMoneySecurity sec : secList )
    {
    	System.out.println(sec.toString());	
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
      System.err.println("KMyMoney file:     '" + kmmFileName + "'");
    
    // <mode>
    try
    {
      mode = Helper.CmdtySecListSelMode.valueOf(cmdLine.getOptionValue("mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <mode>");
      throw new InvalidCommandLineArgsException();
    }

    // <type>
    if ( cmdLine.hasOption( "type" ) )
    {
    	if ( mode != Helper.CmdtySecListSelMode.TYPE )
    	{
            System.err.println("Error: <type> must only be set with <mode> = '" + Helper.CmdtySecListSelMode.TYPE + "'");
            throw new InvalidCommandLineArgsException();
    	}
    	
        try
        {
        	type = KMMSecCurr.Type.valueOf(cmdLine.getOptionValue("type"));
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <type>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    else
    {
    	if ( mode == Helper.CmdtySecListSelMode.TYPE )
    	{
            System.err.println("Error: <type> must be set with <mode> = '" + Helper.CmdtySecListSelMode.TYPE + "'");
            throw new InvalidCommandLineArgsException();
    	}
    	
    	type = null;
    }
    
    if ( ! scriptMode )
      System.err.println("Type:              " + type);

    // <name>
    if ( cmdLine.hasOption( "name" ) )
    {
    	if ( mode != Helper.CmdtySecListSelMode.NAME )
    	{
            System.err.println("Error: <name> must only be set with <mode> = '" + Helper.CmdtySecListSelMode.NAME + "'");
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
    	if ( mode == Helper.CmdtySecListSelMode.NAME )
    	{
            System.err.println("Error: <name> must be set with <mode> = '" + Helper.CmdtySecListSelMode.NAME + "'");
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
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GetSecList", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <mode>:");
    for ( Helper.CmdtySecListSelMode elt : Helper.CmdtySecListSelMode.values() )
      System.out.println(" - " + elt);

    System.out.println("");
    System.out.println("Valid values for <type>:");
    for ( KMMSecCurr.Type elt : KMMSecCurr.Type.values() )
      System.out.println(" - " + elt);
  }
}
