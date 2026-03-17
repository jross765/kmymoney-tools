package org.kmymoney.tools.xml.helper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.base.tuples.AcctIDAmountFPPair;

import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.dateutils.LocalDateHelpers;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class CmdLineHelper
{
  public enum PrcSelectMode
  {
    ID,
    SEC_DATE
  }

  // -----------------------------------------------------------------

  // ::MAGIC
  public  static final String ACCT_AMT_DUMMY_ARG = "DUMMY";
  private static final String ACCT_AMT_SEP_OUTER = "\\|";
  private static final String ACCT_AMT_SEP_INNER = ";";

  // -----------------------------------------------------------------
  
  public static Helper.DateFormat getDateFormat(CommandLine cmdLine, String argName) throws InvalidCommandLineArgsException
  {
    Helper.DateFormat dateFormat;
    
    if ( cmdLine.hasOption(argName) )
    {
      try
      {
        dateFormat = Helper.DateFormat.valueOf(cmdLine.getOptionValue(argName));
      }
      catch (Exception exc)
      {
        System.err.println("Error: Could not parse <" + argName + ">");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      dateFormat = Helper.DateFormat.ISO;
    }
    
    return dateFormat;
  }

  public static Helper.DateFormat getDateFormat(String arg, String argName) throws InvalidCommandLineArgsException
  {
    Helper.DateFormat dateFormat;
    
    if ( arg != null )
    {
      try
      {
        dateFormat = Helper.DateFormat.valueOf(arg);
      }
      catch (Exception exc)
      {
        System.err.println("Error: Could not parse <" + argName + ">");
        throw new InvalidCommandLineArgsException();
      }
    }
    else
    {
      dateFormat = Helper.DateFormat.ISO;
    }
    
    return dateFormat;
  }
  
  // ------------------------------

  public static LocalDate getDate(CommandLine cmdLine, String argName,
		  						  Helper.DateFormat dateFmt) throws InvalidCommandLineArgsException
  {
    LocalDate datum = LocalDate.now();
    
    try
    {
    	datum = LocalDateHelpers.parseLocalDate(cmdLine.getOptionValue(argName), dateFmt);
    }
    catch (Exception exc)
    {
      System.err.println("Error: Could not parse <" + argName + ">");
      throw new InvalidCommandLineArgsException();
    }
    
    return datum;
  }

  public static LocalDate getDate(String arg, String argName,
			  					  Helper.DateFormat dateFmt) throws InvalidCommandLineArgsException
	{
		LocalDate datum = LocalDate.now();

		try
		{
			datum = LocalDateHelpers.parseLocalDate( arg, dateFmt);
		} catch ( Exception exc )
		{
			System.err.println( "Error: Could not parse <" + argName + ">" );
			throw new InvalidCommandLineArgsException();
		}

		return datum;
	}

  // -----------------------------------------------------------------
  
  public static Collection<AcctIDAmountFPPair> getExpAcctAmtMulti(CommandLine cmdLine, String argName) throws InvalidCommandLineArgsException
  {
    List<AcctIDAmountFPPair> result = new ArrayList<AcctIDAmountFPPair>();

    if ( cmdLine.hasOption(argName) )
    {
       	String arg = cmdLine.getOptionValue(argName);
   	    // System.err.println("*** expacctamt: '" + arg + "' ***");
       	return getExpAcctAmtMulti(arg, argName);
    }
    else
    {
    	// ::EMPTY
    }

    return result;
  }

  public static Collection<AcctIDAmountFPPair> getExpAcctAmtMulti(String arg, String argName) throws InvalidCommandLineArgsException
  {
    List<AcctIDAmountFPPair> result = new ArrayList<AcctIDAmountFPPair>();

    if ( arg == null )
    	return result;
    
    if ( arg.trim().equals( "" ) ||
    	 arg.equals( ACCT_AMT_DUMMY_ARG ) )
    {
    	return result;
    }
    
    try
    {
    	String[] pairListArr = arg.split(ACCT_AMT_SEP_OUTER);
    	// System.err.println("*** arr-size: " + pairListArr.length);
    	for ( String pairStr : pairListArr )
    	{
    		// System.err.println("*** pair: '" + pairStr + "'");
    		if ( ! pairStr.trim().equals( "" ) )
    		{
        		AcctIDAmountFPPair newPair = getExpAcctAmtSingle( pairStr );
        		result.add(newPair);
    		}
    	}
   	}
   	catch (Exception e)
   	{
   		System.err.println("Could not parse <" + argName + ">");
   		throw new InvalidCommandLineArgsException();
   	}

    return result;
  }

  private static AcctIDAmountFPPair getExpAcctAmtSingle(String pairStr) throws InvalidCommandLineArgsException
  {
	int pos = pairStr.indexOf(ACCT_AMT_SEP_INNER);
	if ( pos < 0 )
	{
		System.err.println("Error: List element '" + pairStr + "' does not contain the separator");
		throw new InvalidCommandLineArgsException();
	}
	String acctIDStr = pairStr.substring(0, pos);
	String amtStr    = pairStr.substring(pos + 1);
	// System.err.println(" - elt1: '" + acctIDStr + "'/'" + amtStr + "'");
        		
	KMMAcctID acctID = new KMMAcctID(acctIDStr);
	Double amtDbl = Double.valueOf(amtStr);
	// System.err.println(" - elt2: " + acctIDStr + " / " + amtStr);
        		
	AcctIDAmountFPPair newPair = new AcctIDAmountFPPair(acctID, new FixedPointNumber(amtDbl));

    return newPair;
  }
  
  // ------------------------------
  
  public static void parseSecStuffWrap(
		  CommandLine cmdLine,
		  EnumSecSingleSelMode mode,
		  KMMSecID secID, StringBuffer isin, StringBuffer secName,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	    // <mode>
	    try
	    {
	      mode.mode = xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.valueOf(cmdLine.getOptionValue("mode"));
	    }
	    catch ( Exception exc )
	    {
	      System.err.println("Could not parse <mode>");
	      throw new InvalidCommandLineArgsException();
	    }
	    
	    if ( ! scriptMode )
	      System.err.println("Mode:         " + mode.mode);

	  	// ---------
	  	
	    // <security-id>
	    if ( cmdLine.hasOption("security-id") )
	    {
	      if ( mode.mode != xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID )
	      {
	        System.err.println("<security-id> must only be set with <mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID.toString() + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	      
	      try
	      {
	    	// No:
	        // secID = new KMMSecID( cmdLine.getOptionValue("security-id") );
	        // Instead:
	        KMMSecID locSecID = new KMMSecID( cmdLine.getOptionValue("security-id") );
	        secID.reset();
	        secID.set(locSecID);
	      }
	      catch (Exception exc)
	      {
	        System.err.println("Could not parse <security-id>");
	        throw new InvalidCommandLineArgsException();
	      }
	    }
	    else
	    {
	      if ( mode.mode == xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID )
	      {
	        System.err.println("<security-id> must be set with <mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID.toString() + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	    }

	    if (!scriptMode)
	      System.err.println("Security ID:  '" + secID + "'");

	    // ---------
	  	
	    // <isin>
	    if ( cmdLine.hasOption("isin") )
	    {
	      if ( mode.mode != xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN )
	      {
	        System.err.println("<isin> must only be set with <mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN.toString() + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	      
	      try
	      {
	        isin.append( cmdLine.getOptionValue("isin") );
	      }
	      catch (Exception exc)
	      {
	        System.err.println("Could not parse <isin>");
	        throw new InvalidCommandLineArgsException();
	      }
	    }
	    else
	    {
	      if ( mode.mode == xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN )
	      {
	        System.err.println("<isin> must be set with <mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN.toString() + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	    }

	    if ( ! scriptMode )
	      System.err.println("ISIN:         '" + isin + "'");

	  	// ---------
	  	
	    // <name>
	    if ( cmdLine.hasOption("name") )
	    {
	      if ( mode.mode != xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME )
	      {
	        System.err.println("<name> must only be set with <mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME.toString() + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	      
	      try
	      {
	    	secName.append( cmdLine.getOptionValue("name") );
	      }
	      catch (Exception exc)
	      {
	        System.err.println("Could not parse <name>");
	        throw new InvalidCommandLineArgsException();
	      }
	    }
	    else
	    {
	      if ( mode.mode == xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME )
	      {
	        System.err.println("<name> must be set with <mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME.toString() + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	    }

	    if ( ! scriptMode )
	      System.err.println("Security name: '" + secName + "'");
  }

}

