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
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.tools.CommandLineTool;
import org.kmymoney.tools.xml.helper.CmdLineHelper_Sec;
import org.kmymoney.tools.xml.helper.SecurityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetSecInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetSecInfo.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String  kmmFileName = null;
  
  private static Helper.CmdtySecSingleSelMode secSelMode = null;

  // CAUTION: As opposed to most other tools, the following variables
  // have to be instantiated here.
  
  private static KMMSecID      secID    = new KMMSecID();
  // This one and the following: sic, StringBuffer, not String,
  // for it has to be mutable because of the way the args are parsed.
  private static StringBuffer  isin     = new StringBuffer();
  // Possibly later:
  // private static StringBuffer  wkn      = new StringBuffer();
  // private static StringBuffer  cusip    = new StringBuffer();
  // private static StringBuffer  sedol    = new StringBuffer();
  private static StringBuffer  secName  = new StringBuffer();
  
  private static boolean showQuotes = false;
  
  private static boolean scriptMode = false; // ::TODO

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GetSecInfo tool = new GetSecInfo ();
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

    Option optMode = Option.builder("ssm")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode for security")
      .longOpt("sec-sel-mode")
      .get();

    Option optSecID = Option.builder("sec")
      .hasArg()
      .argName("ID")
      .desc("Security ID " + 
      		"(for <mode> = " + Helper.CmdtySecSingleSelMode.ID + " only)")
      .longOpt("security-id")
      .get();

    Option optISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("ISIN " + 
  		   	"(for <mode> = " + Helper.CmdtySecSingleSelMode.ISIN + " only)")
      .longOpt("isin")
      .get();

    Option optSecName = Option.builder("sn")
      .hasArg()
      .argName("name")
      .desc("Security name (full) " + 
  		    "(for <mode> = " + Helper.CmdtySecSingleSelMode.NAME + " only)")
      .longOpt("security-name")
      .get();

    // The convenient ones
    Option optShowQuote = Option.builder("squt")
      .desc("Show quotes")
      .longOpt("show-quotes")
      .get();
        
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optSecID);
    options.addOption(optISIN);
    options.addOption(optSecName);
    options.addOption(optShowQuote);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    KMyMoneyFileImpl kmmFile = new KMyMoneyFileImpl(new File(kmmFileName), true);

    KMyMoneySecurity sec = SecurityHelper.getSec(secSelMode,
												 secID, isin.toString(), secName.toString(), 
												 kmmFile,
												 scriptMode);
    
    // ----------------------------

    try
    {
      System.out.println("Qualified ID:      '" + sec.getQualifID() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("Qualified ID:      " + "ERROR");
    }

    try
    {
      System.out.println("Code (ISIN):       '" + sec.getCode() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("Code (ISIN):       " + "ERROR");
    }

    try
    {
      System.out.println("toString:          " + sec.toString());
    }
    catch (Exception exc)
    {
      System.out.println("toString:          " + "ERROR");
    }
    
    try
    {
      System.out.println("Type:              " + sec.getType());
    }
    catch (Exception exc)
    {
      System.out.println("Type:              " + "ERROR");
    }

    try
    {
      System.out.println("Symbol:            '" + sec.getSymbol() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("Symbol:            " + "ERROR");
    }

    try
    {
      System.out.println("Name:              '" + sec.getName() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("Name:              " + "ERROR");
    }

    try
    {
      System.out.println("PP:                " + sec.getPP());
    }
    catch (Exception exc)
    {
      System.out.println("PP:                " + "ERROR");
    }

    try
    {
      System.out.println("SAF:               " + sec.getSAF());
    }
    catch (Exception exc)
    {
      System.out.println("SAF:               " + "ERROR");
    }

    try
    {
      System.out.println("Rounding method:   " + sec.getRoundingMethod());
    }
    catch (Exception exc)
    {
      System.out.println("Rounding method:   " + "ERROR");
    }

    try
    {
      System.out.println("Trading currency:  " + sec.getTradingCurrency());
    }
    catch (Exception exc)
    {
      System.out.println("Trading currency:  " + "ERROR");
    }

    try
    {
      System.out.println("Trading market:    '" + sec.getTradingMarket() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("Trading market:    " + "ERROR");
    }

    // ---

    if ( showQuotes )
      showQuotes(sec);
  }

  // -----------------------------------------------------------------

  private void showQuotes(KMyMoneySecurity sec)
  {
    System.out.println("");
    System.out.println("Quotes:");

    System.out.println("");
    System.out.println("Number of quotes: " + sec.getQuotes().size());
    
    System.out.println("");
    for ( KMyMoneyPrice prc : sec.getQuotes() )
    {
      System.out.println(" - " + prc.toString());
    }

    System.out.println("");
    System.out.println("Youngest Quote:");
    System.out.println(sec.getYoungestQuote());
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
    catch (Exception exc)
    {
      System.err.println("Could not parse <kmymoney-file>");
      throw new InvalidCommandLineArgsException();
    }

    if (!scriptMode)
      System.err.println("KMyMoney file: '" + kmmFileName + "'");

  	// ---------
  	
    // <sec-sel-mode>
    try
    {
      secSelMode = Helper.CmdtySecSingleSelMode.valueOf(cmdLine.getOptionValue("sec-sel-mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <sec-sel-mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Security mode:         " + secSelMode);

  	// ---------
  	
    // <mode>, <security-id>, <isin>, <name>
    CmdLineHelper_Sec.parseSecStuffWrap( cmdLine,
    									secSelMode, null,
    									secID, isin, secName,
    									scriptMode );

    // <show-quotes>
    if (cmdLine.hasOption("show-quotes"))
    {
      showQuotes = true;
    }
    else
    {
      showQuotes = false;
    }

    if (!scriptMode)
      System.err.println("Show quotes: " + showQuotes);
  }

  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GetSecInfo", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <sec-sel-mode>:");
    for ( Helper.CmdtySecSingleSelMode elt : Helper.CmdtySecSingleSelMode.values() )
      System.out.println(" - " + elt);
  }
}
