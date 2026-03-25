package org.kmymoney.tools.xml.helper;

import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.KMyMoneyWritableSecurity;
import org.kmymoney.base.basetypes.simple.KMMSecID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;

public class SecurityHelper
{

	public static KMyMoneySecurity getSec(
			Helper.CmdtySecSingleSelMode secSelMode, 
			KMMSecID secID, String isin, String secName, 
			KMyMoneyFile kmmFile,
			boolean scriptMode) throws Exception {
		KMyMoneySecurity sec = null;

		if ( secSelMode == Helper.CmdtySecSingleSelMode.ID ) {
			if ( secID != null ) {
				sec = kmmFile.getSecurityByID(secID);
				if ( sec == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find security with this ID");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <secID> is null");
			}
		} else if ( secSelMode == Helper.CmdtySecSingleSelMode.ISIN ) {
			// CAUTION: This branch is *not necessarily* redundant to
			// the Mode.ID / SubMode.SEC_ID_TYPE branch above
			// (it only is in the project's specific test file, which
			// reflects the way the author organizes his data, but by
			// no means is the only "correct", let alone conceivable way).
			if ( isin != null ) {
				sec = kmmFile.getSecurityByCode(isin);
				if ( sec == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find a security with this ISIN.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <isin> is null");
			}
		} else if ( secSelMode == Helper.CmdtySecSingleSelMode.NAME ) {
			if ( secName != null ) {
				sec = kmmFile.getSecurityByNameUniq(secName);
				if ( sec == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find a security (uniquely) matching this name.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <secName> is null");
			}
		}
		
		if ( sec == null ) {
			System.err.println("Something's wrong");
		}
		
		return sec;
	}

	public static KMyMoneyWritableSecurity getWrtSec(
			Helper.CmdtySecSingleSelMode secSelMode, 
			KMMSecID secID, String isin, String secName, 
			KMyMoneyWritableFile kmmFile,
			boolean scriptMode) throws Exception {
		KMyMoneyWritableSecurity sec = null;

		if ( secSelMode == Helper.CmdtySecSingleSelMode.ID ) {
			if ( secID != null ) {
				sec = kmmFile.getWritableSecurityByID(secID);
				if ( sec == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find security with this ID");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <secID> is null");
			}
		} else if ( secSelMode == Helper.CmdtySecSingleSelMode.ISIN ) {
			// CAUTION: This branch is *not necessarily* redundant to
			// the Mode.ID / SubMode.SEC_ID_TYPE branch above
			// (it only is in the project's specific test file, which
			// reflects the way the author organizes his data, but by
			// no means is the only "correct", let alone conceivable way).
			if ( isin != null ) {
				sec = kmmFile.getWritableSecurityByCode(isin);
				if ( sec == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find a security with this ISIN.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <isin> is null");
			}
		} else if ( secSelMode == Helper.CmdtySecSingleSelMode.NAME ) {
			if ( secName != null ) {
				sec = kmmFile.getWritableSecurityByNameUniq(secName);
				if ( sec == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find a security (uniquely) matching this name.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <secName> is null");
			}
		}
		
		if ( sec == null ) {
			System.err.println("Something's wrong: Cannot get security.");
		}
		
		return sec;
	}

}
