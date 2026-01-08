package org.kmymoney.tools.xml.gen.simple;

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
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.write.KMyMoneyWritableAccount;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GenAcct extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GenAcct.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String kmmInFileName = null;
  private static String kmmOutFileName = null;
  
  private static String               name      = null;
  private static KMyMoneyAccount.Type type      = null;
  private static KMMQualifSecCurrID   secCurrID = null;
  private static KMMComplAcctID       parentID  = null;
  
  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GenAcct tool = new GenAcct ();
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
      
    Option optName = Option.builder("n")
      .required()
      .hasArg()
      .argName("name")
      .desc("Account name")
      .longOpt("name")
      .get();
    
    Option optType = Option.builder("t")
      .required()
      .hasArg()
      .argName("type")
      .desc("Account type")
      .longOpt("type")
      .get();
    	    
    Option optSecCurr = Option.builder("sc")
      .required()
      .hasArg()
      .argName("sec/curr")
      .desc("Account currency: a (qualified) security or a currency ID")
      .longOpt("security-currency")
      .get();
    	    
    Option optParent = Option.builder("p")
      .required()
      .hasArg()
      .argName("acctid")
      .desc("Parent account ID")
      .longOpt("parent")
      .get();
    	    
    // The convenient ones
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optName);
    options.addOption(optType);
    options.addOption(optSecCurr);
    options.addOption(optParent);
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

    if ( ! isPlausiCheckOK(kmmFile) ) {
    	System.err.println("Did not pass plausi checks");
    	throw new Exception();
    }
    
    KMyMoneyWritableAccount acct = kmmFile.createWritableAccount(type, secCurrID, parentID, name);
    
    System.out.println("Account to write: " + acct.toString());
    kmmFile.writeFile(new File(kmmOutFileName));
    System.out.println("OK");
  }
  
  private boolean isPlausiCheckOK(final KMyMoneyFile kmmFile) {
	    KMyMoneyAccount parentAcct = kmmFile.getAccountByID( parentID );
	    if ( type == KMyMoneyAccount.Type.STOCK &&
	    	 parentAcct.getType() != KMyMoneyAccount.Type.INVESTMENT ) {
	    	System.err.println("Error: <type> = " + type + ", but parent's type is not " + KMyMoneyAccount.Type.INVESTMENT);
	    	return false;
	    } else if ( ( type == KMyMoneyAccount.Type.CHECKING ||
	    		      type == KMyMoneyAccount.Type.SAVINGS ||
	    			  type == KMyMoneyAccount.Type.MONEY_MARKET ||
	    			  type == KMyMoneyAccount.Type.CASH ||
	    			  type == KMyMoneyAccount.Type.CERTIFICATE_DEPOSIT ||
	    			  type == KMyMoneyAccount.Type.INVESTMENT ) &&
	       	        parentAcct.getType() != KMyMoneyAccount.Type.ASSET ) {
	    	System.err.println("Error: <type> = " + type + ", but parent's type is not " + KMyMoneyAccount.Type.ASSET);
	    	return false;
	    } else if ( ( type == KMyMoneyAccount.Type.LOAN ||
	    		      type == KMyMoneyAccount.Type.ASSET_LOAN ) &&
	   	            parentAcct.getType() != KMyMoneyAccount.Type.LIABILITY ) {
	    	System.err.println("Error: <type> = " + type + ", but parent's type is not " + KMyMoneyAccount.Type.LIABILITY);
	    	return false;
	    } else if ( ( type == KMyMoneyAccount.Type.ASSET ||
	    		      type == KMyMoneyAccount.Type.LIABILITY ||
	    		      type == KMyMoneyAccount.Type.INCOME ||
	    		      type == KMyMoneyAccount.Type.EXPENSE ||
	    		      type == KMyMoneyAccount.Type.EQUITY ) &&
		            type != parentAcct.getType() ) {
	    	System.err.println("Error: <type> = " + type + ", but parent's type is not");
	    	return false;
	    }
	    
	    return true;
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
    System.err.println("KMyMoney file (in):          '" + kmmInFileName + "'");
    
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
    System.err.println("KMyMoney file (out):         '" + kmmOutFileName + "'");
    
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
    System.err.println("Name:                        '" + name + "'");
    
    // <type>
    try
    {
      type = KMyMoneyAccount.Type.valueOf( cmdLine.getOptionValue("type") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <type>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Type:                        " + type);
    
    // <security-currency>
    try
    {
      secCurrID = KMMQualifSecCurrID.parse( cmdLine.getOptionValue("security-currency") );
      if ( secCurrID.getType() == KMMQualifSecCurrID.Type.SECURITY &&
    	   type != KMyMoneyAccount.Type.STOCK ) {
          System.err.println("<security-currency> may be set to a security only if <type> = " + KMyMoneyAccount.Type.STOCK + "");
          throw new InvalidCommandLineArgsException();
      } else if ( secCurrID.getType() == KMMQualifSecCurrID.Type.CURRENCY &&
    		      type == KMyMoneyAccount.Type.STOCK ) {
          System.err.println("<security-currency> may be set to a (real) currenccy only if <type> != " + KMyMoneyAccount.Type.STOCK + "");
          throw new InvalidCommandLineArgsException();
      }
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <security-currency>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Account currency (sec/curr): " + secCurrID);
    
    // <parent>
    try
    {
      parentID = new KMMComplAcctID( cmdLine.getOptionValue("parent") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <name>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Parent account ID:           " + parentID);
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GenAcct", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
