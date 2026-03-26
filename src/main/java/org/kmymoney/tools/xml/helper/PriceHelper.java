package org.kmymoney.tools.xml.helper;

import java.time.LocalDate;

import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.KMyMoneyWritablePrice;
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;

public class PriceHelper
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PriceHelper.class);

	public static KMyMoneyPrice getPrc(
			CmdLineHelper_Prc.PrcSelectMode prcSelMode, 
			KMMPriceID prcID, 
			KMMQualifSecCurrID fromSecCurrID, KMMQualifCurrID toCurrID, 
			Helper.DateFormat dateFormat, LocalDate date,
			String isin,
			KMyMoneyFile kmmFile,
			boolean scriptMode) throws Exception {
		KMyMoneyPrice prc = null;

	    if ( prcSelMode == CmdLineHelper_Prc.PrcSelectMode.ID ) {
			if ( prcID != null ) {
				prc = kmmFile.getPriceByID(prcID);
				if ( prc == null )
				{
					if ( ! scriptMode )
			          System.err.println("Could not find a price with this ID");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <prcID> is null");
			}
	    } else if ( prcSelMode == CmdLineHelper_Prc.PrcSelectMode.SEC_DATE ) {
			if ( fromSecCurrID != null &&
				 date != null ) {
				if ( ! fromSecCurrID.isSet() ) {
					LOGGER.debug("getPrc: security-ID is not set");
					return null;
				}
				prc = kmmFile.getPriceByQualifSecCurrIDDate(fromSecCurrID, date);
				if ( prc == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find a price for this security-ID and date.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <secID> or <date> is null");
			}
	    } else if ( prcSelMode == CmdLineHelper_Prc.PrcSelectMode.ISIN_DATE ) {
			if ( isin != null &&
				 date != null ) {
				if ( isin.equals("") ) {
					LOGGER.debug("getPrc: ISIN is empty");
					return null;
				}
				KMyMoneySecurity sec = kmmFile.getSecurityByCode(isin.toString());
				LOGGER.debug("getPrc: Security with ISIN '" + isin + "' is: " + sec.getID());
				prc = kmmFile.getPriceBySecIDDate(sec.getID(), date);
				if ( prc == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find a price for this ISIN and date.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <isin> or <date> is null");
			}
	    }
		
		if ( prc == null ) {
			System.err.println("Something's wrong: Cannot get price.");
		}
		
		return prc;
	}

	public static KMyMoneyWritablePrice getWrtPrc(
			CmdLineHelper_Prc.PrcSelectMode prcSelMode, 
			KMMPriceID prcID, 
			KMMQualifSecCurrID fromSecCurrID, KMMQualifCurrID toCurrID, 
			Helper.DateFormat dateFormat, LocalDate date,
			String isin,
			KMyMoneyWritableFile kmmFile,
			boolean scriptMode) throws Exception {
		KMyMoneyWritablePrice prc = null;

	    if ( prcSelMode == CmdLineHelper_Prc.PrcSelectMode.ID ) {
			if ( prcID != null ) {
				prc = kmmFile.getWritablePriceByID(prcID);
				if ( prc == null )
				{
					if ( ! scriptMode )
			          System.err.println("Could not find a price with this ID");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <prcID> is null");
			}
	    } else if ( prcSelMode == CmdLineHelper_Prc.PrcSelectMode.SEC_DATE ) {
			if ( fromSecCurrID != null &&
				 date != null ) {
				if ( isin.equals("") ) {
					LOGGER.debug("getPrc: security-ID is not set");
					return null;
				}
				prc = kmmFile.getWritablePriceByQualifSecCurrIDDate(fromSecCurrID, date);
				if ( prc == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find a price for this security-ID and date.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <secID> or <date> is null");
			}
	    } else if ( prcSelMode == CmdLineHelper_Prc.PrcSelectMode.ISIN_DATE ) {
			if ( isin != null &&
				 date != null ) {
				if ( isin.equals("") ) {
					LOGGER.debug("getPrc: ISIN is empty");
					return null;
				}
				KMyMoneySecurity sec = kmmFile.getSecurityByCode(isin.toString());
				LOGGER.debug("getWrtPrc: Security with ISIN '" + isin + "' is: " + sec.getID());
				prc = kmmFile.getWritablePriceBySecIDDate(sec.getID(), date);
				if ( prc == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find a price for this ISIN and date.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <isin> or <date> is null");
			}
	    }
		
		if ( prc == null ) {
			System.err.println("Something's wrong: Cannot get price.");
		}
		
		return prc;
	}

}
