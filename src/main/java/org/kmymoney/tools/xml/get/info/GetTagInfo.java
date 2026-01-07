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
import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.base.basetypes.simple.KMMTagID;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetTagInfo extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GetTagInfo.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String      kmmFileName = null;
  private static Helper.Mode mode        = null;
  private static KMMTagID    tagID       = null;
  private static String      name        = null;
  
  private static boolean showTrx      = false;

  private static boolean scriptMode = false; // ::TODO

  public static void main( String[] args )
  {
    try
    {
      GetTagInfo tool = new GetTagInfo ();
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
        
    Option optTagID = Option.builder("tag")
      .hasArg()
      .argName("ID")
      .desc("Tag ID")
      .longOpt("tag-id")
      .get();
          
    Option optName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Name (or part of)")
      .longOpt("name")
      .get();
          
    // The convenient ones
    Option optShowTrx = Option.builder("strx")
      .desc("Show transactions")
      .longOpt("show-transactions")
      .get();
            
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optTagID);
    options.addOption(optName);
    options.addOption(optShowTrx);
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

    KMyMoneyTag tag = null;
    
    if ( mode == Helper.Mode.ID )
    {
      tag = kmmFile.getTagByID(tagID);
      if ( tag == null )
      {
        System.err.println("Could not find a security with this ID.");
        throw new NoEntryFoundException();
      }
    }
    else if ( mode == Helper.Mode.NAME )
    {
      Collection<KMyMoneyTag> tagList = kmmFile.getTagsByName(name); 
      if ( tagList.size() == 0 )
      {
        System.err.println("Could not find tags matching this name.");
        throw new NoEntryFoundException();
      }
      if ( tagList.size() > 1 )
      {
        System.err.println("Found " + tagList.size() + " tags matching this name.");
        System.err.println("Please specify more precisely.");
        throw new TooManyEntriesFoundException();
      }
      tag = tagList.iterator().next(); // first element
    }
    
    // ----------------------------

    try
    {
      System.out.println("ID:                '" + tag.getID() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("ID:                " + "ERROR");
    }

    try
    {
      System.out.println("toString:          " + tag.toString());
    }
    catch (Exception exc)
    {
      System.out.println("toString:          " + "ERROR");
    }
    
    try
    {
      System.out.println("Name:              '" + tag.getName() + "'");
    }
    catch (Exception exc)
    {
      System.out.println("Name:              " + "ERROR");
    }

    try
    {
      System.out.println("Color:             " + tag.getColor());
    }
    catch (Exception exc)
    {
      System.out.println("Color:             " + "ERROR");
    }

    try
    {
      System.out.println("Notes:             " + tag.getNotes());
    }
    catch (Exception exc)
    {
      System.out.println("Notes:             " + "ERROR");
    }
    
    // ---
    
    if ( showTrx )
      showTransactions(tag);
  }

  private void showTransactions(KMyMoneyTag tag)
  {
    System.out.println("");
    System.out.println("Transactions:");
    
    for ( KMyMoneyTransaction trx : tag.getTransactions() )
    {
      System.out.println(" - " + trx.toString());
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

    // <security-id>
    if ( cmdLine.hasOption("tag-id") )
    {
      if ( mode != Helper.Mode.ID )
      {
        System.err.println("<tag-id> must only be set with <mode> = '" + Helper.Mode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
      
      try
      {
        tagID = new KMMTagID( cmdLine.getOptionValue("tag-id") );
      }
      catch (Exception exc)
      {
        System.err.println("Could not parse <tag-id>");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      if ( mode == Helper.Mode.ID )
      {
        System.err.println("<security-id> must be set with <mode> = '" + Helper.Mode.ID.toString() + "'");
        throw new InvalidCommandLineArgsException();
      }
    }

    if (!scriptMode)
      System.err.println("Tag ID: '" + tagID + "'");

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
    if ( cmdLine.hasOption("show-transactions"))
    {
      showTrx = true;
    }
    else
    {
      showTrx = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show transactions: " + showTrx);
  }

  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "GetTagInfo", "", options, "", true );
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
