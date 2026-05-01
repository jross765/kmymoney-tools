package org.kmymoney.tools.xml.upd.simple;

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
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.write.KMyMoneyWritableAccount;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.AccountNotFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class UpdAcct extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdAcct.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String kmmInFileName = null;
  private static String kmmOutFileName = null;
  
  private static KMMAcctID acctID = null; // sic, not KMMComplAcctID

  // ---

  private static KMyMoneyWritableAccount acct = null;

  private static String               newName      = null;
  private static String               newMemo      = null;
  private static KMyMoneyAccount.Type newType      = null;
  private static KMMQualifSecCurrID   newSecCurrID = null;
  
  private static boolean scriptMode = false;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      UpdAcct tool = new UpdAcct ();
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
      
    Option optID = Option.builder("acct")
      .required()
      .hasArg()
      .argName("acctid")
      .desc("Account-ID")
      .longOpt("account-id")
      .get();

    Option optName = Option.builder("nam")
      .hasArg()
      .argName("name")
      .desc("Account name (new)")
      .longOpt("new-name")
      .get();
    
    Option optMemo = Option.builder("mem")
      .hasArg()
      .argName("memo")
      .desc("Account memo (new)")
      .longOpt("new-memo")
      .get();
      
    Option optType = Option.builder("t")
      .hasArg()
      .argName("type")
      .desc("Account type (new)")
      .longOpt("new-type")
      .get();
        
    Option optSecCurr = Option.builder("sc")
      .hasArg()
      .argName("sec/curr-id")
      .desc("Security/currency ID (new)")
      .longOpt("new-security-currency-id")
      .get();
      
    // The convenient ones
    Option optScript = Option.builder("sl")
      .desc("Script Mode")
      .longOpt("script")
      .get();            
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optID);
    options.addOption(optName);
    options.addOption(optMemo);
    options.addOption(optType);
    options.addOption(optSecCurr);
    options.addOption(optScript);
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

    // CAUTION: Here, we intentionally do not use AccountHelper.getWrtAcct(),
    // because that would necessitate the use of CmdLineHelper_Acct.parseAcctStuffWrap(),
    // and that makes no sense here because there is only one way to select an
    // account: by its ID (the name arg. is for tne *new* name)
    try 
    {
      acct = kmmFile.getWritableAccountByID(acctID);
      System.err.println("Account before update: " + acct.toString());
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not find/instantiate account with ID '" + acctID + "'");
      throw new AccountNotFoundException();
    }
    
    doChanges();
    System.err.println("Account after update: " + acct.toString());
    
    kmmFile.writeFile(new File(kmmOutFileName));
    
    System.out.println("OK");
  }

  private void doChanges() throws Exception
  {
    if ( newName != null )
    {
      System.err.println("Setting name");
      acct.setName(newName);
    }

    if ( newMemo != null )
    {
      System.err.println("Setting memo");
      acct.setMemo(newMemo);
    }

    if ( newType != null )
    {
      System.err.println("Setting type");
      acct.setType(newType);
    }

    if ( newSecCurrID != null )
    {
      System.err.println("Setting security/currency");
      acct.setQualifSecCurrID(newSecCurrID);
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
    
    // CAUTION: Here, we CmdLineHelper_Acct.parseAcctStuffWrap(),
    // because there is only one way to select an account: by its ID 
    // (the name arg. is for tne *new* name).
    // <account-id>
    try
    {
      acctID = new KMMAcctID( cmdLine.getOptionValue("account-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <account-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Account ID: " + acctID);

    // <new-name>
    if ( cmdLine.hasOption("new-name") ) 
    {
      try
      {
        newName = cmdLine.getOptionValue("new-name").trim();
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <new-name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("New name: '" + newName + "'");

    // <new-memo>
    if ( cmdLine.hasOption("new-memo") ) 
    {
      try
      {
        newMemo = cmdLine.getOptionValue("new-memo").trim();
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <new-memo>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("New memo: '" + newMemo + "'");
    
    // <new-type>
    if ( cmdLine.hasOption("new-type") ) 
    {
      try
      {
        newType = KMyMoneyAccount.Type.valueOf( cmdLine.getOptionValue("new-type") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <new-type>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("New type: '" + newType + "'");

    // <new-security-currency-id>
    if ( cmdLine.hasOption("new-security-currency-id") ) 
    {
      try
      {
        newSecCurrID = KMMQualifSecCurrID.parse( cmdLine.getOptionValue("new-security-currency-id") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <new-security-currency-id>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("New sec/Curr: '" + newSecCurrID + "'");
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "UpdAcct", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <new-type>:");
    for ( KMyMoneyAccount.Type elt : KMyMoneyAccount.Type.values() )
      System.out.println(" - " + elt);
  }
}
