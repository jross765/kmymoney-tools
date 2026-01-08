package org.kmymoney.tools.xml.upd;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.kmymoney.api.write.KMyMoneyWritableInstitution;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class UpdInst extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdInst.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String    kmmInFileName = null;
  private static String    kmmOutFileName = null;
  
  private static KMMInstID instID = null;

  private static String name = null;

  private static KMyMoneyWritableInstitution inst = null;

  public static void main( String[] args )
  {
    try
    {
      UpdInst tool = new UpdInst ();
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
    // instID = UUID.randomUUID();

//    cfg = new PropertiesConfiguration(System.getProperty("config"));
//    getConfigSettings(cfg);

    // Options
    // The essential ones
    Option optFileIn = Option.builder("if")
      .required()
      .hasArg()
      .argName("file")
      .desc("KMyMoney file (in)")
      .longOpt("kmymoney-in-file")
      .get();
          
    Option optFileOut = Option.builder("of")
      .required()
      .hasArg()
      .argName("file")
      .desc("KMyMoney file (out)")
      .longOpt("kmymoney-out-file")
      .get();
      
    Option optID = Option.builder("id")
      .required()
      .hasArg()
      .argName("instid")
      .desc("Institution ID")
      .longOpt("institution-id")
      .get();
            
    Option optNumber = Option.builder("num")
      .hasArg()
      .argName("number")
      .desc("Institution number")
      .longOpt("number")
      .get();
    	    
    Option optName = Option.builder("nam")
      .hasArg()
      .argName("name")
      .desc("Institution name")
      .longOpt("name")
      .get();
    
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optID);
    options.addOption(optNumber);
    options.addOption(optName);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    KMyMoneyWritableFileImpl kmmFile = new KMyMoneyWritableFileImpl(new File(kmmInFileName), true);

    try 
    {
      inst = kmmFile.getWritableInstitutionByID(instID);
      System.err.println("Institution before update: " + inst.toString());
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not find/instantiate institution with ID '" + instID + "'");
      // ::TODO
//      throw new InstitutionNotFoundException();
      throw new NoEntryFoundException();
    }
    
    doChanges(kmmFile);
    System.err.println("Institution after update: " + inst.toString());
    
    kmmFile.writeFile(new File(kmmOutFileName));
    
    System.out.println("OK");
  }

  private void doChanges(KMyMoneyWritableFileImpl kmmFile) throws Exception
  {
    if ( name != null )
    {
      System.err.println("Setting name");
      inst.setName(name);
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

    // <kmymoney-in-file>
    try
    {
      kmmInFileName = cmdLine.getOptionValue("kmymoney-in-file");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <kmymoney-in-file>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("KMyMoney file (in): '" + kmmInFileName + "'");
    
    // <kmymoney-out-file>
    try
    {
      kmmOutFileName = cmdLine.getOptionValue("kmymoney-out-file");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <kmymoney-out-file>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("KMyMoney file (out): '" + kmmOutFileName + "'");
    
    // <institution-id>
    try
    {
      instID = new KMMInstID( cmdLine.getOptionValue("institution-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <institution-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Institution ID: " + instID);

    // <name>
    if ( cmdLine.hasOption("name") ) 
    {
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
    System.err.println("Name: '" + name + "'");
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "UpdInst", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
