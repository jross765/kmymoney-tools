package org.kmymoney.tools.xml.get.sonstige;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetStockAcct extends CommandLineTool
{
  // Logger
  private static final Logger LOGGER = LoggerFactory.getLogger(GetStockAcct.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String              kmmFileName = null;
  
  private static Helper.Mode         acctMode    = null;
  private static KMMComplAcctID      acctID      = null;
  private static String              acctName    = null;
  
  private static Helper.CmdtySecSingleSelMode secMode     = null;
  private static KMMSecID            secID       = null;
  private static String              isin        = null;
  private static String              secName     = null;
  
  private static boolean scriptMode = false;

  public static void main( String[] args )
  {
    try
    {
      GetStockAcct tool = new GetStockAcct ();
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
    Option optFile = Option.builder("f")
      .required()
      .hasArg()
      .argName("file")
      .desc("KMyMoney file")
      .longOpt("kmymoney-file")
      .get();
      
    Option optAcctMode = Option.builder("am")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode for account")
      .longOpt("account-mode")
      .get();
      
    Option optSecMode = Option.builder("sm")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode for security")
      .longOpt("security-mode")
      .get();
        
    Option optAcctID = Option.builder("acct")
      .hasArg()
      .argName("acctid")
      .desc("Account-ID")
      .longOpt("account-id")
      .get();
    
    Option optAcctName = Option.builder("an")
      .hasArg()
      .argName("name")
      .desc("Account name (or part of)")
      .longOpt("account-name")
      .get();
      
    Option optSecID = Option.builder("sec")
      .hasArg()
      .argName("ID")
      .desc("Security ID")
      .longOpt("security-id")
      .get();
            
    Option optSecISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("ISIN")
      .longOpt("isin")
      .get();
          
    Option optSecName = Option.builder("sn")
      .hasArg()
      .argName("name")
      .desc("Security name (or part of)")
      .longOpt("security-name")
      .get();
            
    // The convenient ones
    Option optScript = Option.builder("sl")
      .desc("Script Mode")
      .longOpt("script")
      .get();            
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optAcctMode);
    options.addOption(optAcctID);
    options.addOption(optAcctName);
    options.addOption(optSecMode);
    options.addOption(optSecID);
    options.addOption(optSecISIN);
    options.addOption(optSecName);
    options.addOption(optScript);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    KMyMoneyFileImpl kmmFile = new KMyMoneyFileImpl(new File(kmmFileName), true);

    KMyMoneyAccount acct = null;
    
    if (acctMode == Helper.Mode.ID)
    {
      acct = kmmFile.getAccountByID(acctID);
      if (acct == null)
      {
        if ( ! scriptMode )
          System.err.println("Found no account with that name");
        throw new NoEntryFoundException();
      }
    }
    else if (acctMode == Helper.Mode.NAME)
    {
      Collection<KMyMoneyAccount> acctList = null;
      acctList = kmmFile.getAccountsByTypeAndName(KMyMoneyAccount.Type.INVESTMENT, acctName, 
                                                  true, true);
      if (acctList.size() == 0)
      {
        if ( ! scriptMode )
        {
          System.err.println("Could not find accounts matching this name.");
        }
        throw new NoEntryFoundException();
      }
      else if (acctList.size() > 1)
      {
        if ( ! scriptMode )
        {
          System.err.println("Found " + acctList.size() + " accounts with that name.");
          System.err.println("Please specify more precisely.");
        }
        throw new TooManyEntriesFoundException();
      }
      acct = acctList.iterator().next();
    }

    if ( ! scriptMode )
      System.out.println("Account:  " + acct.toString());
    
    // ----------------------------

    KMyMoneySecurity sec = null;
    
    if ( secMode == Helper.CmdtySecSingleSelMode.ID )
    {
      sec = kmmFile.getSecurityByID(secID);
      if ( sec == null )
      {
        if ( ! scriptMode )
          System.err.println("Could not find a security with this ID.");
        throw new NoEntryFoundException();
      }
    }
    else if ( secMode == Helper.CmdtySecSingleSelMode.ISIN )
    {
      sec = kmmFile.getSecurityByCode(isin);
      if ( sec == null )
      {
        if ( ! scriptMode )
          System.err.println("Could not find securities with this ISIN.");
        throw new NoEntryFoundException();
      }
    }
    else if ( secMode == Helper.CmdtySecSingleSelMode.NAME )
    {
      Collection<KMyMoneySecurity> cmdtyList = kmmFile.getSecuritiesByName(secName); 
      if ( cmdtyList.size() == 0 )
      {
        if ( ! scriptMode )
          System.err.println("Could not find securities matching this name.");
        throw new NoEntryFoundException();
      }
      if ( cmdtyList.size() > 1 )
      {
        if ( ! scriptMode )
        {
          System.err.println("Found " + cmdtyList.size() + "securities matching this name.");
          System.err.println("Please specify more precisely.");
        }
        throw new TooManyEntriesFoundException();
      }
      sec = cmdtyList.iterator().next(); // first element
    }
    
    if ( ! scriptMode )
      System.out.println("Security: " + sec.toString());
    
    // ----------------------------
    
    for ( KMyMoneyAccount subAcct : acct.getChildren() ) {
      if ( subAcct.getType() == KMyMoneyAccount.Type.STOCK &&
           subAcct.getQualifSecCurrID().equals(sec.getQualifID()) ) {
          System.out.println(subAcct.getID());
      }
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

    // <script>
    if ( cmdLine.hasOption("script") )
    {
      scriptMode = true; 
    }
    // System.err.println("Script mode: " + scriptMode);
    
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
      System.err.println("KMyMoney file:     '" + kmmFileName + "'");
    
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
    
    if ( ! scriptMode )
      System.err.println("Account mode:  " + acctMode);

    // <security-mode>
    try
    {
      secMode = Helper.CmdtySecSingleSelMode.valueOf(cmdLine.getOptionValue("security-mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <security-mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
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
        acctID = new KMMComplAcctID( cmdLine.getOptionValue("account-id") );
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
    
    if ( ! scriptMode )
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
    
    if ( ! scriptMode )
      System.err.println("Account name:  '" + acctName + "'");

    // <security-id>
    if ( cmdLine.hasOption("security-id") )
    {
      if ( secMode != Helper.CmdtySecSingleSelMode.ID )
      {
        System.err.println("<security-id> must only be set with <security-mode> = '" + Helper.CmdtySecSingleSelMode.ID.toString() + "'");
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
      if ( secMode == Helper.CmdtySecSingleSelMode.ID )
      {
        System.err.println("<security-id> must be set with <security-mode> = '" + Helper.CmdtySecSingleSelMode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("Security ID:   '" + secID + "'");

    // <isin>
    if ( cmdLine.hasOption("isin") )
    {
      if ( secMode != Helper.CmdtySecSingleSelMode.ISIN )
      {
        System.err.println("<isin> must only be set with <security-mode> = '" + Helper.CmdtySecSingleSelMode.ISIN.toString() + "'");
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
      if ( secMode == Helper.CmdtySecSingleSelMode.ISIN )
      {
        System.err.println("<isin> must be set with <security-mode> = '" + Helper.CmdtySecSingleSelMode.ISIN.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("Security ISIN: '" + isin + "'");

    // <security-name>
    if ( cmdLine.hasOption("security-name") )
    {
      if ( secMode != Helper.CmdtySecSingleSelMode.NAME )
      {
        System.err.println("<security-name> must only be set with <security-mode> = '" + Helper.CmdtySecSingleSelMode.NAME.toString() + "'");
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
      if ( secMode == Helper.CmdtySecSingleSelMode.NAME )
      {
        System.err.println("<security-name> must be set with <security-mode> = '" + Helper.CmdtySecSingleSelMode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("Security name: '" + secName + "'");
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GetStockAcct", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <account-mode>:");
    for ( Helper.Mode elt : Helper.Mode.values() )
      System.out.println(" - " + elt);

    System.out.println("");
    System.out.println("Valid values for <security-mode>:");
    for ( Helper.CmdtySecSingleSelMode elt : Helper.CmdtySecSingleSelMode.values() )
      System.out.println(" - " + elt);
  }
}
