package org.kmymoney.tools.xml.helper;

import org.apache.commons.cli.CommandLine;
import org.kmymoney.base.basetypes.simple.KMMIDNotSetException;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.tools.xml.helper.CmdLineHelper_Prc.PrcSelectMode;

import xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class CmdLineHelper_Sec {
	
  public static void setSecID(KMMSecID secID,
			CommandLine cmdLine,
			CmdtySecSingleSelMode secSelmode, PrcSelectMode prcSelMode,
			boolean scriptMode) throws InvalidCommandLineArgsException, KMMIDNotSetException
  {
	  KMMSecID locSecID = getSecID(cmdLine,
			  							secSelmode, prcSelMode, 
			  							scriptMode );

	  secID.reset();
	  secID.set(locSecID);
  }

  public static KMMSecID getSecID(
		  CommandLine cmdLine,
		  CmdtySecSingleSelMode secSelmode, PrcSelectMode prcSelmode,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  return getSecID(cmdLine,
			  secSelmode, prcSelmode,
			  "sec-sel-mode", "prc-sel-mode",
			  "security-id",
			  scriptMode );
  }
  
  public static KMMSecID getSecID(
		  CommandLine cmdLine,
		  CmdtySecSingleSelMode secSelmode, PrcSelectMode prcSelmode,
		  String secSelModeArgName, String prcSelModeArgName,
		  String secIDArgName,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  KMMSecID secID = null;
	    
	  // < secSelModeArgName >
	  if ( cmdLine.hasOption( secIDArgName ) )
	  {
		  if ( secSelmode != CmdtySecSingleSelMode.ID )
		  {
			  System.err.println("<" + secIDArgName + "> may only be set with <" + secSelModeArgName + "> = " + CmdtySecSingleSelMode.ID);
			  throw new InvalidCommandLineArgsException();
		  }

		  if ( prcSelmode != null )
		  {
			  if ( prcSelmode != CmdLineHelper_Prc.PrcSelectMode.ISIN_DATE )
			  {
				  System.err.println("<" + secIDArgName+ "> may only be set with <" + prcSelModeArgName + "> = " + CmdLineHelper_Prc.PrcSelectMode.ISIN_DATE);
				  throw new InvalidCommandLineArgsException();
			  }
		  }
	    		
		  try
		  {
			  secID = new KMMSecID( cmdLine.getOptionValue(secIDArgName) ); 
		  }	
		  catch ( Exception exc )
		  {
			  System.err.println("Could not parse <" + secIDArgName+ ">");
	          throw new InvalidCommandLineArgsException();
		  }
	  }
	  else
	  {
		  if ( secSelmode == CmdtySecSingleSelMode.ID )
		  {
			  System.err.println("<" + secIDArgName+ "> must be set with <" + secSelModeArgName + "> = " + CmdtySecSingleSelMode.ID);
			  throw new InvalidCommandLineArgsException();
		  }

		  if ( prcSelmode != null )
		  {
			  if ( prcSelmode == CmdLineHelper_Prc.PrcSelectMode.ISIN_DATE )
			  {
				  System.err.println("<" + secIDArgName+ "> must be set with <" + prcSelModeArgName + "> = " + CmdLineHelper_Prc.PrcSelectMode.ISIN_DATE);
				  throw new InvalidCommandLineArgsException();
			  }
		  }
	  }
	  
	  if ( ! scriptMode )
		  System.err.println("Security ID: " + secID);
	  
	  return secID;
  }
  
  // ------------------------------
  
  public static void parseSecStuffWrap(
		  CommandLine cmdLine,
		  xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode secSelMode,
		  PrcSelectMode prcSelMode,
		  KMMSecID secID, 
		  StringBuffer isin, 
		  StringBuffer secName,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  parseSecStuffWrap(cmdLine,
			  			secSelMode, prcSelMode,
			  			"security-id", secID,
			  			"isin", isin,
			  			"security-name", secName,
			  			scriptMode);
  }
  
  public static void parseSecStuffWrap(
		  CommandLine cmdLine,
		  xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode secSelMode,
		  PrcSelectMode prcSelMode,
		  String secIDArgName, KMMSecID secID, 
		  String isinArgName, StringBuffer isin, 
		  String secNameArgName, StringBuffer secName,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	    // < secIDArgName >
	    if ( cmdLine.hasOption(secIDArgName) )
	    {
	      if ( secSelMode != xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID )
	      {
	        System.err.println("<" + secIDArgName + "> must only be set with <mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID.toString() + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	      
	      try
	      {
	    	// No:
	        // secID = new KMMSecID( cmdLine.getOptionValue("security-id") );
	        // Instead:
	        KMMSecID locSecID = new KMMSecID( cmdLine.getOptionValue(secIDArgName) );
	        secID.reset();
	        secID.set(locSecID);
	      }
	      catch (Exception exc)
	      {
	        System.err.println("Could not parse <" + secIDArgName + ">");
	        throw new InvalidCommandLineArgsException();
	      }
	    }
	    else
	    {
	      if ( secSelMode == xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID )
	      {
	        System.err.println("<" + secIDArgName + "> must be set with <mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID.toString() + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	    }

	    if (!scriptMode)
	      System.err.println("Security ID:  '" + secID + "'");

	    // ---------
	  	
	    // < isinArgName >
	    if ( cmdLine.hasOption(isinArgName) )
	    {
	      if ( secSelMode != xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN )
	      {
	        System.err.println("<" + isinArgName + "> must only be set with <mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN.toString() + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	      
	      try
	      {
	        isin.append( cmdLine.getOptionValue(isinArgName) );
	      }
	      catch (Exception exc)
	      {
	        System.err.println("Could not parse <" + isinArgName + ">");
	        throw new InvalidCommandLineArgsException();
	      }
	    }
	    else
	    {
	      if ( secSelMode == xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN )
	      {
	        System.err.println("<" + isinArgName + "> must be set with <mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN.toString() + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	    }

	    if ( ! scriptMode )
	      System.err.println("ISIN:         '" + isin + "'");

	  	// ---------
	  	
	    // < secNameArgName >
	    if ( cmdLine.hasOption(secNameArgName) )
	    {
	      if ( secSelMode != xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME )
	      {
	        System.err.println("<" + secNameArgName + "> must only be set with <mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME.toString() + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	      
	      try
	      {
	    	secName.append( cmdLine.getOptionValue(secNameArgName) );
	      }
	      catch (Exception exc)
	      {
	        System.err.println("Could not parse <" + secNameArgName + ">");
	        throw new InvalidCommandLineArgsException();
	      }
	    }
	    else
	    {
	      if ( secSelMode == xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME )
	      {
	        System.err.println("<" + secNameArgName + "> must be set with <mode> = '" + xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME.toString() + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	    }

	    if ( ! scriptMode )
	      System.err.println("Security name: '" + secName + "'");
  }

}
