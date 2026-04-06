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
import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.write.KMyMoneyWritableSecurity;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.tools.CommandLineTool;
import org.kmymoney.tools.xml.helper.CmdLineHelper_Sec;
import org.kmymoney.tools.xml.helper.SecurityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class UpdSec extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdSec.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String kmmInFileName  = null;
  private static String kmmOutFileName = null;

  private static Helper.CmdtySecSingleSelMode secSelMode = null;

  // CAUTION: As opposed to most other tools, the following variables
  // have to be instantiated here.
  
  private static KMMSecID      secID    = new KMMSecID();
  // This one and the following: sic, StringBuffer, not String,
  // for it has to be mutable because of the way the args are parsed.
  private static StringBuffer  isin     = new StringBuffer();
  // Possibly later:
  // private static StringBuffer  wkn      = new StringBuffer();
  // private static StringBuffer  cusip    = new StringBuffer();
  // private static StringBuffer  sedol    = new StringBuffer();
  // private static StringBuffer  secName  = new StringBuffer(); // <-- NOT for selection

  // ---

  private static KMyMoneyWritableSecurity sec = null;

  private static String          newName  = null;
  private static KMMSecCurr.Type newType  = null;

  private static boolean scriptMode = false;

  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      UpdSec tool = new UpdSec ();
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

    Option optMode = Option.builder("ssm")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode for security")
      .longOpt("sec-sel-mode")
      .get();

    Option optSecID = Option.builder("sec")
      .hasArg()
      .argName("ID")
      .desc("Security ID " + 
      		"(for <mode> = " + Helper.CmdtySecSingleSelMode.ID + " only)")
      .longOpt("security-id")
      .get();

    Option optISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("ISIN " + 
  		   	"(for <mode> = " + Helper.CmdtySecSingleSelMode.ISIN + " only)")
      .longOpt("isin")
      .get();

    // ---
    
    Option optName = Option.builder("nam")
      .hasArg()
      .argName("name")
      .desc("Security name (new)") // <-- !
      .longOpt("new-name")
      .get();
    
    Option optType = Option.builder("t")
      .hasArg()
      .argName("type")
      .desc("Security type (new)")
      .longOpt("new-type")
      .get();

    // The convenient ones
    Option optScript = Option.builder("sl")
      .desc("Script Mode")
      .longOpt("script")
      .get();            

    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optMode);
    options.addOption(optSecID);
    options.addOption(optISIN);
    options.addOption(optName);
    options.addOption(optType);
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

    sec = SecurityHelper.getWrtSec(secSelMode,
								secID, isin.toString(), null, // <-- sic, not by name 
								kmmFile,
								scriptMode);
    System.err.println("Security before update: " + sec.toString());

	// ----------------------------
    
    doChanges();
    System.err.println("Security after update: " + sec.toString());
    
    kmmFile.writeFile(new File(kmmOutFileName));
    
    System.out.println("OK");
  }

  private void doChanges() throws Exception
  {
    if ( newName != null )
    {
      System.err.println("Setting name");
      sec.setName(newName);
    }

    if ( newType != null )
    {
      System.err.println("Setting type");
      sec.setType(newType);
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

    if ( ! scriptMode )
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

    if ( ! scriptMode )
    	System.err.println("KMyMoney file (out): '" + kmmOutFileName + "'");

    // <-sec-sel-mode>
    try
    {
      secSelMode = Helper.CmdtySecSingleSelMode.valueOf(cmdLine.getOptionValue("sec-sel-mode"));
      
      if ( secSelMode == Helper.CmdtySecSingleSelMode.NAME )
      {
        System.err.println("<sec-sel-mode> '" + Helper.CmdtySecSingleSelMode.NAME + "' must not be used here");
        throw new InvalidCommandLineArgsException();
      }
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <sec-sel-mode>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Security mode:         " + secSelMode);

  	// ---------

    // <sec-sel-mode>,
    // <secid-type>, <isin>
    // NOT NAME!
    CmdLineHelper_Sec.parseSecStuffWrap( cmdLine, 
    									secSelMode, null,
    									secID, 
    									isin, 
    									null, // <-- !
    									scriptMode );

  	// ---------

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

    if ( ! scriptMode )
      System.err.println("New name: '" + newName + "'");

    // <new-type>
    if ( cmdLine.hasOption("new-type") ) 
    {
      try
      {
        newType = KMMSecCurr.Type.valueOf( cmdLine.getOptionValue("new-type") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <new-type>");
        throw new InvalidCommandLineArgsException();
      }
    }

    if ( ! scriptMode )
      System.err.println("New type: '" + newType + "'");
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "UpdSec", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <sec-sel-mode>:");
    for ( Helper.CmdtySecSingleSelMode elt : Helper.CmdtySecSingleSelMode.values() )
      System.out.println(" - " + elt);
    
    System.out.println("");
    System.out.println("Valid values for <new-type>:");
    for ( KMMSecCurr.Type elt : KMMSecCurr.Type.values() )
      System.out.println(" - " + elt);
  }
}
