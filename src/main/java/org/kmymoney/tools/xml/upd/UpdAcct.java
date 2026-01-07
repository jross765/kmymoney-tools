package org.kmymoney.tools.xml.upd;

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
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
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
  
  private static String         kmmInFileName = null;
  private static String         kmmOutFileName = null;
  
  private static KMMComplAcctID acctID = null;

  private static String               name = null;
  private static String               descr = null;
  private static KMyMoneyAccount.Type type = null;
  private static KMMQualifSecCurrID   secCurrID = null;

  private static KMyMoneyWritableAccount acct = null;

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
    // acctID = UUID.randomUUID();

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
      
    Option optID = Option.builder("id")
      .required()
      .hasArg()
      .argName("acctid")
      .desc("Account ID")
      .longOpt("account-id")
      .get();
            
    Option optName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Account name")
      .longOpt("name")
      .get();
    
    Option optDescr = Option.builder("desc")
      .hasArg()
      .argName("descr")
      .desc("Account description")
      .longOpt("description")
      .get();
      
    Option optType = Option.builder("t")
      .hasArg()
      .argName("type")
      .desc("Account type")
      .longOpt("type")
      .get();
        
    Option optSecCurr = Option.builder("sec")
      .hasArg()
      .argName("sec/curr-id")
      .desc("Security/currency ID")
      .longOpt("security-currency-id")
      .get();
      
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optID);
    options.addOption(optName);
    options.addOption(optDescr);
    options.addOption(optType);
    options.addOption(optSecCurr);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    KMyMoneyWritableFileImpl kmmFile = new KMyMoneyWritableFileImpl(new File(kmmInFileName), true);

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
    
    doChanges(kmmFile);
    System.err.println("Account after update: " + acct.toString());
    
    kmmFile.writeFile(new File(kmmOutFileName));
    
    System.out.println("OK");
  }

  private void doChanges(KMyMoneyWritableFileImpl kmmFile) throws Exception
  {
    if ( name != null )
    {
      System.err.println("Setting name");
      acct.setName(name);
    }

    if ( descr != null )
    {
      System.err.println("Setting description");
      acct.setMemo(descr);
    }

    if ( type != null )
    {
      System.err.println("Setting type");
      acct.setType(type);
    }

    if ( secCurrID != null )
    {
      System.err.println("Setting security/currency");
      acct.setQualifSecCurrID(secCurrID);
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
    
    // <account-id>
    try
    {
      acctID = new KMMComplAcctID( cmdLine.getOptionValue("account-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <account-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Account ID: " + acctID);

    // <name>
    if ( cmdLine.hasOption("name") ) 
    {
      try
      {
        name = cmdLine.getOptionValue("name");
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Name: '" + name + "'");

    // <description>
    if ( cmdLine.hasOption("description") ) 
    {
      try
      {
        descr = cmdLine.getOptionValue("description");
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <description>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Description: '" + descr + "'");
    
    // <type>
    if ( cmdLine.hasOption("type") ) 
    {
      try
      {
        type = KMyMoneyAccount.Type.valueOf( cmdLine.getOptionValue("type") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <type>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Type: '" + type + "'");

    // <security-currency-id>
    if ( cmdLine.hasOption("security-currency-id") ) 
    {
      try
      {
        secCurrID = KMMQualifSecCurrID.parse( cmdLine.getOptionValue("security-currency-id") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <security-currency-id>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Sec/Curr: '" + secCurrID + "'");
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
    System.out.println("Valid values for <type>:");
    for ( KMyMoneyAccount.Type elt : KMyMoneyAccount.Type.values() )
      System.out.println(" - " + elt);
  }
}
