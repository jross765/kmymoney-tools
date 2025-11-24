package org.kmymoney.tools.xml.get.list;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.apiext.Const;
import org.kmymoney.apiext.trxmgr.TransactionFilter;
import org.kmymoney.apiext.trxmgr.TransactionFilter.SplitLogic;
import org.kmymoney.apiext.trxmgr.TransactionFinder;
import org.kmymoney.apiext.trxmgr.TransactionSplitFilter;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMIDNotSetException;
import org.kmymoney.base.basetypes.simple.KMMPyeID;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.dateutils.DateHelpers;
import xyz.schnorxoborx.base.dateutils.LocalDateHelpers;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class GetTrxList extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetTrxList.class);
  
  // -----------------------------------------------------------------
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  // ------------------------------
  
  private static String     kmmFileName     = null;
  
  private static KMyMoneyTransactionSplit.Action     action     = null;
  private static KMyMoneyTransactionSplit.ReconState reconState = null;
  
  private static KMMAcctID  acctID          = null; // sic, not KMMComplAcctID
  private static KMMPyeID   pyeID           = null;
  
  private static LocalDate  datePostedFrom  = TransactionFilter.DATE_UNSET; 
  private static LocalDate  datePostedTo    = TransactionFilter.DATE_UNSET; 
  
  private static LocalDate  dateEnteredFrom = TransactionFilter.DATE_UNSET; 
  private static LocalDate  dateEnteredTo   = TransactionFilter.DATE_UNSET; 
  
  private static double     valueFrom       = Const.UNSET_VALUE; 
  private static double     valueTo         = Const.UNSET_VALUE; 
  
  private static double     nofSharesFrom   = Const.UNSET_VALUE; 
  private static double     nofSharesTo     = Const.UNSET_VALUE; 
  
  private static int        nofSplitsFrom   = TransactionFilter.NOF_SPLT_UNSET; 
  private static int        nofSplitsTo     = TransactionFilter.NOF_SPLT_UNSET; 
  
  private static String     memoTrx         = null; 
  private static String     memoSplt        = null; 
  
  private static boolean    showFlt         = false; 
  private static boolean    showSplt        = false; 
  
  // ------------------------------
  
  private static boolean scriptMode = false; // ::TODO

  // -----------------------------------------------------------------
  
  public static void main( String[] args )
  {
    try
    {
      GetTrxList tool = new GetTrxList ();
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
      
    // The convenient ones
    Option optAction = Option.builder("act")
      .hasArg()
      .argName("act")
      .desc("Action (split level)")
      .longOpt("action")
      .get();
    	          
    Option optReconState = Option.builder("stat")
      .hasArg()
      .argName("stat")
      .desc("Reconciliation state (split level)")
      .longOpt("recon-state")
      .get();
    
    // ---
    
    Option optAcct = Option.builder("acct")
      .hasArg()
      .argName("acct")
      .desc("Account ID (split level)")
      .longOpt("account-id")
      .get();
    	    	          
    Option optPye = Option.builder("pye")
      .hasArg()
      .argName("pye")
      .desc("Payee ID (split level)")
      .longOpt("payee-id")
      .get();
    
    // ---
    
    Option optDatePostedFrom = Option.builder("fdp")
      .hasArg()
      .argName("date")
      .desc("From date posted")
      .longOpt("from-date-posted")
      .get();
    
    Option optDatePostedTo = Option.builder("tdp")
      .hasArg()
      .argName("date")
      .desc("To date posted")
      .longOpt("to-date-posted")
      .get();
    
    // ---
    
    Option optDateEnteredFrom = Option.builder("fde")
      .hasArg()
      .argName("date")
      .desc("From date entered")
      .longOpt("from-date-entered")
      .get();
    
    Option optDateEnteredTo = Option.builder("tde")
      .hasArg()
      .argName("date")
      .desc("To date entered")
      .longOpt("to-date-entered")
      .get();
    
    // ---
    
    Option optValueFrom = Option.builder("fv")
      .hasArg()
      .argName("value")
      .desc("From value (split level)")
      .longOpt("from-value")
      .get();
    	          
    Option optValueTo = Option.builder("tv")
      .hasArg()
      .argName("value")
      .desc("To value (split level)")
      .longOpt("to-value")
      .get();
    	    	          
    // ---
    
    Option optNofSharesFrom = Option.builder("fnsh")
      .hasArg()
      .argName("no")
      .desc("From no. of shares (split level)")
      .longOpt("from-nof-shares")
      .get();
    	          
    Option optNofSharesTo = Option.builder("tnsh")
      .hasArg()
      .argName("no")
      .desc("To no. of shares (split level)")
      .longOpt("to-nof-shares")
      .get();
    	    	          
    // ---
    
    Option optNofSplitsFrom = Option.builder("fnsp")
      .hasArg()
      .argName("no")
      .desc("From no. of splits")
      .longOpt("from-nof-splits")
      .get();
    	          
    Option optNofSplitsTo = Option.builder("tnsp")
      .hasArg()
      .argName("no")
      .desc("To no. of splits")
      .longOpt("to-nof-splits")
      .get();
    
    // ---
    
    Option optMemoTrx = Option.builder("mtrx")
      .hasArg()
      .argName("str")
      .desc("Memo (transaction level)s")
      .longOpt("memo-transaction")
      .get();
    	          
    Option optMemoSplt = Option.builder("msplt")
      .hasArg()
      .argName("str")
      .desc("Memo (split level)")
      .longOpt("memo-split")
      .get();
    
    // ---
    
    Option optShowFilter = Option.builder("sflt")
      .desc("Show filter (for debugging purposes)")
      .longOpt("show-filter")
      .get();
    	    	    
    Option optShowSplits = Option.builder("ssplt")
      .desc("Show splits")
      .longOpt("show-splits")
      .get();
    	    
    	    	          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optAction);
    options.addOption(optReconState);
    options.addOption(optAcct);
    options.addOption(optPye);
    options.addOption(optDatePostedFrom);
    options.addOption(optDatePostedTo);
    options.addOption(optDateEnteredFrom);
    options.addOption(optDateEnteredTo);
    options.addOption(optValueFrom);
    options.addOption(optValueTo);
    options.addOption(optNofSharesFrom);
    options.addOption(optNofSharesTo);
    options.addOption(optNofSplitsFrom);
    options.addOption(optNofSplitsTo);
    options.addOption(optMemoTrx);
    options.addOption(optMemoSplt);
    options.addOption(optShowFilter);
    options.addOption(optShowSplits);
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
    
    // 1) Set filter
    TransactionFilter trxFlt = setFilter();
    
    if ( showFlt )
    {
        System.err.println("");
        System.err.println("Filter: " + trxFlt.toString());
        System.err.println("");
    }
    
    // 2) Find transactions, applying filter
    TransactionFinder trxFnd = new TransactionFinder(kmmFile);
    ArrayList<KMyMoneyTransaction> trxList = trxFnd.find(trxFlt, true, SplitLogic.OR);
    
    // 3) Show results
    showResults( trxList );
  }

  // -----------------------------------------------------------------

  private TransactionFilter setFilter() throws KMMIDNotSetException
  {
	TransactionSplitFilter spltFlt = new TransactionSplitFilter();
    
    if ( action != null )
    	spltFlt.action = action;
    if ( reconState != null )
    	spltFlt.reconState = reconState;
    
    if ( acctID != null )
    	spltFlt.acctID.set( acctID );
    if ( pyeID != null )
    	spltFlt.pyeID.set( pyeID );
    
    if ( valueFrom != Const.UNSET_VALUE )
    	spltFlt.valueFrom = new FixedPointNumber(valueFrom);
    if ( valueTo   != Const.UNSET_VALUE )
    	spltFlt.valueTo   = new FixedPointNumber(valueTo);
    spltFlt.valueAbs = true;

    if ( nofSharesFrom != Const.UNSET_VALUE )
    	spltFlt.sharesFrom = new FixedPointNumber(nofSharesFrom);
    if ( nofSharesTo   != Const.UNSET_VALUE )
    	spltFlt.sharesTo   = new FixedPointNumber(nofSharesTo);
    spltFlt.sharesAbs = true;
    
    if ( memoSplt != null )
    	spltFlt.memoPart = memoSplt;

    // ---

    TransactionFilter trxFlt = new TransactionFilter();
    
    if ( ! datePostedFrom.equals(TransactionFilter.DATE_UNSET) )
    	trxFlt.datePostedFrom  = datePostedFrom;
    if ( ! datePostedTo.equals(TransactionFilter.DATE_UNSET) )
    	trxFlt.datePostedTo    = datePostedTo;
    
    if ( ! dateEnteredFrom.equals(TransactionFilter.DATE_UNSET) )
    	trxFlt.dateEnteredFrom = dateEnteredFrom;
    if ( ! dateEnteredTo.equals(TransactionFilter.DATE_UNSET) )
    	trxFlt.dateEnteredTo   = dateEnteredTo;
    
    if ( nofSplitsFrom != TransactionFilter.NOF_SPLT_UNSET )
    	trxFlt.nofSpltFrom = nofSplitsFrom;
    if ( nofSplitsTo   != TransactionFilter.NOF_SPLT_UNSET )
    	trxFlt.nofSpltTo   = nofSplitsTo;
    
    if ( memoTrx != null )
    	trxFlt.memoPart = memoTrx;

    trxFlt.spltFilt = spltFlt;
    
    // ---

	return trxFlt;
  }

  private void showResults(ArrayList<KMyMoneyTransaction> trxList) throws NoEntryFoundException
  {
	if ( trxList.size() == 0 ) 
    {
    	System.err.println("Found no transaction matching the criteria.");
    	throw new NoEntryFoundException();
    }

	System.err.println("Found " + trxList.size() + " transaction(s).");
    for ( KMyMoneyTransaction trx : trxList )
    {
    	System.out.println(" - " + trx.toString());
        if ( showSplt )
        {
        	for ( KMyMoneyTransactionSplit splt : trx.getSplits() )
        	{
        		System.out.println("   o " + splt.toString());
        	}
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
      System.err.println("KMyMoney file:      '" + kmmFileName + "'");
    
    // ---
    
    // <action>
    if ( cmdLine.hasOption( "action" ) )
    {
        try
        {
        	action = KMyMoneyTransactionSplit.Action.valueOf( cmdLine.getOptionValue("action") );
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <action>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
      System.err.println("Action:             " + action);
    
    // <recon-state>
    if ( cmdLine.hasOption( "recon-state" ) )
    {
        try
        {
        	reconState = KMyMoneyTransactionSplit.ReconState.valueOf( cmdLine.getOptionValue("recon-state") );
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <recon-state>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
      System.err.println("Reconcil. state:    " + reconState);
    
    // ---
    
    // <account-id>
    if ( cmdLine.hasOption( "account-id" ) )
    {
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
    
    if ( ! scriptMode )
      System.err.println("Account ID:         " + acctID);
    
    // <payee-id>
    if ( cmdLine.hasOption( "payee-id" ) )
    {
        try
        {
        	pyeID = new KMMPyeID( cmdLine.getOptionValue("payee-id") );
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <payee-id>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
      System.err.println("Payee ID:           " + pyeID);
    
    // ---
    
    // <from-date-posted>
    if ( cmdLine.hasOption( "from-date-posted" ) )
    {
        try
        {
        	datePostedFrom = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue("from-date-posted"), DateHelpers.DATE_FORMAT_ISO);
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <from-date-posted>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
        if ( datePostedFrom.equals(TransactionFilter.DATE_UNSET) )
        	System.err.println("From date posted:   " + "(unset)");
        else
        	System.err.println("From date posted:   " + datePostedFrom);
    }
    
    // <to-date-posted>
    if ( cmdLine.hasOption( "to-date-posted" ) )
    {
        try
        {
        	datePostedTo = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue("to-date-posted"), DateHelpers.DATE_FORMAT_ISO);
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <to-date-posted>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
        if ( datePostedTo.equals(TransactionFilter.DATE_UNSET) )
        	System.err.println("To date posted:     " + "(unset)");
        else
        	System.err.println("To date posted:     " + datePostedTo);
    }
    
    // ---
    
    // <from-date-entered>
    if ( cmdLine.hasOption( "from-date-entered" ) )
    {
        try
        {
        	dateEnteredFrom = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue("from-date-entered"), DateHelpers.DATE_FORMAT_ISO);
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <from-date-entered>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
        if ( dateEnteredFrom.equals(TransactionFilter.DATE_UNSET) )
        	System.err.println("From date entered:  " + "(unset)");
        else
        	System.err.println("From date entered:  " + dateEnteredFrom);
    }
    
    // <to-date-entered>
    if ( cmdLine.hasOption( "to-date-entered" ) )
    {
        try
        {
        	dateEnteredTo = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue("to-date-entered"), DateHelpers.DATE_FORMAT_ISO);
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <to-date-entered>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
        if ( dateEnteredTo.equals(TransactionFilter.DATE_UNSET) )
        	System.err.println("To date entered:    " + "(unset)");
        else
        	System.err.println("To date entered:    " + dateEnteredTo);
    }
    
    // ---
    
    // <from-value>
    if ( cmdLine.hasOption( "from-value" ) )
    {
        try
        {
        	valueFrom = Double.parseDouble( cmdLine.getOptionValue("from-value") );
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <from-value>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
    	if ( valueFrom == Const.UNSET_VALUE )
    		System.err.println("From value:         " + "(unset)");
    	else
    		System.err.println("From value:         " + valueFrom);
    }
  
    // <to-value>
    if ( cmdLine.hasOption( "to-value" ) )
    {
        try
        {
        	valueTo = Double.parseDouble( cmdLine.getOptionValue("to-value") );
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <to-value>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
    	if ( valueTo == Const.UNSET_VALUE )
    		System.err.println("To value:           " + "(unset)");
    	else
    		System.err.println("To value:           " + valueTo);
    }
    
    // ---
    
    // <from-nof-shares>
    if ( cmdLine.hasOption( "from-nof-shares" ) )
    {
        try
        {
        	nofSharesFrom = Double.parseDouble( cmdLine.getOptionValue("from-nof-shares") );
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <from-nof-shares>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
    	if ( nofSharesFrom == Const.UNSET_VALUE )
    		System.err.println("From no. of shares: " + "(unset)");
    	else
    		System.err.println("From no. of shares: " + nofSharesFrom);
    }
  
    // <to-nof-shares>
    if ( cmdLine.hasOption( "to-nof-shares" ) )
    {
        try
        {
        	nofSharesTo = Double.parseDouble( cmdLine.getOptionValue("to-nof-shares") );
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <to-nof-shares>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
    	if ( nofSharesTo == Const.UNSET_VALUE )
    		System.err.println("To no. of shares:   " + "(unset)");
    	else
    		System.err.println("To no. of shares:   " + nofSharesTo);
    }
    
    // ---
    
    // <from-nof-splits>
    if ( cmdLine.hasOption( "from-nof-splits" ) )
    {
        try
        {
        	nofSplitsFrom = Integer.parseInt( cmdLine.getOptionValue("from-nof-splits"));
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <from-nof-splits>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
    	if ( nofSplitsFrom == TransactionFilter.NOF_SPLT_UNSET )
    		System.err.println("From no. of splits: " + "(unset)");
    	else
    		System.err.println("From no. of splits: " + nofSplitsFrom);
    }
    
    // <to-nof-splits>
    if ( cmdLine.hasOption( "to-nof-splits" ) )
    {
        try
        {
        	nofSplitsTo = Integer.parseInt( cmdLine.getOptionValue("to-nof-splits"));
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <to-nof-splits>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
    	if ( nofSplitsTo == TransactionFilter.NOF_SPLT_UNSET )
    		System.err.println("To no. of splits:   " + "(unset)");
    	else
    	 	System.err.println("To no. of splits:   " + nofSplitsTo);
    }
    
    // ---
    
    // <memo-transaction>
    if ( cmdLine.hasOption( "memo-transaction" ) )
    {
        try
        {
        	memoTrx = cmdLine.getOptionValue("memo-transaction");
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <memo-transaction>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
    	if ( memoTrx == null )
    		System.err.println("Memo (trx level):   " + "(unset)");
    	else
    	 	System.err.println("Memo (trx level):   " + memoTrx);
    }
    
    // <memo-split>
    if ( cmdLine.hasOption( "memo-split" ) )
    {
        try
        {
        	memoSplt = cmdLine.getOptionValue("memo-split");
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <memo-split>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    
    if ( ! scriptMode )
    {
    	if ( memoSplt == null )
    		System.err.println("Memo (split level): " + "(unset)");
    	else
    	 	System.err.println("Memo (split level): " + memoSplt);
    }
    
    // ---
    
    // <show-filter>
    if ( cmdLine.hasOption( "show-filter" ) )
    {
        showFlt = true;
    }
    
    if ( ! scriptMode )
      System.err.println("Show filter:        " + showSplt);
    
    // <show-splits>
    if ( cmdLine.hasOption( "show-splits" ) )
    {
        showSplt = true;
    }
    
    if ( ! scriptMode )
      System.err.println("Show splits:        " + showSplt);
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GetTrxList", options );
    
    System.out.println("");
    System.out.println("Valid values for <action>:");
    for ( KMyMoneyTransactionSplit.Action elt : KMyMoneyTransactionSplit.Action.values() )
      System.out.println(" - " + elt);
    
    System.out.println("");
    System.out.println("Valid values for <recon-state>:");
    for ( KMyMoneyTransactionSplit.ReconState elt : KMyMoneyTransactionSplit.ReconState.values() )
      System.out.println(" - " + elt);
  }
}
