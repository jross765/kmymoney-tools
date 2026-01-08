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
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetTrxSpltInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetTrxSpltInfo.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String  kmmFileName = null;
  private static String  trxID = null;
  private static String  spltID = null;
  
  private static boolean scriptMode = false; // ::TODO

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GetTrxSpltInfo tool = new GetTrxSpltInfo ();
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
      
    Option optSpltID = Option.builder("splt")
      .required()
      .hasArg()
      .argName("spltid")
      .desc("Transaction-split-ID")
      .longOpt("split-id")
      .get();
    
    // The convenient ones
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optTrxID);
    options.addOption(optSpltID);
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
    
    KMyMoneyTransactionSplit splt = kmmFile.getTransactionSplitByID(new KMMQualifSpltID(trxID, spltID));
    
    try
    {
      System.out.println("Qualif. ID:     " + splt.getQualifID());
    }
    catch ( Exception exc )
    {
      System.out.println("Qualif. ID:     " + "ERROR");
    }
    
    try
    {
      System.out.println("toString:       " + splt.toString());
    }
    catch ( Exception exc )
    {
      System.out.println("toString:       " + "ERROR");
    }
    
    try
    {
      System.out.println("Transaction ID: " + splt.getTransaction().getID());
    }
    catch ( Exception exc )
    {
      System.out.println("Transaction ID: " + "ERROR");
    }
    
    try
    {
      System.out.println("Action:         " + splt.getAction());
    }
    catch ( Exception exc )
    {
      System.out.println("Action:         " + "ERROR");
    }
        
    try
    {
      System.out.println("Recon state:    " + splt.getReconState());
    }
    catch ( Exception exc )
    {
      System.out.println("Recon state:    " + "ERROR");
    }
        
    try
    {
      System.out.println("Number:         '" + splt.getNumber() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Number:         " + "ERROR");
    }
        
    try
    {
      System.out.println("Account ID:     " + splt.getAccountID());
    }
    catch ( Exception exc )
    {
      System.out.println("Account ID:     " + "ERROR");
    }
    
    try
    {
      System.out.println("Payee ID:       " + splt.getPayeeID());
    }
    catch ( Exception exc )
    {
      System.out.println("Payee:          " + "ERROR");
    }
        
    System.out.println("");
    try
    {
      System.out.println("Value:          " + splt.getValue());
    }
    catch ( Exception exc )
    {
      System.out.println("Value:          " + "ERROR");
    }
        
    try
    {
      System.out.println("Value (exact):  " + splt.getValueRat());
    }
    catch ( Exception exc )
    {
      System.out.println("Value (exact):  " + "ERROR");
    }
        
    try
    {
      System.out.println("Value (fmt):    " + splt.getValueFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Value (fmt):    " + "ERROR");
    }
        
    System.out.println("");
    try
    {
      System.out.println("Shares:         " + splt.getShares());
    }
    catch ( Exception exc )
    {
      System.out.println("Quantity:       " + "ERROR");
    }
        
    try
    {
      System.out.println("Shares (exact): " + splt.getSharesRat());
    }
    catch ( Exception exc )
    {
      System.out.println("Shares (exact): " + "ERROR");
    }
        
    try
    {
      System.out.println("Shares (fmt):   " + splt.getSharesFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Shares (fmt):   " + "ERROR");
    }
        
    System.out.println("");
    try
    {
      System.out.println("Price:          " + splt.getPrice());
    }
    catch ( Exception exc )
    {
      System.out.println("Price:          " + "ERROR");
    }
        
    try
    {
      System.out.println("Price (exact):  " + splt.getPriceRat());
    }
    catch ( Exception exc )
    {
      System.out.println("Price (exact):  " + "ERROR");
    }
        
    try
    {
      System.out.println("Price (fmt):    " + splt.getPriceFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Price (fmt):    " + "ERROR");
    }
        
    System.out.println("");
    try
    {
      System.out.println("Memo:           '" + splt.getMemo() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Memo:           " + "ERROR");
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
      System.err.println("KMyMoney file:  '" + kmmFileName + "'");
    
    // <transaction-id>
    try
    {
      trxID = cmdLine.getOptionValue("transaction-id");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <transaction-id>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Transaction ID: " + trxID);
    
    // <split-id>
    try
    {
      spltID = cmdLine.getOptionValue("split-id");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <split-id>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Split ID:       " + spltID);
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GetTrxSpltInfo", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
