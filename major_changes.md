# Major Changes

## V. 0.9 &rarr; 0.10
Followed the deprecation of `FixedPointNumber` in the modules
"(Core) API", V. 1.9,
"Specialized Entitites", V. 0.4 and
"API Extended", V. 1.9:

Changed various implementations so that it's (almost) not used any more.

## V. 0.8 &rarr; 0.9
* New tools: 
  * GetTrxList
  * GetTrxSpltList
  * TestSel[Acct|Prc|Sec]

* Existing tools:
  * Better encapsulation of selection of accounts, securities and prices.
  * GenSec: More optional fields; check whether security with given ISIN already exists.
  * Overall: Small improvements, general maintenance

## V. 0.7 &rarr; 0.8
* New tools: 
  * Dump

* Existing tools:
  * GetAccountInfo: Now lists newly-introduced list of account-reconciliations.
  * GetTrxSpltInfo: Now returns number and payee-ID.

## V. 0.6 &rarr; 0.7
* New tools: 
  * UpdSec, GenAcct (finally!), GetCurrList, GetPrcList.
  * Tag tools (new entity): GenTag, GetTagInfo, getTatList, UpdTag.

* Existing tools: 
  * For parsing command-line options: Replaced `GnuParser` by `DefaultParser` (the former has been deprecated).
  * Payees have transactions (cf. module "API") ==> adapted GetPyeInfo
  * Analogously: Institutions have accounts ==> adapted GetInstInfo
  * Fixed a few small bugs

## V. 0.5 &rarr; 0.6
Created and added a number of tools:

* Package `...get`: Tools for getting information from KMyMoney files:
	* package `...get.list`: Simple tools that print an unfiltered list of all entries of a given entity. Rather low-level.
	* package `...get.info`: Simple tools that print the information of one entry of one entity. No bells, no whistles. A little bit of convenience, however, in how selecting the entry (not just by ID).
	* package `...get.sonstige`: Specialized tool (currently, only one) that retrieve specific information from the KMyMoney file.

* Package `...gen`: Tools for generating new entries in KMyMoney files:
	* package `...gen.simple`: Tools that generate exactly one entry of a given entity, with virtually no business logic involved (i.e., the user provides all data as is). No convenience.
	* package `...gen.complex`: Tools that generate one or more entries of one of more given entities, with business logic involved. Convenience where possible.

* Package `...upd`: Tools for updating entries in KMyMoney files:

	Simple tools that update specific fields of one entry of a given entity. As in package `gen.info`: No bells, no whistles. A little bit of convenience, however, in how selecting the entry (not just by ID).
