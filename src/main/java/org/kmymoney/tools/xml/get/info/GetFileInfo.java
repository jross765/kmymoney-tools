package org.kmymoney.tools.xml.get.info;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetFileInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetFileInfo.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String  kmmFileName = null;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GetFileInfo tool = new GetFileInfo ();
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
      .build();

    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFile);
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
    
    printMetaInfoAndStats(kmmFile);
    printRootAcct(kmmFile);
    printTopAccts(kmmFile);
    printStatement(kmmFile);
  }

  // -----------------------------------------------------------------

  private void printMetaInfoAndStats(KMyMoneyFileImpl kmmFile)
  {
    System.out.println("");
    System.out.println("Meta Info and Stats:");
    System.out.println(kmmFile.toString());
  }

  private void printRootAcct(KMyMoneyFileImpl kmmFile)
  {
    System.out.println("");
    System.out.println("Root Account:");
    
    System.out.println("(none)");
  }

  private void printTopAccts(KMyMoneyFileImpl kmmFile)
  {
    System.out.println("");
    System.out.println("Top Accounts:");
    
    for ( KMyMoneyAccount acct : kmmFile.getTopAccounts() ) {
      System.out.println(" - " + acct.toString());
    }
  }

  private void printStatement(KMyMoneyFileImpl kmmFile)
  {
    System.out.println("");
    System.out.println("Financial Statement:");
    
    for ( KMyMoneyAccount acct : kmmFile.getTopAccounts() ) {
      if ( acct.getType() == KMyMoneyAccount.Type.ASSET )
        System.out.print("  Assets:      ");
      else if ( acct.getType() == KMyMoneyAccount.Type.LIABILITY )
        System.out.print("  Liabilities: ");
      else if ( acct.getType() == KMyMoneyAccount.Type.INCOME )
        System.out.print("  Income:      ");
      else if ( acct.getType() == KMyMoneyAccount.Type.EXPENSE )
        System.out.print("  Expenses:    ");
      else if ( acct.getType() == KMyMoneyAccount.Type.EQUITY )
        System.out.print("  Equity:      ");
        
      System.out.println(acct.getBalanceRecursiveFormatted());
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
    
    System.err.println("KMyMoney file:      '" + kmmFileName + "'");
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GetFileInfo", options );
  }
}
