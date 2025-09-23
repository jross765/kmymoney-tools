package org.kmymoney.tools.xml.gen.complex;

import java.io.File;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.write.KMyMoneyWritableAccount;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.apiext.secacct.SecuritiesAccountManager;
import org.kmymoney.apiext.secacct.WritableSecuritiesAccountManager;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GenStockAcct extends CommandLineTool
{
  enum BookMode {
	  SINGLE_TRX,
	  LISTFILE
  }

  // -----------------------------------------------------------------

  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GenStockAcct.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  // ------------------------------

  private static String           kmmInFileName = null;
  private static String           kmmOutFileName = null;
  private static KMyMoneyWritableFileImpl kmmFile = null;
		  
  // ------------------------------

  private static Helper.Mode           acctMode     = null;
  private static KMMAcctID             acctID       = null;
  private static String                acctName     = null;
  
  private static Helper.CmdtySecMode   secMode      = null;
  private static KMMSecID              secID        = null;
  private static String                isin         = null;
  private static String                secName      = null;

  // ------------------------------

  // batch-mode:
  private static boolean    silent           = false;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GenStockAcct tool = new GenStockAcct ();
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
    // cfg = new PropertiesConfiguration(System.getProperty("config"));
    // getConfigSettings(cfg);

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
    
    Option optAcctMode = Option.builder("am")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode for account")
      .longOpt("account-mode")
      .build();
    	      
    Option optAcctID = Option.builder("acct")
      .hasArg()
      .argName("acctid")
      .desc("Account-ID")
      .longOpt("account-id")
      .build();
    	    
    Option optAcctName = Option.builder("an")
      .hasArg()
      .argName("name")
      .desc("Account name (or part of)")
      .longOpt("account-name")
      .build();
    	      
    Option optSecMode = Option.builder("sm")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode for security")
      .longOpt("security-mode")
      .build();
    	    	        
    Option optSecID = Option.builder("sec")
      .hasArg()
      .argName("ID")
      .desc("Security ID")
      .longOpt("security-id")
      .build();
    	            
    Option optSecISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("ISIN")
      .longOpt("isin")
      .build();
    	          
    Option optSecName = Option.builder("sn")
      .hasArg()
      .argName("name")
      .desc("Security name (or part of)")
      .longOpt("security-name")
      .build();
    
    // ---
    	    
    Option optSilent = Option.builder("sl")
      .desc("Silent mode")
      .longOpt("silent")
      .build();

    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optAcctMode );
    options.addOption(optAcctID);
    options.addOption(optAcctName);
    options.addOption(optSecMode);
    options.addOption(optSecID);
    options.addOption(optSecISIN);
    options.addOption(optSecName);
    options.addOption(optSilent);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
	  kmmFile = new KMyMoneyWritableFileImpl(new File(kmmInFileName));

	  KMyMoneyWritableAccount acct = getSecAccount();
	  KMyMoneySecurity sec = getSecurity();
	  
	  WritableSecuritiesAccountManager secAcctMgr = new WritableSecuritiesAccountManager(acct);
	  if ( stockAcctAlreadyExists(secAcctMgr, sec) )
	  {
		  System.err.println("Error: Stock account already exists");
		  throw new IllegalStateException("Stock account already exists");
	  }
	  
	  KMyMoneyWritableAccount newStockAcct = secAcctMgr.genShareAcct( sec );
	  System.out.println("Stock account generated: " + newStockAcct);
	  
	  // ---

	  kmmFile.writeFile(new File(kmmOutFileName));
		    
	  if ( ! silent )
		  System.out.println("OK");
  }
  
  private KMyMoneyWritableAccount getSecAccount() throws Exception
  {
    KMyMoneyWritableAccount acct = null;
    
    if (acctMode == Helper.Mode.ID)
    {
      acct = kmmFile.getWritableAccountByID(acctID);
      if (acct == null)
      {
        if ( ! silent )
          System.err.println("Found no account with that name");
        throw new NoEntryFoundException();
      }
    }
    else if (acctMode == Helper.Mode.NAME)
    {
      Collection<KMyMoneyAccount> acctList = null;
      acctList = kmmFile.getAccountsByTypeAndName(KMyMoneyAccount.Type.INVESTMENT, acctName, 
    		  									   true, true);
      if ( acctList.size() == 0 )
      {
        if ( ! silent )
        {
          System.err.println("Could not find accounts matching this name.");
        }
        throw new NoEntryFoundException();
      }
      else if ( acctList.size() > 1 )
      {
        if ( ! silent )
        {
          System.err.println("Found " + acctList.size() + " accounts with that name.");
          System.err.println("Please specify more precisely.");
        }
        throw new TooManyEntriesFoundException();
      }
      // No:
      // acct = acctList.iterator().next();
      acct = kmmFile.getWritableAccountByID(acctList.iterator().next().getID());
    }

    if ( ! silent )
      System.out.println("Account:  " + acct.toString());
    
    return acct;
  }
  
  private KMyMoneySecurity getSecurity() throws Exception
  {
    KMyMoneySecurity sec = null;
    
    if ( secMode == Helper.CmdtySecMode.ID )
    {
      sec = kmmFile.getSecurityByID(secID);
      if ( sec == null )
      {
        if ( ! silent )
          System.err.println("Could not find a security with this ID.");
        throw new NoEntryFoundException();
      }
    }
    else if ( secMode == Helper.CmdtySecMode.ISIN )
    {
      sec = kmmFile.getSecurityByCode(isin);
      if ( sec == null )
      {
        if ( ! silent )
          System.err.println("Could not find securities with this ISIN.");
        throw new NoEntryFoundException();
      }
    }
    else if ( secMode == Helper.CmdtySecMode.NAME )
    {
      Collection<KMyMoneySecurity> secList = kmmFile.getSecuritiesByName(secName); 
      if ( secList.size() == 0 )
      {
        if ( ! silent )
          System.err.println("Could not find securities matching this name.");
        throw new NoEntryFoundException();
      }
      else if ( secList.size() > 1 )
      {
        if ( ! silent )
        {
          System.err.println("Found " + secList.size() + "securities matching this name.");
          System.err.println("Please specify more precisely.");
        }
        throw new TooManyEntriesFoundException();
      }
      sec = secList.iterator().next(); // first element
    }
    
    if ( ! silent )
      System.out.println("Security: " + sec.toString());

    return sec;
  }
  
  private boolean stockAcctAlreadyExists(SecuritiesAccountManager secAcctMgr, KMyMoneySecurity sec)
  {
	  for ( KMyMoneyAccount acct : secAcctMgr.getShareAccts() )
	  {
		  if ( acct.getQualifSecCurrID().toString().equals( sec.getQualifID().toString() ) ) // Important: toString()
		  {
			  return true;
		  }
	  }
	  
	  return false;
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

    // <silent>
    if (cmdLine.hasOption("silent"))
    {
      silent = true;
    }
    else
    {
      silent = false;
    }
    if (! silent)
      System.err.println("silent:              " + silent);
    
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
    if (! silent)
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
    if (! silent)
    	System.err.println("KMyMoney file (out): '" + kmmOutFileName + "'");
    
    // <account-mode>
    try
    {
      acctMode = Helper.Mode.valueOf(cmdLine.getOptionValue("account-mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <account-mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! silent )
      System.err.println("Account mode:  " + acctMode);

    // <security-mode>
    try
    {
      secMode = Helper.CmdtySecMode.valueOf(cmdLine.getOptionValue("security-mode"));
      if ( secMode == Helper.CmdtySecMode.TYPE )
      {
    	  // sic, not valid
          System.err.println("Could not parse <security-mode>");
          throw new InvalidCommandLineArgsException();
      }
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <security-mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! silent )
      System.err.println("Security mode: " + secMode);

    // <account-id>
    if ( cmdLine.hasOption("account-id") )
    {
      if ( acctMode != Helper.Mode.ID )
      {
        System.err.println("<account-id> must only be set with <account-mode> = '" + Helper.Mode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        acctID = new KMMAcctID( cmdLine.getOptionValue("account-id") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <account-id>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( acctMode == Helper.Mode.ID )
      {
        System.err.println("<account-id> must be set with <account-mode> = '" + Helper.Mode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }      
    }
    
    if ( ! silent )
      System.err.println("Account ID:    '" + acctID + "'");

    // <account-name>
    if ( cmdLine.hasOption("account-name") )
    {
      if ( acctMode != Helper.Mode.NAME )
      {
        System.err.println("<account-name> must only be set with <account-mode> = '" + Helper.Mode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        acctName = cmdLine.getOptionValue("account-name");
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( acctMode == Helper.Mode.NAME )
      {
        System.err.println("<account-name> must be set with <account-mode> = '" + Helper.Mode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }      
    }
    
    if ( ! silent )
      System.err.println("Account name:  '" + acctName + "'");

    // <security-id>
    if ( cmdLine.hasOption("security-id") )
    {
      if ( secMode != Helper.CmdtySecMode.ID )
      {
        System.err.println("<security-id> must only be set with <security-mode> = '" + Helper.CmdtySecMode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        secID = new KMMSecID( cmdLine.getOptionValue("security-id") );
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <security-id>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( secMode == Helper.CmdtySecMode.ID )
      {
        System.err.println("<security-id> must be set with <security-mode> = '" + Helper.CmdtySecMode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!silent)
      System.err.println("Security ID:   '" + secID + "'");

    // <isin>
    if ( cmdLine.hasOption("isin") )
    {
      if ( secMode != Helper.CmdtySecMode.ISIN )
      {
        System.err.println("<isin> must only be set with <security-mode> = '" + Helper.CmdtySecMode.ISIN.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        isin = cmdLine.getOptionValue("isin");
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <isin>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( secMode == Helper.CmdtySecMode.ISIN )
      {
        System.err.println("<isin> must be set with <security-mode> = '" + Helper.CmdtySecMode.ISIN.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!silent)
      System.err.println("Security ISIN: '" + isin + "'");

    // <security-name>
    if ( cmdLine.hasOption("security-name") )
    {
      if ( secMode != Helper.CmdtySecMode.NAME )
      {
        System.err.println("<security-name> must only be set with <security-mode> = '" + Helper.CmdtySecMode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        secName = cmdLine.getOptionValue("security-name");
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <security-name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( secMode == Helper.CmdtySecMode.NAME )
      {
        System.err.println("<security-name> must be set with <security-mode> = '" + Helper.CmdtySecMode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!silent)
      System.err.println("Security name: '" + secName + "'");
  }

  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GenStockAcct", options );
    
    System.out.println("");
    System.out.println("Valid values for <account-mode>:");
    for ( Helper.Mode elt : Helper.Mode.values() )
      System.out.println(" - " + elt);

    System.out.println("");
    System.out.println("Valid values for <security-mode>:");
    for ( Helper.CmdtySecMode elt : Helper.CmdtySecMode.values() )
    {
      if ( elt != Helper.CmdtySecMode.TYPE ) // sic
        System.out.println(" - " + elt);
    }
  }
}
