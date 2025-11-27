package org.kmymoney.tools.xml.gen.simple;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.kmymoney.api.Const;
import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.read.KMyMoneySecurity;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(GenSec.class);
  
  // -----------------------------------------------------------------

  // ::MAGIC
  private static final String TRADING_MARKET_DEFAULT = "XETRA";

  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String kmmInFileName = null;
  private static String kmmOutFileName = null;

  private static String     isin      = null;
  private static String     name      = null;
  
  private static KMMSecCurr.Type type = null;
  private static String     symbol    = null;
  private static KMMSecCurr.RoundingMethod roundingMethod = null;
  private static BigInteger saf       = null;
  private static BigInteger pp        = null;

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
      .get();
          
    Option optFileOut = Option.builder("of")
      .required()
      .hasArg()
      .argName("file")
      .desc("KMyMoney file (out)")
      .longOpt("kmymoney-out-file")
      .get();
      
    Option optISIN = Option.builder("is")
      .required()
      .hasArg()
      .argName("isin")
      .desc("ISIN")
      .longOpt("isin")
      .get();
          
    Option optName = Option.builder("n")
      .required()
      .hasArg()
      .argName("name")
      .desc("Name")
      .longOpt("name")
      .get();
    
    // The convenient ones
    Option optType = Option.builder("t")
      .hasArg()
      .argName("type")
      .desc("Type (default " + Const.SEC_TYPE_DEFAULT + ")")
      .longOpt("type")
      .get();
    
    Option optSymbol = Option.builder("sy")
      .hasArg()
      .argName("symb")
      .desc("Symbol (ticker)")
      .longOpt("symbol")
      .get();

    Option optRoundMeth= Option.builder("rm")
      .hasArg()
      .argName("meth")
      .desc("Rounding method (default " + Const.SEC_ROUNDMETH_DEFAULT + ")")
      .longOpt("rounding-method")
      .get();

    Option optSAF = Option.builder("saf")
      .hasArg()
      .argName("num")
      .desc("SAF (default " + Const.SEC_SAF_DEFAULT + ")")
      .longOpt("saf")
      .get();

    Option optPP = Option.builder("pp")
      .hasArg()
      .argName("num")
      .desc("PP (default " + Const.SEC_PP_DEFAULT + ")")
      .longOpt("pp")
      .get();

          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optISIN);
    options.addOption(optName);
    options.addOption(optType);
    options.addOption(optSymbol);
    options.addOption(optRoundMeth);
    options.addOption(optSAF);
    options.addOption(optPP);
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

    // 1) Check whether there already is a security with that ISIN
    KMyMoneySecurity checkSec = kmmFile.getSecurityByCode( isin );
    if ( checkSec != null )
    {
    	LOGGER.error("kernel: Encountered a security with code '" + isin + "' in KMyMoney file");
    	LOGGER.error("kernel: Aborting");
    	System.err.println("Error: There already is a security with code '" + isin + "' in KMyMoney file");
    	System.err.println("Aborting");
    	System.exit( 1 );
    }
    
    // 2) Generate security
    KMyMoneyWritableSecurity sec = kmmFile.createWritableSecurity(type, isin, name);
    
    sec.setTradingMarket( TRADING_MARKET_DEFAULT );
    
    if ( symbol != null )
    	sec.setSymbol(symbol);

    if ( roundingMethod != null )
    	sec.setRoundingMethod( roundingMethod );
    
    if ( saf != null )
    	sec.setSAF(saf);
    
    if ( pp != null )
    	sec.setPP(pp);
    
    // 3) Write to file
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
    
    System.err.println("KMyMoney file (in):  '" + kmmInFileName + "'");
    
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
    
    // --
    
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
    
    System.err.println("ISIN:                '" + isin + "'");
    
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
    
    System.err.println("Name:                '" + name + "'");
    
    // --
    
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
    // Explicit!
    else
    {
    	type = Const.SEC_TYPE_DEFAULT;
    }

    System.err.println("Type:                " + type);

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

    System.err.println("Symbol:              '" + symbol + "'");

    // <rounding-method>
    if ( cmdLine.hasOption("rounding-method") )
    {
        try
        {
        	roundingMethod = KMMSecCurr.RoundingMethod.valueOf( cmdLine.getOptionValue("rounding-method") );
        }
        catch ( Exception exc )
        {
          System.err.println("Could not parse <rounding-method>");
          throw new InvalidCommandLineArgsException();
        }
    }
    // Implicit!
//    else
//    {
//    	roundingMethod = Const.SEC_ROUNDMETH_DEFAULT;
//    }

    System.err.println("Rounding method:     " + roundingMethod);

    // <saf>
    if ( cmdLine.hasOption("saf") )
    {
        try
        {
        	saf = BigInteger.valueOf( Long.parseLong( cmdLine.getOptionValue("saf") ) );
        }
        catch ( Exception exc )
        {
          System.err.println("Could not parse <saf>");
          throw new InvalidCommandLineArgsException();
        }
    }
    // Implicit!
//    else
//    {
//    	saf = Const.SEC_SAF_DEFAULT;
//    }

    System.err.println("SAF:                 " + saf);

    // <pp>
    if ( cmdLine.hasOption("pp") )
    {
        try
        {
        	pp = BigInteger.valueOf( Long.parseLong( cmdLine.getOptionValue("pp") ) );
        }
        catch ( Exception exc )
        {
          System.err.println("Could not parse <pp>");
          throw new InvalidCommandLineArgsException();
        }
    }
    // Implicit!
//    else
//    {
//    	pp = Const.SEC_PP_DEFAULT;
//    }

    System.err.println("PP:                  " + pp);
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GenSec", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <type>:");
    for ( KMMSecCurr.Type elt : KMMSecCurr.Type.values() )
      System.out.println(" - " + elt);
    
    System.out.println("");
    System.out.println("Valid values for <rounding-method>:");
    for ( KMMSecCurr.RoundingMethod elt : KMMSecCurr.RoundingMethod.values() )
      System.out.println(" - " + elt);
  }
}
