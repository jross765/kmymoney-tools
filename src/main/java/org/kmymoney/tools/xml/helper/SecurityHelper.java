package org.kmymoney.tools.xml.helper;

import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.base.basetypes.simple.KMMSecID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;

public class SecurityHelper
{

	public static KMyMoneySecurity getSec(
			xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode mode, 
			KMMSecID secID, String isin, String secName, 
			KMyMoneyFile gcshFile) throws Exception {
		KMyMoneySecurity sec = null;

		if ( mode == xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ID ) {
			if ( secID != null ) {
				sec = gcshFile.getSecurityByID(secID);
				if ( sec == null ) {
					System.err.println("Could not find security with this ID");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <secID> is null");
			}
		} else if ( mode == xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.ISIN ) {
			// CAUTION: This branch is *not necessarily* redundant to
			// the Mode.ID / SubMode.SEC_ID_TYPE branch above
			// (it only is in the project's specific test file, which
			// reflects the way the author organizes his data, but by
			// no means is the only "correct", let alone conceivable way).
			if ( isin != null ) {
				sec = gcshFile.getSecurityByCode(isin);
				if ( sec == null ) {
					System.err.println("Could not find a security with this ISIN.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <isin> is null");
			}
		} else if ( mode == xyz.schnorxoborx.base.cmdlinetools.Helper.CmdtySecSingleSelMode.NAME ) {
			if ( secName != null ) {
				sec = gcshFile.getSecurityByNameUniq(secName);
				if ( sec == null ) {
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

}
