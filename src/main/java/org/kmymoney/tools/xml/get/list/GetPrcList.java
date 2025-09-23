package org.kmymoney.tools.xml.get.list;

import java.io.File;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetPrcList extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetPrcList.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String                kmmFileName   = null;
  private static Helper.CmdtySecMode   secMode       = null;
  private static KMMQualifSecCurrID    fromSecCurrID = null;
  private static String                fromSecIsin   = null;
  private static String                fromSecName   = null;
  
  private static boolean scriptMode = false; // ::TODO

  public static void main( String[] args )
  {
    try
    {
      GetPrcList tool = new GetPrcList ();
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
      .build();
      
    Option optMode = Option.builder("m")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Security/currency selection mode")
      .longOpt("mode")
      .build();
    	        
    Option optFromSecCurr= Option.builder("fr")
      .hasArg()
      .argName("qualif-ID")
      .desc("From-security/currency qualified ID")
      .longOpt("from-sec-curr")
      .build();
    	    	          
    Option optFromISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("From-security/currency ISIN")
      .longOpt("isin")
      .build();
    	        
    Option optFromName = Option.builder("fn")
      .hasArg()
      .argName("name")
      .desc("From-security/currency Name (or part of)")
      .longOpt("name")
      .build();
    	          
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optFromSecCurr);
    options.addOption(optFromISIN);
    options.addOption(optFromName);
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
    
    if ( secMode == Helper.CmdtySecMode.ISIN )
    {
        KMyMoneySecurity sec = kmmFile.getSecurityByCode(fromSecIsin);
    	fromSecCurrID = sec.getQualifID();
    }
    else if ( secMode == Helper.CmdtySecMode.NAME )
    {
        KMyMoneySecurity sec = kmmFile.getSecurityByNameUniq(fromSecName);
    	fromSecCurrID = sec.getQualifID();
    }
    
    List<KMyMoneyPrice> prcList = kmmFile.getPricesByQualifSecCurrID( fromSecCurrID );
    if ( prcList.size() == 0 ) 
    {
    	System.err.println("Found no price with for that security/currency ID.");
    	throw new NoEntryFoundException();
    }

	System.err.println("Found " + prcList.size() + " price(s).");
    for ( KMyMoneyPrice prc : prcList )
    {
    	System.out.println(prc.toString());	
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
      System.err.println("KMyMoney file: '" + kmmFileName + "'");
    
    // <mode>
    try
    {
      secMode = Helper.CmdtySecMode.valueOf(cmdLine.getOptionValue("mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <mode>");
      throw new InvalidCommandLineArgsException();
    }

    // <from-sec-curr>
    if ( cmdLine.hasOption("from-sec-curr") )
    {
      if ( secMode != Helper.CmdtySecMode.ID )
      {
        System.err.println("<from-sec-curr> must only be set with <mode> = '" + Helper.CmdtySecMode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
          fromSecCurrID = KMMQualifSecCurrID.parse(cmdLine.getOptionValue("from-sec-curr")); 
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <from-sec-curr>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( secMode == Helper.CmdtySecMode.ID )
      {
        System.err.println("<from-sec-curr> must be set with <mode> = '" + Helper.CmdtySecMode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("From-security/currency ID:   '" + fromSecCurrID + "'");

    // <isin>
    if ( cmdLine.hasOption("isin") )
    {
      if ( secMode != Helper.CmdtySecMode.ISIN )
      {
        System.err.println("<isin> must only be set with <mode> = '" + Helper.CmdtySecMode.ISIN.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
    	  fromSecIsin = cmdLine.getOptionValue("isin");
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <isin>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( secMode == Helper.CmdtySecMode.ISIN )
      {
        System.err.println("<isin> must be set with <mode> = '" + Helper.CmdtySecMode.ISIN.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("From-security/currency ISIN: '" + fromSecIsin + "'");

    // <name>
    if ( cmdLine.hasOption("name") )
    {
      if ( secMode != Helper.CmdtySecMode.NAME )
      {
        System.err.println("<name> must only be set with <mode> = '" + Helper.CmdtySecMode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
    	  fromSecName = cmdLine.getOptionValue("name");
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( secMode == Helper.CmdtySecMode.NAME )
      {
        System.err.println("<name> must be set with <mode> = '" + Helper.CmdtySecMode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("From-security/currency name: '" + fromSecName + "'");
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GetPrcList", options );
    
    System.out.println("");
    System.out.println("Valid values for <mode>:");
    for ( Helper.CmdtySecMode elt : Helper.CmdtySecMode.values() )
      System.out.println(" - " + elt);
  }
}
