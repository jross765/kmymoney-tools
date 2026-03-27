package org.kmymoney.tools.xml.helper;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.write.KMyMoneyWritableAccount;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.base.basetypes.simple.KMMAcctID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.Helper;

public class AccountHelper
{

	public static KMyMoneyAccount getAcct(
			Helper.Mode acctSelMode, 
			KMMAcctID acctID, String acctName, boolean acctNameQualif,
			KMyMoneyFile kmmFile,
			boolean scriptMode) throws Exception {
		KMyMoneyAccount acct = null;

	    if ( acctSelMode == Helper.Mode.ID ) {
			if ( acctID != null ) {
				acct = kmmFile.getAccountByID(acctID);
				if ( acct == null )
				{
					if ( ! scriptMode )
			          System.err.println("Could not find an account with this ID");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <acctID> is null");
			}
	    } else if ( acctSelMode == Helper.Mode.NAME ) {
			if ( acctName != null ) {
				acct = kmmFile.getAccountByNameUniq(acctName, acctNameQualif);
				if ( acct == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find an account (uniquely) matching this name.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <acctName> is null");
			}
	    }
		
		if ( acct == null ) {
			System.err.println("Something's wrong: Cannot get account.");
		}
		
		return acct;
	}

	public static KMyMoneyWritableAccount getWrtAcct(
			Helper.Mode acctSelMode, 
			KMMAcctID acctID, String acctName, 
			KMyMoneyWritableFile gcshFile,
			boolean scriptMode) throws Exception {
		KMyMoneyWritableAccount acct = null;

	    if ( acctSelMode == Helper.Mode.ID ) {
			if ( acctID != null ) {
				acct = gcshFile.getWritableAccountByID(acctID);
				if ( acct == null )
				{
					if ( ! scriptMode )
			          System.err.println("Could not find an account with this ID");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <acctID> is null");
			}
	    } else if ( acctSelMode == Helper.Mode.NAME ) {
			if ( acctName != null ) {
				acct = gcshFile.getWritableAccountByNameUniq(acctName, true);
				if ( acct == null ) {
					if ( ! scriptMode )
						System.err.println("Could not find an account (uniquely) matching this name.");
					throw new NoEntryFoundException();
				}
			} else {
				throw new IllegalArgumentException("argument <acctName> is null");
			}
	    }
		
		if ( acct == null ) {
			System.err.println("Something's wrong: Cannot get account.");
		}
		
		return acct;
	}

}
