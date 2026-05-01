package org.kmymoney.tools.xml.upd.other;

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
import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.write.KMyMoneyWritableAccount;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMTagID;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.AccountNotFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.dateutils.LocalDateHelpers;

public class CloseBooks extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(CloseBooks.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String kmmInFileName = null;
  private static String kmmOutFileName = null;
  
  private static KMMAcctID acctIncID = null; // sic, not KMMComplAcctID
  private static KMMAcctID acctExpID = null; // sic, not KMMComplAcctID

  private static LocalDate closingDate = null; 

  private static KMMTagID tagID = null;

  private static KMyMoneyWritableAccount acctInc = null;
  private static KMyMoneyWritableAccount acctExp = null;
  
  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      CloseBooks tool = new CloseBooks ();
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
      
    Option optAcctIncID = Option.builder("acci")
      .required()
      .hasArg()
      .argName("acctid")
      .desc("Account-ID (equity account for closing income accounts)")
      .longOpt("account-income-id")
      .get();

    Option optAcctExpID = Option.builder("acce")
      .required()
      .hasArg()
      .argName("acctid")
      .desc("Account-ID (equity account for closing expenses accounts)")
      .longOpt("account-expenses-id")
      .get();
      
    Option optDate = Option.builder("dat")
      .required()
      .hasArg()
      .argName("date")
      .desc("Closing date")
      .longOpt("closing-date")
      .get();
    	      
    // The convenient ones
    Option optTagID = Option.builder("tag")
      .required()
      .hasArg()
      .argName("tagid")
      .desc("ID of Tag to be added to each transaction split")
      .longOpt("tag-id")
      .get();
    	      
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optAcctIncID);
    options.addOption(optAcctExpID);
    options.addOption(optDate);
    options.addOption(optTagID);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    KMyMoneyWritableFileImpl kmmFile = new KMyMoneyWritableFileImpl(new File(kmmInFileName), true);

    // CAUTION: Here, we intentionally do not use AccountHelper.getWrtAcct().
    try 
    {
      acctInc = kmmFile.getWritableAccountByID(acctIncID);
      System.err.println("Account before update: " + acctInc.toString());
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not find/instantiate account with ID '" + acctIncID + "'");
      throw new AccountNotFoundException();
    }
    
    if ( acctInc.getType() != KMyMoneyAccount.Type.EQUITY )
    {
        System.err.println("Error: Account " + acctIncID + " is not of type " + KMyMoneyAccount.Type.EQUITY + ": " + acctInc.getType() );
        throw new AccountNotFoundException();
    }
    
    // CAUTION: Here, we intentionally do not use AccountHelper.getWrtAcct().
    try 
    {
      acctExp = kmmFile.getWritableAccountByID(acctExpID);
      System.err.println("Account before update: " + acctExp.toString());
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not find/instantiate account with ID '" + acctExpID + "'");
      throw new AccountNotFoundException();
    }
    
    if ( acctExp.getType() != KMyMoneyAccount.Type.EQUITY )
    {
        System.err.println("Error: Account " + acctExpID + " is not of type " + KMyMoneyAccount.Type.EQUITY + ": " + acctExp.getType() );
        throw new AccountNotFoundException();
    }
    
    closeBooks(kmmFile);
    
    kmmFile.writeFile(new File(kmmOutFileName));
    
    System.out.println("OK");
  }

  // ::TODO: Check whether all income/equity accounts have the same
  // currency. If not, this code will not work properly.
  private void closeBooks(KMyMoneyWritableFile kmmFile) throws Exception
  {
	  for ( KMyMoneyAccount topAcct : kmmFile.getTopAccounts() )
	  {
		  if ( topAcct.getType() == KMyMoneyAccount.Type.INCOME )
		  {
			  closeIncomeAccounts( kmmFile, topAcct );
		  }
		  else if ( topAcct.getType() == KMyMoneyAccount.Type.EXPENSE )
		  {
			  closeExpensesAccounts( kmmFile, topAcct );
		  }
	  }
  }

  private void closeIncomeAccounts(KMyMoneyWritableFile kmmFile, KMyMoneyAccount topAcct)
  {
	  System.out.println("Income accounts: ");
	  KMyMoneyWritableTransaction closingTrx = kmmFile.createWritableTransaction();
	  closingTrx.setDatePosted( closingDate );
	  closingTrx.setDateEntered( LocalDate.now() );
	  closingTrx.setMemo("Buchabschluss (Inc/Trx)");
	  
	  BigFraction sum = BigFraction.ZERO; 
	  int nofSplits = 0;
	  for ( KMyMoneyAccount subAcct : topAcct.getChildrenRecursive() )
	  {
		  System.out.println(" - " + subAcct.getQualifiedName());
		  if ( subAcct.getType() == KMyMoneyAccount.Type.INCOME ) // just in case...
		  {
			  if ( subAcct.getBalanceRat(closingDate).compareTo(BigFraction.ZERO) != 0 )
			  {
				  // Generate new split for closing transaction 
				  System.out.println("   Balance is not zero");
				  System.out.println("   Generating closing split");
				  BigFraction blc = subAcct.getBalanceRat(closingDate);
				  KMyMoneyWritableTransactionSplit newSplt = closingTrx.createWritableSplit(subAcct);
				  newSplt.setValue( blc.negate() );
				  newSplt.setShares( blc.negate() );
				  newSplt.setMemo("Buchabschluss (Inc/Splt)");
				  if ( tagID != null )
					  newSplt.addTagID(tagID);
				  sum = sum.add( blc.negate() );
				  nofSplits++;
			  }
			  else
			  {
				  System.out.println("   Balance is zero");
				  System.out.println("   Omitting");
			  }
		  }
		  else
		  {
			  System.out.println("   Error: This is not an income account");
			  System.out.println("   Omitting");
		  }
	  }
	  
	  // Split on equity account
	  System.out.println("No. of closing splits generated: " + nofSplits);
	  if ( nofSplits > 0 )
	  {
		  System.out.println("Generating last split");
		  KMyMoneyWritableTransactionSplit lastSplt = closingTrx.createWritableSplit(acctInc);
		  lastSplt.setValue( sum.negate() );
		  lastSplt.setShares( sum.negate() );
		  if ( tagID != null )
			  lastSplt.addTagID(tagID);
	  }
	  else
	  {
		  System.out.println("No last split to generate");
	  }
	  System.out.println("New Transaction: " + closingTrx);
  }

  private void closeExpensesAccounts(KMyMoneyWritableFile kmmFile, KMyMoneyAccount topAcct)
  {
	  System.out.println("Expenses accounts: ");
	  KMyMoneyWritableTransaction closingTrx = kmmFile.createWritableTransaction();
	  closingTrx.setDatePosted( closingDate );
	  closingTrx.setDateEntered( LocalDate.now() );
	  closingTrx.setMemo("Buchabschluss (Exp/Trx)");
	  
	  BigFraction sum = BigFraction.ZERO;
	  int nofSplits = 0;
	  for ( KMyMoneyAccount subAcct : topAcct.getChildrenRecursive() )
	  {
		  System.out.println(" - " + subAcct.getQualifiedName());
		  if ( subAcct.getType() == KMyMoneyAccount.Type.EXPENSE ) // just in case...
		  {
			  if ( subAcct.getBalanceRat(closingDate).compareTo(BigFraction.ZERO) != 0 )
			  {
				  // Generate new split for closing transaction 
				  System.out.println("   Balance is not zero");
				  System.out.println("   Generating closing split");
				  BigFraction blc = subAcct.getBalanceRat(closingDate);
				  KMyMoneyWritableTransactionSplit newSplt = closingTrx.createWritableSplit(subAcct);
				  newSplt.setValue( blc.negate() );
				  newSplt.setShares( blc.negate() );
				  newSplt.setMemo("Buchabschluss (Exp/Splt)");
				  if ( tagID != null )
					  newSplt.addTagID(tagID);
				  sum = sum.add( blc.negate() );
				  nofSplits++;
			  }
			  else
			  {
				  System.out.println("   Balance is zero");
				  System.out.println("   Omitting");
			  }
		  }
		  else
		  {
			  System.out.println("   Error: This is not an expenses account");
			  System.out.println("   Omitting");
		  }
	  }
	  
	  // Split on equity account
	  System.out.println("No. of closing splits generated: " + nofSplits);
	  if ( nofSplits > 0 )
	  {
		  System.out.println("Generating last split");
		  KMyMoneyWritableTransactionSplit lastSplt = closingTrx.createWritableSplit(acctExp);
		  lastSplt.setValue( sum.negate() );
		  lastSplt.setShares( sum.negate() );
		  if ( tagID != null )
			  lastSplt.addTagID(tagID);
	  }
	  else
	  {
		  System.out.println("No last split to generate");
	  }
	  System.out.println("New Transaction: " + closingTrx);
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
    
    // ---

    // CAUTION: Here, we CmdLineHelper_Acct.parseAcctStuffWrap(),
    // <account-income-id>
    try
    {
      acctIncID = new KMMAcctID( cmdLine.getOptionValue("account-income-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <account-income-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Account ID (income):   " + acctIncID);

    // <account-expenses-id>
    try
    {
      acctExpID = new KMMAcctID( cmdLine.getOptionValue("account-expenses-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <account-expenses-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Account ID (expenses): " + acctExpID);

    // <closing-date>
    try
    {
      closingDate = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue( "closing-date" ) );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <closing-date>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Stichtag:              " + closingDate);

    // <tag-id>
    if ( cmdLine.hasOption("tag-id") )
    {
        try
        {
          tagID = new KMMTagID ( cmdLine.getOptionValue( "tag-id" ) );
        }
        catch ( Exception exc )
        {
          System.err.println("Could not parse <tag-id>");
          throw new InvalidCommandLineArgsException();
        }
    }
    System.err.println("Tag-ID:                " + tagID);
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "CloseBooks", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
