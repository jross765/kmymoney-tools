package org.kmymoney.tools.xml.get.info;

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
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
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

public class GetPrcInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetPrcInfo.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String kmmFileName = null;
  
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
  
  private static boolean scriptMode = false;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GetPrcInfo tool = new GetPrcInfo ();
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

    // The convenient ones
    // ::EMPTY

    options = new Options();
    options.addOption(optFile);
    options.addOption(optPrcMode);
    options.addOption(optPrcSubMode);
    options.addOption(optPrcID);
    options.addOption(optPrcFromSecCurr);
    options.addOption(optPrcToCurr);
    options.addOption(optPrcDateFormat);
    options.addOption(optPrcDate);
    options.addOption(optPrcISIN);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
	KMyMoneyFileImpl kmmFile = new KMyMoneyFileImpl(new File(kmmFileName), ! scriptMode);

    KMyMoneyPrice prc = PriceHelper.getPrc(prcSelMode, 
    									prcID, 
    									fromSecCurrID, toCurrID, 
    									dateFormat, date.dat,
    									isin.toString(),
    									kmmFile,
    									scriptMode);

    // ----------------------------

    try
    {
      System.out.println("Parent price pair: '" + prc.getParentPricePair() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("Parent price pair:  " + "ERROR");
    }

    try
    {
      System.out.println("toString:          " + prc.toString());
    }
    catch (Exception exc)
    {
      System.out.println("toString:          " + "ERROR");
    }
    
    try
    {
      System.out.println("From sec/curr:     " + prc.getFromSecCurrQualifID());
    }
    catch (Exception exc)
    {
      System.out.println("From sec/curr:     " + "ERROR");
    }

    try
    {
      System.out.println("To curr:           " + prc.getToCurrencyQualifID());
    }
    catch (Exception exc)
    {
      System.out.println("To curr:           " + "ERROR");
    }

    try
    {
      System.out.println("Date:              " + prc.getDate());
    }
    catch (Exception exc)
    {
      System.out.println("Date:              " + "ERROR");
    }

    try
    {
      System.out.println("Value:             " + prc.getValue());
    }
    catch (Exception exc)
    {
      System.out.println("Value:             " + "ERROR");
    }

    try
    {
      System.out.println("Value (exact):     " + prc.getValueRat());
    }
    catch (Exception exc)
    {
      System.out.println("Value (exact):     " + "ERROR");
    }

    try
    {
      System.out.println("Value (fmt):       " + prc.getValueFormatted());
    }
    catch (Exception exc)
    {
      System.out.println("Value (fmt):       " + "ERROR");
    }

    try
    {
      System.out.println("Source:            " + prc.getSource());
    }
    catch (Exception exc)
    {
      System.out.println("Source:            " + "ERROR");
    }
  }

  // -----------------------------------------------------------------

  @Override
  protected void parseCommandLineArgs(String[] args)
      throws InvalidCommandLineArgsException
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
  }

  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GetPrcInfo", "", options, "", true );
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
