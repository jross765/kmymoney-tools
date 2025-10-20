package org.kmymoney.tools.xml.gen.simple;

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
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GenSec extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GenSec.class);
  
  // -----------------------------------------------------------------

  // ::MAGIC
  private static final String TRADING_MARKET_DEFAULT = "XETRA";

  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String kmmInFileName = null;
  private static String kmmOutFileName = null;

  private static String  name     = null;
  private static String  symbol   = null;
  private static String  isin     = null;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GenSec tool = new GenSec ();
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
      .build();
          
    Option optFileOut = Option.builder("of")
      .required()
      .hasArg()
      .argName("file")
      .desc("KMyMoney file (out)")
      .longOpt("kmymoney-out-file")
      .build();
      
    Option optISIN = Option.builder("is")
      .required()
      .hasArg()
      .argName("isin")
      .desc("ISIN")
      .longOpt("isin")
      .build();
          
    Option optName = Option.builder("n")
      .required()
      .hasArg()
      .argName("name")
      .desc("Name")
      .longOpt("name")
      .build();
    
    // The convenient ones
    Option optSymbol = Option.builder("sy")
      .hasArg()
      .argName("symb")
      .desc("Symbol (ticker)")
      .longOpt("symbol")
      .build();

          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optISIN);
    options.addOption(optName);
    options.addOption(optSymbol);
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
    
    KMyMoneyWritableSecurity sec = kmmFile.createWritableSecurity(KMMSecCurr.Type.STOCK, isin, name);
    
    sec.setTradingMarket( TRADING_MARKET_DEFAULT );
    
    if ( symbol != null )
    	sec.setSymbol(symbol);
    
    System.out.println("Security to write: " + sec.toString());
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
    
    // <isin>
    try
    {
      isin = cmdLine.getOptionValue("isin");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <isin>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("ISIN: '" + isin + "'");
    
    // <name>
    try
    {
      name = cmdLine.getOptionValue("name");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <name>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Name: '" + name + "'");
    
    // <symbol>
    if ( cmdLine.hasOption("symbol") )
    {
        try
        {
        	symbol = cmdLine.getOptionValue("symbol");
        }
        catch ( Exception exc )
        {
          System.err.println("Could not parse <symbol>");
          throw new InvalidCommandLineArgsException();
        }
    }
    else
    {
    	symbol = null;
    }
    System.err.println("Symbol: '" + symbol + "'");

  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GenSec", options );
  }
}
