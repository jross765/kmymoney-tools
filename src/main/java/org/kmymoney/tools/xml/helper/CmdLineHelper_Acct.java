package org.kmymoney.tools.xml.helper;

import org.apache.commons.cli.CommandLine;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMIDNotSetException;

import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class CmdLineHelper_Acct {

  public static void setAcctID(KMMAcctID acctID,
		  						CommandLine cmdLine,
		  						Helper.Mode acctSelMode,
		  						boolean scriptMode) throws InvalidCommandLineArgsException, KMMIDNotSetException
  {
	  KMMAcctID locAcctID = getAcctID(cmdLine,
			  							acctSelMode,
			  							scriptMode );
	  
	  acctID.reset();
	  acctID.set(locAcctID);
  }
  
  public static KMMAcctID getAcctID(
		  CommandLine cmdLine,
		  Helper.Mode acctSelMode,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  return getAcctID(cmdLine,
			  acctSelMode,
			  "acct-sel-mode",
			  "account-id",
			  scriptMode );
  }
  
  public static KMMAcctID getAcctID(
		  CommandLine cmdLine,
		  Helper.Mode acctSelMode,
		  String acctSelModeArgName, String acctIDArgName,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  KMMAcctID acctID = null;
	    
	  // < acctSelModeArgName >
	  if ( cmdLine.hasOption( acctIDArgName ) )
	  {
		  if ( acctSelMode != Helper.Mode.ID )
		  {
			  System.err.println("<" + acctIDArgName + "> may only be set with <" + acctSelModeArgName + "> = " + Helper.Mode.ID);
			  throw new InvalidCommandLineArgsException();
		  }

		  try
		  {
			  acctID = new KMMAcctID( cmdLine.getOptionValue(acctIDArgName) ); 
		  }	
		  catch ( Exception exc )
		  {
			  System.err.println("Could not parse <" + acctIDArgName+ ">");
	          throw new InvalidCommandLineArgsException();
		  }
	  }
	  else
	  {
		  if ( acctSelMode == Helper.Mode.ID )
		  {
			  System.err.println("<" + acctIDArgName+ "> must be set with <" + acctSelModeArgName + "> = " + Helper.Mode.ID);
			  throw new InvalidCommandLineArgsException();
		  }
	  }
	  
	  if ( ! scriptMode )
		  System.err.println("Account ID: " + acctID);
	  
	  return acctID;
  }
  
  // -----------------------------------------------------------------

  
  
  // ------------------------------
  
  public static void parseAcctStuffWrap(
		  CommandLine cmdLine,
		  Helper.Mode acctSelMode, 
		  KMMAcctID acctID, StringBuffer acctName,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  parseAcctStuffWrap(cmdLine,
			  			acctSelMode,
			  			"account-id", acctID,
			  			"account-name", acctName,
			  			scriptMode);
  }
  
  public static void parseAcctStuffWrap(
		  CommandLine cmdLine,
		  Helper.Mode acctSelMode, 
		  String acctIDArgName, KMMAcctID acctID, 
		  String acctNameArgName, StringBuffer acctName,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	    // < acctIDArgName >
	    if ( cmdLine.hasOption(acctIDArgName) )
	    {
	      if ( acctSelMode != Helper.Mode.ID )
	      {
	        System.err.println("<" + acctIDArgName + "> must only be set with <acct-sel-mode> = '" + Helper.Mode.ID + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	      
	      try
	      {
	    	  // NO:
	    	  // acctID = new KMMAcctID( cmdLine.getOptionValue("account-id") );
			  // Instead:
			  setAcctID(acctID, 
					  cmdLine,
					  acctSelMode,
					  scriptMode);
	      }
	      catch ( Exception exc )
	      {
	        System.err.println("Could not parse <" + acctIDArgName + ">");
	        throw new InvalidCommandLineArgsException();
	      }
	    }
	    else
	    {
	      if ( acctSelMode == Helper.Mode.ID )
	      {
	        System.err.println("<" + acctIDArgName + "> must be set with <acct-sel-mode> = '" + Helper.Mode.ID + "'");
	        throw new InvalidCommandLineArgsException();
	      }      
	    }
	    
	    if ( ! scriptMode )
	      System.err.println("Account ID:    '" + acctID + "'");

	    // < acctNameArgName >
	    if ( cmdLine.hasOption(acctNameArgName) )
	    {
	      if ( acctSelMode != Helper.Mode.NAME )
	      {
	        System.err.println("<" + acctNameArgName + "> must only be set with <acct-sel-mode> = '" + Helper.Mode.NAME + "'");
	        throw new InvalidCommandLineArgsException();
	      }
	      
	      try
	      {
	        acctName.append( cmdLine.getOptionValue(acctNameArgName) );
	      }
	      catch ( Exception exc )
	      {
	        System.err.println("Could not parse <" + acctNameArgName + ">");
	        throw new InvalidCommandLineArgsException();
	      }
	    }
	    else
	    {
	      if ( acctSelMode == Helper.Mode.NAME )
	      {
	        System.err.println("<" + acctNameArgName + "> must be set with <acct-sel-mode> = '" + Helper.Mode.NAME + "'");
	        throw new InvalidCommandLineArgsException();
	      }      
	    }
	    
	    if ( ! scriptMode )
	      System.err.println("Account name:  '" + acctName + "'");
  }
  
  // ------------------------------


  // ------------------------------


}

