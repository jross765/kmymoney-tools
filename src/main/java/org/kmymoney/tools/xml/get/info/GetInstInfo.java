package org.kmymoney.tools.xml.get.info;

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
import org.kmymoney.api.read.KMyMoneyInstitution;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetInstInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetInstInfo.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String      kmmFileName = null;
  private static Helper.Mode mode        = null;
  private static KMMInstID   instID      = null;
  private static String      name        = null;
  
  private static boolean showAcct   = false;

  private static boolean scriptMode = false; // ::TODO

  public static void main( String[] args )
  {
    try
    {
      GetInstInfo tool = new GetInstInfo ();
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
      .get();
      
    Option optMode = Option.builder("m")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode")
      .longOpt("mode")
      .get();
        
    Option optInstID = Option.builder("inst")
      .hasArg()
      .argName("ID")
      .desc("Institution ID")
      .longOpt("institution-id")
      .get();
          
    Option optName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Name (or part of)")
      .longOpt("name")
      .get();
          
    // The convenient ones
    Option optShowAcct = Option.builder("sacct")
      .desc("Show accounts")
      .longOpt("show-accounts")
      .get();
            
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optInstID);
    options.addOption(optName);
    options.addOption(optShowAcct);
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

    KMyMoneyInstitution inst = null;
    
    if ( mode == Helper.Mode.ID )
    {
      inst = kmmFile.getInstitutionByID(instID);
      if ( inst == null )
      {
        System.err.println("Could not find an institution with this ID.");
        throw new NoEntryFoundException();
      }
    }
    else if ( mode == Helper.Mode.NAME )
    {
      Collection<KMyMoneyInstitution> instList = kmmFile.getInstitutionsByName(name); 
      if ( instList.size() == 0 )
      {
        System.err.println("Could not find institutions matching this name.");
        throw new NoEntryFoundException();
      }
      if ( instList.size() > 1 )
      {
        System.err.println("Found " + instList.size() + " institutions matching this name.");
        System.err.println("Please specify more precisely.");
        throw new TooManyEntriesFoundException();
      }
      inst = instList.iterator().next(); // first element
    }
    
    // ----------------------------

    try
    {
      System.out.println("ID:                '" + inst.getID() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("ID:                " + "ERROR");
    }

    try
    {
      System.out.println("toString:          " + inst.toString());
    }
    catch (Exception exc)
    {
      System.out.println("toString:          " + "ERROR");
    }
    
    try
    {
      System.out.println("Name:              '" + inst.getName() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("Name:              " + "ERROR");
    }

    try
    {
      System.out.println("Sort code:         " + inst.getSortCode());
    }
    catch (Exception exc)
    {
      System.out.println("Sort code:             " + "ERROR");
    }

    try
    {
      System.out.println("Address:           " + inst.getAddress());
    }
    catch (Exception exc)
    {
      System.out.println("Address:           " + "ERROR");
    }
    
    try
    {
      System.out.println("BIC:               '" + inst.getBIC() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("BIC:               " + "ERROR");
    }
    
    try
    {
      System.out.println("URL:               '" + inst.getURL() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("URL:               " + "ERROR");
    }
    
    // ---
    
    if ( showAcct )
      showAccounts(inst);
  }

  private void showAccounts(KMyMoneyInstitution inst)
  {
    System.out.println("");
    System.out.println("Accounts:");
    
    for ( KMyMoneyAccount acct : inst.getAccounts() )
    {
      System.out.println(" - " + acct.toString());
    }
  }

  // -----------------------------------------------------------------

  @Override
  protected void parseCommandLineArgs(String[] args)
      throws InvalidCommandLineArgsException
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
    catch (Exception exc)
    {
      System.err.println("Could not parse <kmymoney-file>");
      throw new InvalidCommandLineArgsException();
    }

    if (!scriptMode)
      System.err.println("KMyMoney file: '" + kmmFileName + "'");

    // <mode>
    try
    {
      mode = Helper.Mode.valueOf(cmdLine.getOptionValue("mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Mode:     " + mode);

    // <institution-id>
    if ( cmdLine.hasOption("institution-id") )
    {
      if ( mode != Helper.Mode.ID )
      {
        System.err.println("<institution-id> must only be set with <mode> = '" + Helper.Mode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        instID = new KMMInstID( cmdLine.getOptionValue("institution-id") );
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <institution-id>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( mode == Helper.Mode.ID )
      {
        System.err.println("<institution-id> must be set with <mode> = '" + Helper.Mode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("Institution ID: '" + instID + "'");

    // <name>
    if ( cmdLine.hasOption("name") )
    {
      if ( mode != Helper.Mode.NAME )
      {
        System.err.println("<name> must only be set with <mode> = '" + Helper.Mode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        name = cmdLine.getOptionValue("name");
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( mode == Helper.Mode.NAME )
      {
        System.err.println("<name> must be set with <mode> = '" + Helper.Mode.NAME.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("Name:     '" + name + "'");
    
    // <show-transactions>
    if ( cmdLine.hasOption("show-accounts"))
    {
      showAcct = true;
    }
    else
    {
    	showAcct = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show accounts: " + showAcct);
  }

  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GetInstInfo", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <mode>:");
    for ( Helper.Mode elt : Helper.Mode.values() )
      System.out.println(" - " + elt);
  }
}
