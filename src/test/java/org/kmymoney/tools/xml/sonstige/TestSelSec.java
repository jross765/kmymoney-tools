package org.kmymoney.tools.xml.sonstige;

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
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.tools.CommandLineTool;
import org.kmymoney.tools.xml.helper.CmdLineHelper;
import org.kmymoney.tools.xml.helper.EnumSecSingleSelMode;
import org.kmymoney.tools.xml.helper.SecurityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class TestSelSec extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(TestSelSec.class);
  
  // -----------------------------------------------------------------

  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String  kmmFileName = null;
  
  // CAUTION: As opposed to most other tools, the following variables
  // have to be instantiated here.
  
  private static EnumSecSingleSelMode mode = new EnumSecSingleSelMode();

  private static KMMSecID      secID    = new KMMSecID();
  // This one and the following: sic, StringBuffer, not String,
  // for it has to be mutable because of the way the args are parsed.
  private static StringBuffer  isin     = new StringBuffer();
  private static StringBuffer  secName  = new StringBuffer();
  
  private static boolean scriptMode = false;
  
  // -----------------------------------------------------------------

  public static void main( String[] args )
  {
    try
    {
      TestSelSec tool = new TestSelSec ();
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
      
    Option optMode = Option.builder("m")
      .required()
      .hasArg()
      .argName("mode")
      .desc("Selection mode")
      .longOpt("mode")
      .get();
        
    Option optSecID = Option.builder("sec")
      .hasArg()
      .argName("ID")
      .desc("Security ID " + 
      		"(for <mode> = " + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID + " only)")
      .longOpt("security-id")
      .get();
    	          
    Option optISIN = Option.builder("is")
      .hasArg()
      .argName("isin")
      .desc("ISIN " + 
  		   	"(for <mode> = " + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN + " only)")
      .longOpt("isin")
      .get();
        
    Option optSecName = Option.builder("n")
      .hasArg()
      .argName("name")
      .desc("Security name (full) " + 
  		    "(for <mode> = " + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME + " only)")
      .longOpt("name")
      .get();
          
    // The convenient ones
    // ::EMPTY
            
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    options.addOption(optSecID);
    options.addOption(optISIN);
    options.addOption(optSecName);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cfg) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
	KMyMoneyFileImpl kmmFile = new KMyMoneyFileImpl(new File(kmmFileName), true);

    KMyMoneySecurity sec = SecurityHelper.getSec(mode.mode, 
    											 secID, isin.toString(), secName.toString(), 
    											 kmmFile);

    System.out.println("Selected security: " + sec.toString());
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

    // <mode>, <security-id>, <isin>, <name>
    CmdLineHelper.parseSecStuffWrap( cmdLine, 
    								 mode, 
    								 secID, isin, secName, 
    								 scriptMode );
  }

  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "TestSelSec", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    System.out.println("");
    System.out.println("Valid values for <mode>:");
    for ( xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode elt : xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.values() )
      System.out.println(" - " + elt);
  }
}
