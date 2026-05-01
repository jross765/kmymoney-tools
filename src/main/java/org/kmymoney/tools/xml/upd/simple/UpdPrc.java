package org.kmymoney.tools.xml.upd.simple;

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
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.write.KMyMoneyWritablePrice;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.base.basetypes.complex.KMMPrcID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.tools.CommandLineTool;
import org.kmymoney.tools.xml.helper.CmdLineHelper_Prc;
import org.kmymoney.tools.xml.helper.LocalDateWrp;
import org.kmymoney.tools.xml.helper.PriceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class UpdPrc extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdPrc.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String     kmmInFileName = null;
  private static String     kmmOutFileName = null;

  private static CmdLineHelper_Prc.PrcSelectMode    prcSelMode = null;
  private static CmdLineHelper_Prc.PrcSelectSubMode prcSelSubMode = null;

  // CAUTION: As opposed to most other tools, the following variables
  // have to be instantiated here.
  
  private static KMMPrcID           prcID         = new KMMPrcID();
  private static KMMQualifSecCurrID fromSecCurrID = new KMMQualifSecCurrID();
  private static KMMQualifCurrID    toCurrID      = new KMMQualifCurrID();
  private static Helper.DateFormat  dateFormat    = null;
  private static LocalDateWrp       date          = new LocalDateWrp();
  private static StringBuffer       isin          = new StringBuffer();
  // Possibly later:
  // private static StringBuffer  wkn             = new StringBuffer();
  // private static StringBuffer  cusip           = new StringBuffer();
  // private static StringBuffer  sedol           = new StringBuffer();
  
  // ---
  
  private static KMyMoneyWritablePrice prc = null;

  private static KMyMoneyPrice.Source newSource = null;
  private static FixedPointNumber     newValue  = null;

  private static boolean scriptMode = false;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      UpdPrc tool = new UpdPrc ();
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

    Option optPrcMode = Option.builder("psm")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode for price")
      .longOpt("prc-sel-mode")
      .get();

    Option optPrcSubMode = Option.builder("pssm")
      .hasArg()
      .argName("submode")
      .desc("Selection sub-mode for price " +
    		  "(for <mode> = " + CmdLineHelper_Prc.PrcSelectMode.ID + " only)")
      .longOpt("prc-sel-sub-mode")
      .get();

    Option optPrcID = Option.builder("prc")
      .hasArg()
      .argName("prcid")
      .desc("Price-ID" +
    		  "(for <mode> = " + CmdLineHelper_Prc.PrcSelectMode.ID + " only)")
      .longOpt("price-id")
      .get();

    Option optPrcFromSecCurr = Option.builder("fsc")
      .hasArg()
      .argName("sec/curr")
      .desc("Price from security/currency ID " +
    		  "(for <mode> = " + CmdLineHelper_Prc.PrcSelectMode.ID + " or " +
    		       "<mode> = " + CmdLineHelper_Prc.PrcSelectMode.SEC_DATE + " only)")
      .longOpt("price-from-sec-curr-id")
      .get();

    Option optPrcToCurr = Option.builder("tc")
      .hasArg()
      .argName("curr")
      .desc("Price to currency ID " +
    		  "(for <mode> = " + CmdLineHelper_Prc.PrcSelectMode.ID + " only)")
      .longOpt("price-to-curr-id")
      .get();

    Option optPrcDateFormat = Option.builder("df")
      .hasArg()
      .argName("date-format")
      .desc("Price date format")
      .longOpt("price-date-format")
      .get();

    Option optPrcDate = Option.builder("dat")
      .hasArg()
      .argName("date")
      .desc("Price date")
      .longOpt("price-date")
      .get();

    Option optPrcISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("ISIN " + 
    		  "(for <mode> = " + CmdLineHelper_Prc.PrcSelectMode.ISIN_DATE + " only)")
      .longOpt("isin")
      .get();

    Option optSource = Option.builder("s")
      .hasArg()
      .argName("source")
      .desc("Price source (new)")
      .longOpt("new-source")
      .get();

    Option optValue = Option.builder("v")
      .hasArg()
      .argName("value")
      .desc("Price value (new)")
      .longOpt("new-value")
      .get();

    // The convenient ones
    Option optScript = Option.builder("sl")
      .desc("Script Mode")
      .longOpt("script")
      .get();            

    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optPrcMode);
    options.addOption(optPrcSubMode);
    options.addOption(optPrcID);
    options.addOption(optPrcFromSecCurr);
    options.addOption(optPrcToCurr);
    options.addOption(optPrcDateFormat);
    options.addOption(optPrcDate);
    options.addOption(optPrcISIN);
    options.addOption(optSource);
    options.addOption(optValue);
    options.addOption(optScript);
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

    prc = PriceHelper.getWrtPrc(prcSelMode, 
    									prcID, 
    									fromSecCurrID, toCurrID, 
    									dateFormat, date.dat,
    									isin.toString(),
    									kmmFile,
    									scriptMode);

    // ----------------------------
    
    doChanges();
    System.err.println("Price after update: " + prc.toString());
    
    kmmFile.writeFile(new File(kmmOutFileName));
    
    System.out.println("OK");
  }

  private void doChanges() throws Exception
  {
    if ( newSource != null )
    {
      System.err.println("Setting source");
      prc.setSource(newSource);
    }

    if ( newValue != null )
    {
      System.err.println("Setting value");
      prc.setValue(newValue);
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

    // <script>
    if ( cmdLine.hasOption("script") )
    {
      scriptMode = true; 
    }
    // System.err.println("Script mode: " + scriptMode);
    
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
    
    if ( ! scriptMode )
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
    
    if ( ! scriptMode )
    	System.err.println("KMyMoney file (out): '" + kmmOutFileName + "'");
    
  	// ---------
  	
    // <prc-sel-mode>
    try
    {
      prcSelMode = CmdLineHelper_Prc.PrcSelectMode.valueOf(cmdLine.getOptionValue("prc-sel-mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <prc-sel-mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Price mode:     " + prcSelMode);

    // <prc-sel-sub-mode>
    if ( cmdLine.hasOption("prc-sel-sub-mode") )
    {
        if ( prcSelMode != CmdLineHelper_Prc.PrcSelectMode.ID )
        {
          System.err.println("<prc-sel-sub-mode> may only be set with <prc-sel-mode> = '" + CmdLineHelper_Prc.PrcSelectMode.ID + "'");
          throw new InvalidCommandLineArgsException();
        }
        
        try
        {
          prcSelSubMode = CmdLineHelper_Prc.PrcSelectSubMode.valueOf(cmdLine.getOptionValue("prc-sel-sub-mode"));
        }
        catch ( Exception exc )
        {
          System.err.println("Could not parse <prc-sel-sub-mode>");
          throw new InvalidCommandLineArgsException();
        }
    }
    else
    {
        if ( prcSelMode == CmdLineHelper_Prc.PrcSelectMode.ID )
        {
          System.err.println("<sec-sel-sub-mode> must be set with <sec-sel-mode> = '" + CmdLineHelper_Prc.PrcSelectMode.ID + "'");
          throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
      System.err.println("Price sub-mode: " + prcSelSubMode);
    
  	// ---------

    // <prc-sel-mode>
    // <price-id>, 
    // <from-sec-curr-id>, <to-curr-id>, <date>,
    // <isin>
    try
	{
		CmdLineHelper_Prc.parsePrcStuffWrap( cmdLine, 
										 prcSelMode, prcSelSubMode,
										 prcID,
										 fromSecCurrID, toCurrID, 
										 dateFormat, date, 
										 isin,
										 scriptMode );
	}
	catch ( Exception exc )
	{
		// TODO Auto-generated catch block
		exc.printStackTrace();
		throw new InvalidCommandLineArgsException();
	}

    // <new-source>
    if ( cmdLine.hasOption("new-source") ) 
    {
      try
      {
    	newSource = KMyMoneyPrice.Source.valueOf( cmdLine.getOptionValue("new-source") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <new-source>");
        throw new InvalidCommandLineArgsException();
      }
    }
    
    if ( ! scriptMode )
    	System.err.println("New source: " + newSource);

    // <new-value>
    if ( cmdLine.hasOption("new-value") ) 
    {
      try
      {
        newValue = new FixedPointNumber( cmdLine.getOptionValue("new-value") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <new-value>");
        throw new InvalidCommandLineArgsException();
      }
    }
    
    if ( ! scriptMode )
    	System.err.println("New value: " + newValue);
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "UpdPrc", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <prc-sel-mode>:");
    for ( CmdLineHelper_Prc.PrcSelectMode elt : CmdLineHelper_Prc.PrcSelectMode.values() )
      System.out.println(" - " + elt);
    
    System.out.println("");
    System.out.println("Valid values for <prc-sel-sub-mode>:");
    for ( CmdLineHelper_Prc.PrcSelectSubMode elt : CmdLineHelper_Prc.PrcSelectSubMode.values() )
      System.out.println(" - " + elt);
    
    System.out.println("");
    System.out.println("Valid values for <price-date-format>:");
    for ( Helper.DateFormat elt : Helper.DateFormat.values() )
      System.out.println(" - " + elt);
  }
}
