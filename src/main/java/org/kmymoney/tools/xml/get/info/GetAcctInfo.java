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
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.aux.KMMAccountReconciliation;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.tools.CommandLineTool;
import org.kmymoney.tools.xml.helper.AccountHelper;
import org.kmymoney.tools.xml.helper.CmdLineHelper_Acct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetAcctInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetAcctInfo.class);
  
  // -----------------------------------------------------------------

  // ::MAGIC
  private static final String RECONCILIATION_HISTORY_KEY = "reconciliationHistory";

  // ------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String  kmmFileName  = null;

  private static Helper.Mode acctSelMode = null;

  // CAUTION: As opposed to most other tools, the following variables
  // have to be instantiated here.
  
  private static KMMAcctID     acctID   = new KMMAcctID(); // KMMComplAcctID would be poassible...
  // The following: sic, StringBuffer, not String,
  // for it has to be mutable because of the way the args are parsed.
  private static StringBuffer  acctName = new StringBuffer();
  
  private static boolean showParents  = false;
  private static boolean showChildren = false;
  private static boolean showTrx      = false;
  private static boolean showRcn      = false;
  
  private static boolean scriptMode = false; // ::TODO

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      GetAcctInfo tool = new GetAcctInfo ();
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
      
    Option optAcctMode = Option.builder("asm")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode for account")
      .longOpt("acct-sel-mode")
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

    // The convenient ones
    Option optShowPrnt = Option.builder("sprnt")
      .desc("Show parents")
      .longOpt("show-parents")
      .get();
        
    Option optShowChld = Option.builder("schld")
      .desc("Show children")
      .longOpt("show-children")
      .get();
          
    Option optShowTrx = Option.builder("strx")
      .desc("Show transactions")
      .longOpt("show-transactions")
      .get();
          
    Option optShowRcn = Option.builder("srcn")
      .desc("Show reconciliations")
      .longOpt("show-reconciliations")
      .get();

    options = new Options();
    options.addOption(optFile);
    options.addOption(optAcctMode);
    options.addOption(optAcctID);
    options.addOption(optAcctName);
    options.addOption(optShowPrnt);
    options.addOption(optShowChld);
    options.addOption(optShowTrx);
    options.addOption(optShowRcn);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    KMyMoneyFileImpl kmmFile = new KMyMoneyFileImpl(new File(kmmFileName), ! scriptMode);

    KMyMoneyAccount acct = AccountHelper.getAcct(acctSelMode, 
												 acctID, acctName.toString(), 
												 kmmFile,
												 scriptMode);

    printAcctInfo(acct, 0);
  }

  private void printAcctInfo(KMyMoneyAccount acct, int depth)
  {
    System.out.println("Depth:           " + depth);

    try
    {
      System.out.println("ID:              " + acct.getID());
    }
    catch ( Exception exc )
    {
      System.out.println("ID:              " + "ERROR");
    }
    
    try
    {
      System.out.println("toString:        " + acct.toString());
    }
    catch ( Exception exc )
    {
      System.out.println("toString:        " + "ERROR");
    }
    
    try
    {
      System.out.println("Type:            " + acct.getType());
    }
    catch ( Exception exc )
    {
      System.out.println("Type:            " + "ERROR");
    }
    
    try
    {
      System.out.println("Name:            '" + acct.getName() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Name:            " + "ERROR");
    }
    
    try
    {
      System.out.println("Qualified name:  '" + acct.getQualifiedName() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Qualified name:  " + "ERROR");
    }
    
    try
    {
      System.out.println("Parent ID:       " + acct.getParentAccountID());
    }
    catch ( Exception exc )
    {
      System.out.println("Parent ID:       " + "ERROR");
    }
    
    try
    {
      System.out.println("Closed:          " + acct.isClosed());
    }
    catch ( Exception exc )
    {
      System.out.println("Closed:          " + "ERROR");
    }
    
    try
    {
      System.out.println("Memo:            '" + acct.getMemo() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Memo:            " + "ERROR");
    }
    
    try
    {
      System.out.println("Sec/Curr:        '" + acct.getQualifSecCurrID() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Sec/Curr:        " + "ERROR");
    }
    
    System.out.println("");
    try
    {
      System.out.println("Balance:         " + acct.getBalance());
    }
    catch ( Exception exc )
    {
      System.out.println("Balance:         " + "ERROR");
    }

    try
    {
      System.out.println("Balance (exact): " + acct.getBalanceRat());
    }
    catch ( Exception exc )
    {
      System.out.println("Balance (exact): " + "ERROR");
    }

    try
    {
      System.out.println("Balance (fmt):   " + acct.getBalanceFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Balance (fmt):   " + "ERROR");
    }

    System.out.println("");
    try
    {
      System.out.println("Balance recurs.:         " + acct.getBalanceRecursive());
    }
    catch ( Exception exc )
    {
      System.out.println("Balance recurs.: " + "ERROR");
    }

    try
    {
      System.out.println("Balance recurs. (exact): " + acct.getBalanceRecursiveRat());
    }
    catch ( Exception exc )
    {
      System.out.println("Balance recurs. (exact): " + "ERROR");
    }

    try
    {
      System.out.println("Balance recurs. (fmt):   " + acct.getBalanceRecursiveFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Balance recurs. (fmt):   " + "ERROR");
    }

    // ---
        
    if ( showParents )
      showParents(acct, depth);
    
    if ( showChildren )
      showChildren(acct, depth);
    
    if ( showTrx )
      showTransactions(acct);
    
    if ( showRcn )
      showReconciliations(acct);
  }

  // -----------------------------------------------------------------

  private void showParents(KMyMoneyAccount acct, int depth)
  {
    if ( depth <= 0 &&
         ! acct.isRootAccount() ) 
    {
      System.out.println("");
      System.out.println(">>> BEGIN Parent Account");
      printAcctInfo(acct.getParentAccount(), depth - 1);
      System.out.println("<<< END Parent Account");
    }
  }
  
  private void showChildren(KMyMoneyAccount acct, int depth)
  {
    System.out.println("");
    System.out.println("Children (1st Level, Overview):");
    
    for ( KMyMoneyAccount chld : acct.getChildren() )
    {
        System.out.println(" - " + chld.toString());
    }
    
    System.out.println("");
    System.out.println("Children (Recursive, Details):");
    
    if ( depth >= 0 )
    {
      System.out.println(">>> BEGIN Child Account");
      for ( KMyMoneyAccount childAcct : acct.getChildren())
      {
        printAcctInfo(childAcct, depth + 1);
      }
      System.out.println("<<< END Child Account");
    }
  }
  
  private void showTransactions(KMyMoneyAccount acct)
  {
    System.out.println("");
    System.out.println("Transactions:");
    
    for ( KMyMoneyTransaction trx : acct.getTransactions() )
    {
      System.out.println(" - " + trx.toString());
    }
  }

  private void showReconciliations(KMyMoneyAccount acct)
  {
    System.out.println("");
    System.out.println("Reconciliations (primary):");
    
    for ( KMMAccountReconciliation rcn : acct.getReconciliations() )
    {
      System.out.println(" - " + rcn.toString());
    }
    
    // ---
    
    System.out.println("");
    System.out.println("Reconciliation History (redundant):");
    
    String reconHistStr = acct.getUserDefinedAttribute(RECONCILIATION_HISTORY_KEY);
    String[] reconHistArr = reconHistStr.split(";");
    for ( String rcnHistPairStr : reconHistArr )
    {
//      String[] reconHistPairArr = rcnHistPairStr.split(":");
//      System.out.println(" - " + reconHistPairArr[0] + ";" + reconHistPairArr[1]);
      System.out.println(" - " + rcnHistPairStr);
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
      System.err.println("KMyMoney file:        '" + kmmFileName + "'");
    
    // <acct-sel-mode>
    try
    {
      acctSelMode = Helper.Mode.valueOf(cmdLine.getOptionValue("acct-sel-mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <acct-sel-mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Account mode:  " + acctSelMode);

  	// ---------

    // <acct-sel-mode>
    // <account-id>, <acct-name>
    CmdLineHelper_Acct.parseAcctStuffWrap( cmdLine, 
    									acctSelMode,
    									acctID, acctName,
    									scriptMode );

    // <show-parents>
    if ( cmdLine.hasOption("show-parents"))
    {
      showParents = true;
    }
    else
    {
      showParents = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show parents:         " + showParents);

    // <show-children>
    if ( cmdLine.hasOption("show-children"))
    {
      showChildren = true;
    }
    else
    {
      showChildren = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show children:        " + showChildren);

    // <show-transactions>
    if ( cmdLine.hasOption("show-transactions"))
    {
      showTrx = true;
    }
    else
    {
      showTrx = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show transactions:    " + showTrx);

    // <show-reconciliations>
    if ( cmdLine.hasOption("show-reconciliations"))
    {
      showRcn = true;
    }
    else
    {
      showRcn = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show reconciliations: " + showRcn);
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GetAcctInfo", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <acct-sel-mode>:");
    for ( Helper.Mode elt : Helper.Mode.values() )
      System.out.println(" - " + elt);
  }
}
