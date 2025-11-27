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
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetCurrInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetCurrInfo.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String  kmmFileName = null;
  private static String  symbol      = null;
  
  private static boolean showQuotes = false;
  
  private static boolean scriptMode = false; // ::TODO

  public static void main( String[] args )
  {
    try
    {
      GetCurrInfo tool = new GetCurrInfo ();
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
    Option optFile = Option.builder("f")
      .required()
      .hasArg()
      .argName("file")
      .desc("KMyMoney file")
      .longOpt("kmymoney-file")
      .get();
      
    Option optSymbol = Option.builder("s")
      .hasArg()
      .argName("symbol")
      .desc("Symbol (ISO 4127)")
      .longOpt("symbol")
      .get();
          
    // The convenient ones
    Option optShowQuote = Option.builder("squt")
      .desc("Show quotes")
      .longOpt("show-quotes")
      .get();
            
    options = new Options();
    options.addOption(optFile);
    options.addOption(optSymbol);
    options.addOption(optShowQuote);
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

    KMMQualifCurrID currID = new KMMQualifCurrID(symbol);
    KMyMoneyCurrency curr = kmmFile.getCurrencyByQualifID(currID);
    if ( curr == null )
    {
      System.err.println("Could not find currency with qualif. ID " + currID.toString());
      throw new NoEntryFoundException();
    }

    try
    {
      System.out.println("Qualified ID:      '" + curr.getQualifID() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("Qualified ID:      " + "ERROR");
    }

    try
    {
      System.out.println("Symbol:            '" + curr.getSymbol() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("Symbol:            " + "ERROR");
    }

    try
    {
      System.out.println("toString:          " + curr.toString());
    }
    catch (Exception exc)
    {
      System.out.println("toString:          " + "ERROR");
    }
    
    try
    {
      System.out.println("Name:              '" + curr.getName() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("Name:              " + "ERROR");
    }

    try
    {
      System.out.println("PP:                " + curr.getPP());
    }
    catch (Exception exc)
    {
      System.out.println("PP:                " + "ERROR");
    }

    try
    {
      System.out.println("SAF:               " + curr.getSAF());
    }
    catch (Exception exc)
    {
      System.out.println("SAF:               " + "ERROR");
    }

    try
    {
      System.out.println("SCF:               " + curr.getSCF());
    }
    catch (Exception exc)
    {
      System.out.println("SCF:               " + "ERROR");
    }

    try
    {
      System.out.println("Rounding method:   " + curr.getRoundingMethod());
    }
    catch (Exception exc)
    {
      System.out.println("Rounding method:   " + "ERROR");
    }

    // ---

    if ( showQuotes )
      showQuotes(curr);
  }

  // -----------------------------------------------------------------

  private void showQuotes(KMyMoneyCurrency curr) throws InvalidQualifSecCurrIDException
  {
    System.out.println("");
    System.out.println("Quotes:");

    System.out.println("");
    System.out.println("Number of quotes: " + curr.getQuotes().size());
    
    System.out.println("");
    for (KMyMoneyPrice prc : curr.getQuotes())
    {
      System.out.println(" - " + prc.toString());
    }

    System.out.println("");
    System.out.println("Youngest Quote:");
    System.out.println(curr.getYoungestQuote());
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

    // <symbol>
    try
    {
      symbol = cmdLine.getOptionValue("symbol");
    }
    catch (Exception exc)
    {
      System.err.println("Could not parse <symbol>");
      throw new InvalidCommandLineArgsException();
    }

    if (!scriptMode)
      System.err.println("Symbol:   '" + symbol + "'");

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
		formatter.printHelp( "GetCurrInfo", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
