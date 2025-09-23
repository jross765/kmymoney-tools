package org.kmymoney.tools.xml.upd;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.write.KMyMoneyWritableSecurity;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class UpdSec extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdSec.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String         kmmInFileName  = null;
  private static String         kmmOutFileName = null;

  private static Helper.CmdtySecMode mode        = null;
  private static KMMSecID            secID       = null;
  private static String              isin        = null;
  
  private static String          name = null;
  @SuppressWarnings("unused")
private static String          descr = null;
  private static KMMSecCurr.Type type = null;

  private static KMyMoneyWritableSecurity sec = null;

  public static void main( String[] args )
  {
    try
    {
      UpdSec tool = new UpdSec ();
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
    // acctID = UUID.randomUUID();

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
      .build();
          
    Option optFileOut = Option.builder("of")
      .required()
      .hasArg()
      .argName("file")
      .desc("KMyMoney file (out)")
      .longOpt("kmymoney-out-file")
      .build();
      
    Option optMode = Option.builder("m")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode")
      .longOpt("mode")
      .build();
    	        
    Option optSecID = Option.builder("sec")
      .hasArg()
      .argName("ID")
      .desc("Security ID")
      .longOpt("security-id")
      .build();
    	          
    Option optISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("ISIN")
      .longOpt("isin")
      .build();
            
    Option optName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Security name")
      .longOpt("name")
      .build();
    
    Option optType = Option.builder("t")
      .hasArg()
      .argName("type")
      .desc("Security type")
      .longOpt("type")
      .build();
        
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optMode);
    options.addOption(optSecID);
    options.addOption(optISIN);
    options.addOption(optName);
    options.addOption(optType);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    KMyMoneyWritableFileImpl kmmFile = new KMyMoneyWritableFileImpl(new File(kmmInFileName));

    KMyMoneyWritableSecurity sec = null;
    
    if ( mode == Helper.CmdtySecMode.ID )
    {
      sec = kmmFile.getWritableSecurityByID(secID);
      if ( sec == null )
      {
        System.err.println("Could not find a security with this ID.");
        throw new NoEntryFoundException();
      }
    }
    else if ( mode == Helper.CmdtySecMode.ISIN )
    {
      sec = kmmFile.getWritableSecurityByCode(isin);
      if ( sec == null )
      {
        System.err.println("Could not find securities with this ISIN.");
        throw new NoEntryFoundException();
      }
    }
    
    // ----------------------------
    
    doChanges(kmmFile);
    System.err.println("Security after update: " + sec.toString());
    
    kmmFile.writeFile(new File(kmmOutFileName));
    
    System.out.println("OK");
  }

  private void doChanges(KMyMoneyWritableFileImpl kmmFile) throws Exception
  {
    if ( name != null )
    {
      System.err.println("Setting name");
      sec.setName(name);
    }

    if ( type != null )
    {
      System.err.println("Setting type");
      sec.setType(type);
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
    
    // <mode>
    try
    {
      mode = Helper.CmdtySecMode.valueOf(cmdLine.getOptionValue("mode"));
      
      if ( mode != Helper.CmdtySecMode.NAME )
      {
        System.err.println("<mode> '" + Helper.CmdtySecMode.NAME + "' must not be used here");
        throw new InvalidCommandLineArgsException();
      }
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    System.err.println("Mode:         " + mode);

    // <security-id>
    if ( cmdLine.hasOption("security-id") )
    {
      if ( mode != Helper.CmdtySecMode.ID )
      {
        System.err.println("<security-id> must only be set with <mode> = '" + Helper.CmdtySecMode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        secID = new KMMSecID( cmdLine.getOptionValue("security-id") );
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <security-id>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( mode == Helper.CmdtySecMode.ID )
      {
        System.err.println("<security-id> must be set with <mode> = '" + Helper.CmdtySecMode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    System.err.println("Security ID:  '" + secID + "'");

    // <isin>
    if ( cmdLine.hasOption("isin") )
    {
      if ( mode != Helper.CmdtySecMode.ISIN )
      {
        System.err.println("<isin> must only be set with <mode> = '" + Helper.CmdtySecMode.ISIN.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        isin = cmdLine.getOptionValue("isin");
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <isin>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( mode == Helper.CmdtySecMode.ISIN )
      {
        System.err.println("<isin> must be set with <mode> = '" + Helper.CmdtySecMode.ISIN.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    System.err.println("ISIN:         '" + isin + "'");

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

    // <type>
    if ( cmdLine.hasOption("type") ) 
    {
      try
      {
        type = KMMSecCurr.Type.valueOf( cmdLine.getOptionValue("type") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <type>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Type: '" + type + "'");
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "UpdSec", options );
    
    System.out.println("");
    System.out.println("Valid values for <type>:");
    for ( KMMSecCurr.Type elt : KMMSecCurr.Type.values() )
      System.out.println(" - " + elt);
  }
}
