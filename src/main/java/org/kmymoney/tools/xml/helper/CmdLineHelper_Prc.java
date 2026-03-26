package org.kmymoney.tools.xml.helper;

import java.time.LocalDate;

import org.apache.commons.cli.CommandLine;
import org.kmymoney.base.basetypes.complex.KMMPrcID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.simple.KMMIDNotSetException;

import xyz.schnorxoborx.base.cmdlinetools.Helper;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.dateutils.LocalDateHelpers;

public class CmdLineHelper_Prc {

  public enum PrcSelectMode
  {
    ID,
    SEC_DATE,
    ISIN_DATE
  }

  public enum PrcSelectSubMode // for <prc-select-mode> = 'ID' only
  {
	DIRECT,
    INDIRECT
  }

  // -----------------------------------------------------------------
  
  public static void setPrcID_direct(KMMPrcID prcID,
			CommandLine cmdLine,
			PrcSelectMode prcSelMode,
			boolean scriptMode) throws InvalidCommandLineArgsException, KMMIDNotSetException
  {
	  KMMPrcID locPrcID = getPrcID_direct(cmdLine,
			  							prcSelMode, 
			  							scriptMode );

	  prcID.reset();
	  prcID.set(locPrcID);
  }

  public static KMMPrcID getPrcID_direct(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  return getPrcID_direct(cmdLine,
			  prcSelMode,
			  "prc-sel-mode",
			  "price-id",
			  scriptMode );
  }

  public static KMMPrcID getPrcID_direct(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode,
		  String prcSelModeArgName,
		  String prcIDArgName,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  KMMPrcID prcID = null;
	    
	  // < prcIDArgName >
	  if ( cmdLine.hasOption( prcIDArgName ) )
	  {
		  if ( prcSelMode != PrcSelectMode.ID )
		  {
			  System.err.println("<" + prcIDArgName + "> may only be set with <" + prcSelMode + "> = " + PrcSelectMode.ID);
			  throw new InvalidCommandLineArgsException();
		  }
	    		
		  try
		  {
			  prcID = KMMPrcID.parse( cmdLine.getOptionValue(prcIDArgName) ); 
		  }	
		  catch ( Exception exc )
		  {
			  System.err.println("Could not parse <" + prcIDArgName+ ">");
	          throw new InvalidCommandLineArgsException();
		  }
	  }
	  else
	  {
		  if ( prcSelMode == PrcSelectMode.ID )
		  {
			  System.err.println("<" + prcIDArgName+ "> must be set with <" + prcSelMode + "> = " + PrcSelectMode.ID);
			  throw new InvalidCommandLineArgsException();
		  }
	  }
	  
	  if ( ! scriptMode )
		  System.err.println("Price ID (direct): " + prcID);
	  
	  return prcID;
  }

  //------------------------------
  
  public static KMMPrcID getPrcID_indirect(CommandLine cmdLine,
		  PrcSelectMode prcSelmode, PrcSelectSubMode prcSelSubMode,
			boolean scriptMode) throws InvalidCommandLineArgsException
  {
	  return getPrcID_indirect(cmdLine,
			  prcSelmode, prcSelSubMode,
			  "price-from-sec-curr-id", "price-to-curr-id", 
			  "price-date-format", "price-date",
			  scriptMode);
  }

  public static KMMPrcID getPrcID_indirect(CommandLine cmdLine,
		  PrcSelectMode prcSelmode, PrcSelectSubMode prcSelSubMode,
			String fromSecCurrIDArgName, String toCurrIDArgName, 
			String dateFormatArgName, String dateArgName,
			boolean scriptMode) throws InvalidCommandLineArgsException
  {
		if ( prcSelmode != PrcSelectMode.ID )
		{
			throw new IllegalArgumentException( "arg <prc-sel-mode> must be " + PrcSelectMode.ID );
		}

		if ( prcSelSubMode != PrcSelectSubMode.INDIRECT )
		{
			throw new IllegalArgumentException( "arg <prc-sel-sub-mode> must be " + PrcSelectSubMode.INDIRECT );
		}

		KMMQualifSecCurrID fromSecCurrID = null;
		KMMQualifCurrID    toCurrID = null;
		Helper.DateFormat  dateFormat = null;
		LocalDate          date = null;

		// < fromSecCurrIDArgName >, < toCurrIDArgName >, < dateArgName >
		if ( cmdLine.hasOption( fromSecCurrIDArgName ) )
		{
			if ( !cmdLine.hasOption( toCurrIDArgName ) )
			{
				System.err.println("Error: <" + fromSecCurrIDArgName + "> and <" + toCurrIDArgName + "> must both either be set or unset" );
				throw new InvalidCommandLineArgsException();
			}

			if ( !cmdLine.hasOption( dateArgName ) )
			{
				System.err.println("Error: <" + fromSecCurrIDArgName + "> and <" + dateArgName + "> must both either be set or unset" );
				throw new InvalidCommandLineArgsException();
			}

			try
			{
				fromSecCurrID = KMMQualifSecCurrID.parse( cmdLine.getOptionValue( fromSecCurrIDArgName ) );
			}
			catch ( Exception exc )
			{
				System.err.println( "Could not parse <" + fromSecCurrIDArgName + ">" );
				throw new InvalidCommandLineArgsException();
			}

			try
			{
				toCurrID = KMMQualifCurrID.parse( cmdLine.getOptionValue( toCurrIDArgName ) );
			}
			catch ( Exception exc )
			{
				System.err.println( "Could not parse <" + toCurrIDArgName + ">" );
				throw new InvalidCommandLineArgsException();
			}

			try
			{
				dateFormat = Helper.DateFormat.valueOf( cmdLine.getOptionValue( dateFormatArgName ) );
			}
			catch ( Exception exc )
			{
				System.err.println( "Could not parse <" + dateFormatArgName + ">" );
				throw new InvalidCommandLineArgsException();
			}

			try
			{
				date = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue( dateArgName ), dateFormat );
			}
			catch ( Exception exc )
			{
				System.err.println( "Could not parse <" + dateArgName + ">" );
				throw new InvalidCommandLineArgsException();
			}
		}
		else
		{
			if ( cmdLine.hasOption( toCurrIDArgName ) )
			{
				System.err.println("Error: <" + fromSecCurrIDArgName + "> and <" + toCurrIDArgName + "> must both either be set or unset" );
				throw new InvalidCommandLineArgsException();
			}

			if ( cmdLine.hasOption( dateFormatArgName ) )
			{
				System.err.println("Error: <" + fromSecCurrIDArgName + "> and <" + dateFormatArgName + "> must both either be set or unset" );
				throw new InvalidCommandLineArgsException();
			}

			if ( cmdLine.hasOption( dateArgName ) )
			{
				System.err.println("Error: <" + fromSecCurrIDArgName + "> and <" + dateArgName + "> must both either be set or unset" );
				throw new InvalidCommandLineArgsException();
			}
		}

		if ( !scriptMode )
		{
			System.err.println( "From-Sec/Curr:  " + fromSecCurrID );
			System.err.println( "To-Curr:        " + toCurrID );
			System.err.println( "Date format:    " + dateFormat );
			System.err.println( "Date:           " + date );
		}

		KMMPrcID prcID = new KMMPrcID( fromSecCurrID, toCurrID, date );

		if ( !scriptMode )
			System.err.println( "Price ID (indirect): " + prcID );

		return prcID;
	}

  public static void setPrcID_indirect(KMMPrcID prcID, 
			CommandLine cmdLine, 
			PrcSelectMode prcSelMode, PrcSelectSubMode prcSelSubMode,
			boolean scriptMode) throws InvalidCommandLineArgsException
	{
		KMMPrcID locPrcID = getPrcID_indirect( cmdLine, prcSelMode, prcSelSubMode, scriptMode );

		prcID.set(locPrcID);
	}

	// ------------------------------

  public static void parsePrcStuffWrap(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode, PrcSelectSubMode prcSelSubMode,
		  KMMPrcID prcID, 
		  KMMQualifSecCurrID fromSecCurrID, KMMQualifCurrID toCurrID, 
		  Helper.DateFormat dateFormat, LocalDateWrp date,
		  StringBuffer isin,
		  boolean scriptMode) throws InvalidCommandLineArgsException, KMMIDNotSetException {
	  parsePrcStuffWrap(cmdLine,
			  			prcSelMode, prcSelSubMode,
			  			"price-id", prcID,
			  			"price-from-sec-curr-id", fromSecCurrID,
			  			"price-to-curr-id", toCurrID,
			  			"price-date-format", dateFormat,
			  			"price-date", date,
			  			"isin", isin,
			  			scriptMode);
  }
  
  public static void parsePrcStuffWrap(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode, PrcSelectSubMode prcSelSubMode,
		  String prcIDArgName, KMMPrcID prcID, 
		  String fromSecCurrIDArgName, KMMQualifSecCurrID fromSecCurrID,
		  String toCurrIDArgName, KMMQualifCurrID toCurrID,
		  String dateFormatArgName, Helper.DateFormat dateFormat,
		  String dateArgName, LocalDateWrp date,
		  String isinArgName, StringBuffer isin,
		  boolean scriptMode) throws InvalidCommandLineArgsException, KMMIDNotSetException {
	  if ( prcSelMode == PrcSelectMode.ID ) {
		  if ( cmdLine.hasOption( isinArgName ) ) {
			  System.err.println("<" + isinArgName + "> must not be set with <prc-sel-mode> = '" + PrcSelectMode.ID + "'");
			  throw new InvalidCommandLineArgsException();
		  }
			
		  if ( prcSelSubMode == PrcSelectSubMode.DIRECT ) {
			  parsePrcStuffWrap_ID_direct(cmdLine,
					  prcSelMode,
					  prcIDArgName, prcID,
					  scriptMode);
		  } else if ( prcSelSubMode == PrcSelectSubMode.INDIRECT ) {
			  parsePrcStuffWrap_ID_indirect(cmdLine,
					  prcSelMode, prcSelSubMode,
					  prcIDArgName, prcID,
					  fromSecCurrIDArgName, fromSecCurrID, 
					  toCurrIDArgName, toCurrID,
					  dateFormatArgName, dateFormat,
					  dateArgName, date, 
					  isinArgName, isin,
					  scriptMode);
		  }
	  } else if ( prcSelMode == PrcSelectMode.SEC_DATE ) {
		  if ( cmdLine.hasOption(prcIDArgName) ) {
			  System.err.println("<" + prcIDArgName + "> must not be set with <prc-sel-mode> = '" + PrcSelectMode.SEC_DATE + "'");
			  throw new InvalidCommandLineArgsException();
		  }
		  
		  if ( cmdLine.hasOption(isinArgName) ) {
			  System.err.println("<" + isinArgName + "> must not be set with <prc-sel-mode> = '" + PrcSelectMode.SEC_DATE + "'");
			  throw new InvalidCommandLineArgsException();
		  }
			
		  parsePrcStuffWrap_SecID_etc(cmdLine,
				  prcSelMode,
				  fromSecCurrIDArgName, fromSecCurrID,
				  toCurrIDArgName,
				  isinArgName,
				  dateFormatArgName, dateFormat,
				  dateArgName, date,
				  scriptMode);
	  } else if ( prcSelMode == PrcSelectMode.ISIN_DATE ) {
		  if ( cmdLine.hasOption(prcIDArgName) ) {
			  System.err.println("<" + prcIDArgName + "> must not be set with <prc-sel-mode> = '" + PrcSelectMode.ISIN_DATE + "'");
			  throw new InvalidCommandLineArgsException();
		  }
		  
		  if ( cmdLine.hasOption( fromSecCurrIDArgName ) ) {
			  System.err.println("<" + fromSecCurrIDArgName + "> must not be set with <prc-sel-mode> = '" + PrcSelectMode.ISIN_DATE + "'");
			  throw new InvalidCommandLineArgsException();
		  }
			
		  parsePrcStuffWrap_ISIN_etc(cmdLine,
				  prcSelMode,
				  isinArgName, isin,
				  fromSecCurrIDArgName,
				  toCurrIDArgName,
				  dateFormatArgName, dateFormat,
				  dateArgName, date, 
				  scriptMode);
	  }
  }
  
  private static void parsePrcStuffWrap_ID_direct(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode,
		  KMMPrcID prcID,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  	if ( prcSelMode != PrcSelectMode.ID ) {
			System.err.println("Error: Wrong <prc-sel-mode>" );
			throw new InvalidCommandLineArgsException();
	  	}
	  
	  parsePrcStuffWrap_ID_direct(cmdLine,
			  				prcSelMode,
			  				"price-id", prcID,
			  				scriptMode);
  }
  
  private static void parsePrcStuffWrap_ID_direct(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode,
		  String prcIDArgName, KMMPrcID prcID,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  	if ( prcSelMode != PrcSelectMode.ID ) {
			System.err.println("Error: Wrong <prc-sel-mode>" );
			throw new InvalidCommandLineArgsException();
	  	}
	  
	  // < prcIDArgName >
	  if ( cmdLine.hasOption(prcIDArgName) )
	  {
		  try
		  {
			  // No:
			  // secID = GCshSecID.parse( cmdLine.getOptionValue(secIDArgNanme) );
			  // Instead:
			  setPrcID_direct(prcID, 
					  cmdLine,
					  prcSelMode,
					  scriptMode);
		  }	
		  catch ( Exception exc )
		  {
			  System.err.println("Could not parse <" + prcIDArgName + ">");
	          throw new InvalidCommandLineArgsException();
		  }
	  }
	  else
	  {
		  System.err.println("Error: <" + prcIDArgName + "> must be set with <prc-sel-mode> = " + PrcSelectMode.ID);
		  throw new InvalidCommandLineArgsException();
	  }
  }
  
  private static void parsePrcStuffWrap_ID_indirect(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode, PrcSelectSubMode prcSelSubMode,
		  KMMPrcID prcID, 
		  KMMQualifSecCurrID fromSecCurrID, KMMQualifCurrID toCurrID, 
		  Helper.DateFormat dateFormat, LocalDateWrp date,
		  StringBuffer isin,
		  boolean scriptMode) throws InvalidCommandLineArgsException, KMMIDNotSetException {
	  	if ( prcSelMode != PrcSelectMode.ID ) {
			System.err.println("Error: Wrong <prc-sel-mode>" );
			throw new InvalidCommandLineArgsException();
	  	}
	  
	  parsePrcStuffWrap_ID_indirect(cmdLine,
			  					prcSelMode, prcSelSubMode,
			  					"price-id", prcID,
			  					"price-from-sec-curr-id", fromSecCurrID,
			  					"price-to-curr-id", toCurrID,
			  					"price-date-format", dateFormat,
			  					"price-date", date,
			  					"isin", isin,
			  					scriptMode);
  }
  
  private static void parsePrcStuffWrap_ID_indirect(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode, PrcSelectSubMode prcSelSubMode,
		  String prcIDArgName, KMMPrcID prcID, 
		  String fromSecCurrIDArgName, KMMQualifSecCurrID fromSecCurrID,
		  String toCurrIDArgName, KMMQualifCurrID toCurrID,
		  String dateFormatArgName, Helper.DateFormat dateFormat,
		  String dateArgName, LocalDateWrp date,
		  String isinArgName, StringBuffer isin,
		  boolean scriptMode) throws InvalidCommandLineArgsException, KMMIDNotSetException {
	  	if ( prcSelMode != PrcSelectMode.ID ) {
			System.err.println("Error: Wrong <prc-sel-mode>" );
			throw new InvalidCommandLineArgsException();
	  	}
	  
	  	if ( prcSelSubMode != PrcSelectSubMode.INDIRECT ) {
			System.err.println("Error: Wrong <prc-sel-sub-mode>" );
			throw new InvalidCommandLineArgsException();
	  	}
	  
	// < secIDArgNanme >
	// Well, not directly, but rather:
	// < fromSecCurrIDArgName >, < toSecCurrIDArgName >, < dateArgName >
	if ( cmdLine.hasOption(fromSecCurrIDArgName) && 
		 cmdLine.hasOption(toCurrIDArgName) &&
		 cmdLine.hasOption(dateFormatArgName) &&
		 cmdLine.hasOption(dateArgName) ) 
	{
	    if ( cmdLine.hasOption( isinArgName ) )
	    {
	      System.err.println("<" + isinArgName + "> must not be set with <prc-sel-mode> = '" + PrcSelectMode.ID + "'");
	      throw new InvalidCommandLineArgsException();
	    }
		
	    // No:
//	    	prcID = getPrcID_ByID(cmdLine,
//	    				mode, subMode,
//	    				scriptMode);
		// Instead:
	    setPrcID_indirect(prcID, 
	    		cmdLine,
	    		prcSelMode, prcSelSubMode,
	    		scriptMode);
		
		if ( prcID == null )
		{
	        System.err.println("Could not get price ID from " + 
	        				   "<" + fromSecCurrIDArgName + ">/<" + toCurrIDArgName + ">/<" + dateFormatArgName + ">/<" + dateArgName + ">");
	        throw new InvalidCommandLineArgsException();
		}
	}
	else
	{
		System.err.println("Tuple <" + fromSecCurrIDArgName + ">/" + 
								  "<" + toCurrIDArgName + ">/" +
								  "<" + dateFormatArgName + ">/" +
								  "<" + dateArgName + "> " +
						   "must be set with " +
						   "<prc-sel-mode> = '" + PrcSelectMode.ID + "' and " +
						   "<prc-sel-sub-mode> = '" +  PrcSelectSubMode.INDIRECT + "'");
		throw new InvalidCommandLineArgsException();
	}
  }

  private static void parsePrcStuffWrap_SecID_etc(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode,
		  String fromSecCurrIDArgName, KMMQualifSecCurrID fromSecCurrID,
		  String toCurrIDArgName,
		  String isinArgName,
		  String dateFormatArgName, Helper.DateFormat dateFormat,
		  String dateArgName, LocalDateWrp date,
		  boolean scriptMode) throws InvalidCommandLineArgsException {
	  	if ( prcSelMode != PrcSelectMode.SEC_DATE ) {
			System.err.println("Error: Wrong <prc-sel-mode>" );
			throw new InvalidCommandLineArgsException();
	  	}
	  
		// < secCurrIDArgName >, < toCurrIDArgName >, < dateArgName >
		if ( cmdLine.hasOption( fromSecCurrIDArgName ) )
		{
			if ( cmdLine.hasOption( toCurrIDArgName ) )
			{
				System.err.println("Error: <" + toCurrIDArgName + "> must not be set with <prc-sel-mode> = " + PrcSelectMode.SEC_DATE);
				throw new InvalidCommandLineArgsException();
			}

			if ( cmdLine.hasOption( isinArgName ) )
			{
				System.err.println("Error: <" + isinArgName + "> must not be set with <prc-sel-mode> = " + PrcSelectMode.SEC_DATE);
				throw new InvalidCommandLineArgsException();
			}

			if ( !cmdLine.hasOption( dateFormatArgName ) )
			{
				System.err.println("Error: <" + dateFormatArgName + "> must be set with <prc-sel-mode> = " + PrcSelectMode.SEC_DATE);
				throw new InvalidCommandLineArgsException();
			}

			if ( !cmdLine.hasOption( dateArgName ) )
			{
				System.err.println("Error: <" + dateArgName + "> must be set with <prc-sel-mode> = " + PrcSelectMode.SEC_DATE);
				throw new InvalidCommandLineArgsException();
			}

			try
			{
				KMMQualifSecCurrID locFromSecCurrID = KMMQualifSecCurrID.parse( cmdLine.getOptionValue( fromSecCurrIDArgName ) );
				fromSecCurrID.set( locFromSecCurrID );
			}
			catch ( Exception exc )
			{
				System.err.println( "Could not parse <" + fromSecCurrIDArgName + ">" );
				throw new InvalidCommandLineArgsException();
			}
			
			try
			{
				dateFormat = Helper.DateFormat.valueOf( cmdLine.getOptionValue( dateFormatArgName ) );
			}
			catch ( Exception exc )
			{
				System.err.println( "Could not parse <" + dateFormatArgName + ">" );
				throw new InvalidCommandLineArgsException();
			}

			try
			{
				date.dat = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue( dateArgName ), dateFormat );
			}
			catch ( Exception exc )
			{
				System.err.println( "Could not parse <" + dateArgName + ">" );
				throw new InvalidCommandLineArgsException();
			}
		}
		else
		{
			System.err.println("Error: <" + fromSecCurrIDArgName + "> must be set with <prc-sel-mode> = " + PrcSelectMode.SEC_DATE);
			throw new InvalidCommandLineArgsException();
		}

		if ( !scriptMode )
		{
			System.err.println( "Sec/Curr:       " + fromSecCurrID );
			System.err.println( "Date format:    " + dateFormat );
			System.err.println( "Date:           " + date.dat );
		}
  }

  private static void parsePrcStuffWrap_ISIN_etc(
		  CommandLine cmdLine,
		  PrcSelectMode prcSelMode, 
		  String isinArgName, StringBuffer isin,
		  String fromSecCurrIDArgName,
		  String toCurrIDArgName,
		  String dateFormatArgName, Helper.DateFormat dateFormat,
		  String dateArgName, LocalDateWrp date,
		  boolean scriptMode) throws InvalidCommandLineArgsException, KMMIDNotSetException {
	  	if ( prcSelMode != PrcSelectMode.ISIN_DATE ) {
			System.err.println("Error: Wrong <prc-sel-mode>" );
			throw new InvalidCommandLineArgsException();
	  	}
	  
		// < isinArgName >, < toCurrIDArgName >, < dateArgName >
		if ( cmdLine.hasOption( isinArgName ) )
		{
			if ( cmdLine.hasOption( fromSecCurrIDArgName ) )
			{
				System.err.println("Error: <" + fromSecCurrIDArgName + "> must not be set with <prc-sel-mode> = " + PrcSelectMode.ISIN_DATE );
				throw new InvalidCommandLineArgsException();
			}

			if ( cmdLine.hasOption( toCurrIDArgName ) )
			{
				System.err.println("Error: <" + toCurrIDArgName + "> must not be set with <prc-sel-mode> = " + PrcSelectMode.ISIN_DATE );
				throw new InvalidCommandLineArgsException();
			}

			if ( ! cmdLine.hasOption( dateFormatArgName ) )
			{
				System.err.println("Error: <" + dateFormatArgName + "> must be set with <prc-sel-mode> = " + PrcSelectMode.ISIN_DATE);
				throw new InvalidCommandLineArgsException();
			}

			if ( ! cmdLine.hasOption( dateArgName ) )
			{
				System.err.println("Error: <" + dateArgName + "> must be set with <prc-sel-mode> = " + PrcSelectMode.ISIN_DATE);
				throw new InvalidCommandLineArgsException();
			}

			try
			{
				isin.append( cmdLine.getOptionValue( isinArgName ) );
			}
			catch ( Exception exc )
			{
				System.err.println( "Could not parse <" + isinArgName + ">" );
				throw new InvalidCommandLineArgsException();
			}

			try
			{
				dateFormat = Helper.DateFormat.valueOf( cmdLine.getOptionValue( dateFormatArgName ) );
			}
			catch ( Exception exc )
			{
				System.err.println( "Could not parse <" + dateFormatArgName + ">" );
				throw new InvalidCommandLineArgsException();
			}

			try
			{
				date.dat = LocalDateHelpers.parseLocalDate( cmdLine.getOptionValue( dateArgName ), dateFormat );
			}
			catch ( Exception exc )
			{
				System.err.println( "Could not parse <" + dateArgName + ">" );
				throw new InvalidCommandLineArgsException();
			}
		}
		else
		{
			if ( cmdLine.hasOption( toCurrIDArgName ) )
			{
				System.err.println("Error: <" + isinArgName + "> and <" + toCurrIDArgName + "> must both either be set or unset" );
				throw new InvalidCommandLineArgsException();
			}

			if ( cmdLine.hasOption( dateFormatArgName ) )
			{
				System.err.println("Error: <" + isinArgName + "> and <" + dateFormatArgName + "> must both either be set or unset" );
				throw new InvalidCommandLineArgsException();
			}

			if ( cmdLine.hasOption( dateArgName ) )
			{
				System.err.println("Error: <" + isinArgName + "> and <" + dateArgName + "> must both either be set or unset" );
				throw new InvalidCommandLineArgsException();
			}
		}

		if ( !scriptMode )
		{
			System.err.println( "ISIN:           " + isin );
			System.err.println( "Date format:    " + dateFormat );
			System.err.println( "Date:           " + date.dat );
		}
  }

}
