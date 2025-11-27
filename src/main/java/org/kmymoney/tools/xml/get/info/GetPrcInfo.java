package org.kmymoney.tools.xml.get.info;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

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
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.tools.CommandLineTool;
import org.kmymoney.tools.xml.helper.CmdLineHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
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
  
  private static String               kmmFileName   = null;
  
  private static CmdLineHelper.PrcSelectMode mode = null;
  
  private static KMMPriceID           prcID         = null;
  private static KMMQualifSecCurrID   fromSecCurrID = null; // for mode = ID only
  private static KMMQualifCurrID      toCurrID      = null; // dto.
  private static KMMSecID             secID         = null; // for mode = SEC_DATE only  
  private static Helper.DateFormat    dateFormat    = null; // for both modes
  private static LocalDate            date          = null; // for both modes
  
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
  	// prcID = new KMMPriceID();
  	secID = new KMMSecID();

//    cfg = new PropertiesConfiguration(System.getProperty("config"));
//    getConfigSettings(cfg);

    // Options
    // The essential ones
    Option optFile = Option.builder("if")
      .required()
      .hasArg()
      .argName("file")
      .desc("KMyMoney file")
      .longOpt("kmymoney-file")
      .get();
    
    // Note: As opposed to the sister project's variant of this program,
    // it is somewhat senseless here to have the user select the mode
    // and the other parameters accordingly. Why? Because KMyMoney does
    // not have a real price ID but rather a price pairs and date-identified
    // entries within a price pair (hell knows why...). The KMMPriceID and
    // KMMPricePairID classes are pseudo-IDs. 
    // Given that, both "variants" of selecting a price are essentially the
    // same. 
    // Still, for the sake of symmetry, we have the selection mode here
    // and everything else analogous to the sister project's program.
    Option optMode = Option.builder("m")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode")
      .longOpt("mode")
      .get();
    	    	      
    Option optFromSecCurr= Option.builder("fr")
      .hasArg()
      .argName("sec/curr")
      .desc("From-security/currency (qualified) (for mode = '" + CmdLineHelper.PrcSelectMode.ID + "' only)")
      .longOpt("from-sec-curr")
      .get();
    	          
    Option optToCurr = Option.builder("to")
      .hasArg()
      .argName("curr")
      .desc("To-currency (qualified) (for mode = '" + CmdLineHelper.PrcSelectMode.ID + "' only)")
      .longOpt("to-curr")
      .get();
    	    
    Option optSecID = Option.builder("sec")
      .hasArg()
      .argName("secid")
      .desc("Security ID (for mode = '" + CmdLineHelper.PrcSelectMode.SEC_DATE + "' only)")
      .longOpt("security-id")
      .get();
    	    	    	          
    Option optDateFormat = Option.builder("df")
      .required() // sic, because needed in both modes
      .hasArg()
      .argName("date-format")
      .desc("Date format")
      .longOpt("date-format")
      .get();
    	            
    Option optDate = Option.builder("dat")
      .required() // sic, because needed in both modes
      .hasArg()
      .argName("date")
      .desc("Date")
      .longOpt("date")
      .get();    	          
          
    // The convenient ones
    // ::EMPTY
            
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optFromSecCurr);
    options.addOption(optToCurr);
    options.addOption(optDateFormat);
    options.addOption(optSecID);
    options.addOption(optDate);
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
    

    KMyMoneyPrice prc = null;
    
    // The following does not really make a difference, but we 
    // stubbornly do as if we did not know about the internal
    // structure of a (pseudo) price (pair) ID here 
    // (cf. comment above).
    if ( mode == CmdLineHelper.PrcSelectMode.ID )
    {
        prcID = new KMMPriceID( fromSecCurrID, toCurrID, date );
        System.out.println("Price ID: " + prcID);
        
    	prc = kmmFile.getPriceByID(prcID);
    	if ( prc == null )
    	{
    		System.err.println("Could not find a price with this ID.");
    		throw new NoEntryFoundException();
    	}
    }
    else if ( mode == CmdLineHelper.PrcSelectMode.SEC_DATE )
    {
        prc = kmmFile.getPriceBySecIDDate(secID, date);
        if ( prc == null )
        {
          System.err.println("Could not find a price matching this security-ID/date.");
          throw new NoEntryFoundException();
        }
    }

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
      System.out.println("Value:             " + prc.getValueFormatted());
    }
    catch (Exception exc)
    {
      System.out.println("Value:             " + "ERROR");
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
    catch (Exception exc)
    {
      System.err.println("Could not parse <kmymoney-file>");
      throw new InvalidCommandLineArgsException();
    }

    if (!scriptMode)
      System.err.println("KMyMoney file: '" + kmmFileName + "'");

    // <mode>
    try
    {
      mode = CmdLineHelper.PrcSelectMode.valueOf(cmdLine.getOptionValue("mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if (!scriptMode)
        System.err.println("Mode:                       " + mode);

    // <from-sec-curr>
    if ( cmdLine.hasOption( "from-sec-curr" ) )
    {
    	if ( mode != CmdLineHelper.PrcSelectMode.ID )
    	{
            System.err.println("<from-sec-curr> may only be set with <mode> = " + CmdLineHelper.PrcSelectMode.ID);
            throw new InvalidCommandLineArgsException();
    	}
    		
    	try
    	{
    		fromSecCurrID = KMMQualifSecCurrID.parse(cmdLine.getOptionValue("from-sec-curr")); 
    	}
    	catch ( Exception exc )
    	{
    		System.err.println("Could not parse <from-sec-curr>");
    		throw new InvalidCommandLineArgsException();
    	}
    }
    else
    {
    	if ( mode == CmdLineHelper.PrcSelectMode.ID )
    	{
            System.err.println("<from-sec-curr> must be set with <mode> = " + CmdLineHelper.PrcSelectMode.ID);
            throw new InvalidCommandLineArgsException();
    	}
    }
    
    if (!scriptMode)
        System.err.println("From-security/currency ID:  " + fromSecCurrID);

    // <to-curr>
    if ( cmdLine.hasOption( "to-curr" ) )
    {
    	if ( mode != CmdLineHelper.PrcSelectMode.ID )
    	{
            System.err.println("<to-curr> may only be set with <mode> = " + CmdLineHelper.PrcSelectMode.ID);
            throw new InvalidCommandLineArgsException();
    	}
    		
    	try
    	{
    		toCurrID = KMMQualifCurrID.parse(cmdLine.getOptionValue("to-curr")); 
    	}
    	catch ( Exception exc )
    	{
    		System.err.println("Could not parse <to-curr>");
    		throw new InvalidCommandLineArgsException();
    	}
    }
    else
    {
    	if ( mode == CmdLineHelper.PrcSelectMode.ID )
    	{
            System.err.println("<to-curr> must be set with <mode> = " + CmdLineHelper.PrcSelectMode.ID);
            throw new InvalidCommandLineArgsException();
    	}
    }
    
    if (!scriptMode)
        System.err.println("To-currency:                " + toCurrID);

    // <security-id>
    if ( cmdLine.hasOption( "security-id" ) )
    {
    	if ( mode != CmdLineHelper.PrcSelectMode.SEC_DATE )
    	{
            System.err.println("<security-id> may only be set with <mode> = " + CmdLineHelper.PrcSelectMode.SEC_DATE);
            throw new InvalidCommandLineArgsException();
    	}
    		
    	try
    	{
    		secID = new KMMSecID(cmdLine.getOptionValue("security-id")); 
    	}
    	catch ( Exception exc )
    	{
    		System.err.println("Could not parse <security-id>");
    		throw new InvalidCommandLineArgsException();
    	}
    }
    else
    {
    	if ( mode == CmdLineHelper.PrcSelectMode.SEC_DATE )
    	{
            System.err.println("<security-id> must be set with <mode> = " + CmdLineHelper.PrcSelectMode.SEC_DATE);
            throw new InvalidCommandLineArgsException();
    	}
    }
    
    if (!scriptMode)
        System.err.println("Security ID:                " + secID);

    // <date-format>
    dateFormat = CmdLineHelper.getDateFormat(cmdLine, "date-format");
    
    if (!scriptMode)
    	System.err.println("Date format:                " + dateFormat);

    // <date>
    try
    {
      date = CmdLineHelper.getDate(cmdLine, "date", dateFormat); 
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <date>");
      throw new InvalidCommandLineArgsException();
    }
    
    if (!scriptMode)
        System.err.println("Date:                       " + date);

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
    System.out.println("Valid values for <mode>:");
    for ( CmdLineHelper.PrcSelectMode elt : CmdLineHelper.PrcSelectMode.values() )
      System.out.println(" - " + elt);
    
    System.out.println("");
    System.out.println("Valid values for <date-format>:");
    for ( Helper.DateFormat elt : Helper.DateFormat.values() )
      System.out.println(" - " + elt);
  }
}
