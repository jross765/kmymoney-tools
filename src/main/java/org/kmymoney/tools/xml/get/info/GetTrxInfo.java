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
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.base.basetypes.simple.KMMTrxID;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetTrxInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetTrxInfo.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String   kmmFileName = null;
  private static KMMTrxID trxID = null;
  
  private static boolean showSplits = false;
  
  private static boolean scriptMode = false; // ::TODO

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GetTrxInfo tool = new GetTrxInfo ();
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
    // trxID = UUID.randomUUID();

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
      
    Option optTrxID = Option.builder("trx")
      .required()
      .hasArg()
      .argName("trxid")
      .desc("Transaction-ID")
      .longOpt("transaction-id")
      .get();
    
    // The convenient ones
    Option optShowSplt = Option.builder("ssplt")
      .desc("Show splits")
      .longOpt("show-splits")
      .get();
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optTrxID);
    options.addOption(optShowSplt);
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
    
    KMyMoneyTransaction trx = kmmFile.getTransactionByID(trxID);
    
    try
    {
      System.out.println("ID:              " + trx.getID());
    }
    catch ( Exception exc )
    {
      System.out.println("ID:              " + "ERROR");
    }
    
    try
    {
      System.out.println("toString:        " + trx.toString());
    }
    catch ( Exception exc )
    {
      System.out.println("toString:        " + "ERROR");
    }
    
    System.out.println("");
    try
    {
      System.out.println("Balance:         " + trx.getBalance());
    }
    catch ( Exception exc )
    {
      System.out.println("Balance:         " + "ERROR");
    }
    
    try
    {
      System.out.println("Balance (exact): " + trx.getBalanceRat());
    }
    catch ( Exception exc )
    {
      System.out.println("Balance (exact): " + "ERROR");
    }
    
    try
    {
      System.out.println("Balance (fmt):   " + trx.getBalanceFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Balance (fmt):   " + "ERROR");
    }
    
    System.out.println("");
    try
    {
      System.out.println("Sec/Curr:        " + trx.getQualifSecCurrID());
    }
    catch ( Exception exc )
    {
      System.out.println("Sec/Curr:        " + "ERROR");
    }
        
    try
    {
      System.out.println("Date posted:     " + trx.getDatePosted());
    }
    catch ( Exception exc )
    {
      System.out.println("Date posted:     " + "ERROR");
    }
    
    try
    {
      System.out.println("Date entered:    " + trx.getDateEntered());
    }
    catch ( Exception exc )
    {
      System.out.println("Date entered:    " + "ERROR");
    }
        
    try
    {
      System.out.println("Memo:            '" + trx.getMemo() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Memo:            " + "ERROR");
    }

    // ---
        
    if ( showSplits )
      showSplits(trx);
  }

  // -----------------------------------------------------------------

  private void showSplits(KMyMoneyTransaction trx)
  {
    System.out.println("");
    System.out.println("Splits:");
    
    for ( KMyMoneyTransactionSplit splt : trx.getSplits() )
    {
      System.out.println(" - " + splt.toString());
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
    
    // <transaction-id>
    try
    {
      trxID = new KMMTrxID( cmdLine.getOptionValue("transaction-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <transaction-id>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Transaction ID: " + trxID);

    // <show-splits>
    if ( cmdLine.hasOption("show-splits"))
    {
      showSplits = true;
    }
    else
    {
      showSplits = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show splits: " + showSplits);
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GetTrxInfo", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
