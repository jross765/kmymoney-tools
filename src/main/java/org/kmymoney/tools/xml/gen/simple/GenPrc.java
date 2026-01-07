package org.kmymoney.tools.xml.gen.simple;

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
import org.kmymoney.api.read.impl.KMyMoneyPricePairImpl;
import org.kmymoney.api.write.KMyMoneyWritablePrice;
import org.kmymoney.api.write.KMyMoneyWritablePricePair;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.tools.CommandLineTool;
import org.kmymoney.tools.xml.helper.CmdLineHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class GenPrc extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GenPrc.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String kmmInFileName = null;
  private static String kmmOutFileName = null;

  private static KMMQualifSecCurrID   fromSecCurrID = null;
  private static KMMQualifCurrID      toCurrID = null;
  private static Helper.DateFormat    dateFormat    = null;
  private static LocalDate            date = null;
  private static FixedPointNumber     value = null;
  private static KMyMoneyPrice.Source source = null;

  public static void main( String[] args )
  {
    try
    {
      GenPrc tool = new GenPrc ();
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
    // invcID = UUID.randomUUID();

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
      
    Option optFromSecCurr= Option.builder("f")
      .required()
      .hasArg()
      .argName("sec/curr")
      .desc("From-commodity/currency")
      .longOpt("from-sec-curr")
      .get();
          
    Option optToCurr = Option.builder("t")
      .required()
      .hasArg()
      .argName("curr")
      .desc("To-currency")
      .longOpt("to-curr")
      .get();
    
    Option optDateFormat = Option.builder("df")
      .hasArg()
      .argName("date-format")
      .desc("Date format")
      .longOpt("date-format")
      .get();
            
    Option optDate = Option.builder("dat")
      .required()
      .hasArg()
      .argName("date")
      .desc("Date")
      .longOpt("date")
      .get();
          
    Option optValue = Option.builder("v")
      .required()
      .hasArg()
      .argName("value")
      .desc("Value")
      .longOpt("value")
      .get();
            
    // The convenient ones
    Option optSource = Option.builder("src")
      .hasArg()
      .argName("source")
      .desc("Source")
      .longOpt("source")
      .get();
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optFromSecCurr);
    options.addOption(optToCurr);
    options.addOption(optDateFormat);
    options.addOption(optDate);
    options.addOption(optValue);
    options.addOption(optSource);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    KMyMoneyWritableFileImpl kmmFile = new KMyMoneyWritableFileImpl(new File(kmmInFileName), true);

    KMMPricePairID prcPrID = new KMMPricePairID(fromSecCurrID, toCurrID);
    KMyMoneyWritablePricePair prcPr = kmmFile.getWritablePricePairByID(prcPrID);
    if ( prcPr == null ) {
    	System.err.println("Price pair '" + prcPrID + "' does not exist in KMyMoney file yet.");
    	System.err.println("Will generate it.");
        prcPr = kmmFile.createWritablePricePair(fromSecCurrID, toCurrID);
    } else {
    	System.err.println("Price pair '" + prcPrID + "' already exists in KMyMoney file.");
    	System.err.println("Will take that one.");
    }
    
    KMyMoneyWritablePrice prc = kmmFile.createWritablePrice((KMyMoneyPricePairImpl) prcPr, date);
    // prc.setParentPricePair(prcPr);
    // prc.setDate(date);
    prc.setValue(value);
    prc.setSource(source);
    
    System.out.println("Price to write: " + prc.toString());
    kmmFile.writeFile(new File(kmmOutFileName));
    System.out.println("OK");
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
    
    // <from-sec-curr>
    try
    {
      fromSecCurrID = KMMQualifSecCurrID.parse(cmdLine.getOptionValue("from-sec-curr")); 
      System.err.println("from-sec-curr: " + fromSecCurrID);
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <from-sec-curr>");
      throw new InvalidCommandLineArgsException();
    }
    
    // <to-curr>
    try
    {
      toCurrID = KMMQualifCurrID.parse(cmdLine.getOptionValue("to-curr")); 
      System.err.println("to-curr: " + toCurrID);
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <to-curr>");
      throw new InvalidCommandLineArgsException();
    }
    
    // <date-format>
    dateFormat = CmdLineHelper.getDateFormat(cmdLine, "date-format");
    System.err.println("date-format: " + dateFormat);

    // <date>
    try
    {
      date = CmdLineHelper.getDate(cmdLine, "date", dateFormat); 
      System.err.println("date: " + date);
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <date>");
      throw new InvalidCommandLineArgsException();
    }
    
    // <value>
    try
    {
      value = new FixedPointNumber( Double.parseDouble( cmdLine.getOptionValue("value") ) ) ; 
      System.err.println("value: " + value);
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <name>");
      throw new InvalidCommandLineArgsException();
    }
    
    // <source>
    if ( cmdLine.hasOption("source") )
    {
      try
      {
        source = KMyMoneyPrice.Source.valueOf( cmdLine.getOptionValue("source") ); 
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      source = KMyMoneyPrice.Source.USER;
    }
    System.err.println("source: " + source);
    
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GenPrc", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <source>:");
    for ( KMyMoneyPrice.Source elt : KMyMoneyPrice.Source.values() )
      System.out.println(" - " + elt);
  }
}
