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
import org.kmymoney.api.write.KMyMoneyWritableTag;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.base.basetypes.simple.KMMTagID;
import org.kmymoney.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class UpdTag extends CommandLineTool
{
  // Logger
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdTag.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String   kmmInFileName = null;
  private static String   kmmOutFileName = null;
  
  private static KMMTagID tagID = null;

  private static String name  = null;
  private static String descr = null;
  private static String color = null;

  private static KMyMoneyWritableTag tag = null;

  public static void main( String[] args )
  {
    try
    {
      UpdTag tool = new UpdTag ();
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
    // TagID = UUID.randomUUID();

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
      .argName("tagid")
      .desc("Tag ID")
      .longOpt("Tag-id")
      .get();
            
    Option optName = Option.builder("nam")
      .hasArg()
      .argName("name")
      .desc("Tag name")
      .longOpt("name")
      .get();
    
    Option optDescr = Option.builder("desc")
      .hasArg()
      .argName("descr")
      .desc("Tag description")
      .longOpt("description")
      .get();
      
    Option optColor = Option.builder("c")
      .hasArg()
      .argName("descr")
      .desc("Tag color")
      .longOpt("color")
      .get();
    	      
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optID);
    options.addOption(optName);
    options.addOption(optDescr);
    options.addOption(optColor);
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
      tag = kmmFile.getWritableTagByID(tagID);
      System.err.println("Tag before update: " + tag.toString());
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not find/instantiate Tag with ID '" + tagID + "'");
      // ::TODO
//      throw new TagNotFoundException();
      throw new NoEntryFoundException();
    }
    
    doChanges(kmmFile);
    System.err.println("Tag after update: " + tag.toString());
    
    kmmFile.writeFile(new File(kmmOutFileName));
    
    System.out.println("OK");
  }

  private void doChanges(KMyMoneyWritableFileImpl kmmFile) throws Exception
  {
    if ( name != null )
    {
      System.err.println("Setting name");
      tag.setName(name);
    }

    if ( descr != null )
    {
      System.err.println("Setting description");
      tag.setNotes(descr);
    }

    if ( color != null )
    {
      System.err.println("Setting color");
      tag.setColor(color);
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
    
    // <tag-id>
    try
    {
    	tagID = new KMMTagID( cmdLine.getOptionValue("tag-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <tag-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Tag ID: " + tagID);

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

    // <color>
    if ( cmdLine.hasOption("color") ) 
    {
      try
      {
        color = cmdLine.getOptionValue("color");
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <color>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Color: '" + color + "'");
  }
  
  @Override
  protected void printUsage()
  {
	HelpFormatter formatter = HelpFormatter.builder().get();
	try
	{
		formatter.printHelp( "UpdTag", "", options, "", true );
	}
	catch ( IOException e )
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
