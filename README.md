# Notes on the Module "Tools"

## What Does It Do?

This module is a collection of various ready-to-use tools reading and manipulating 
KMyMoney 
XML files. They are, of course, based on the module "Base", "API" and "Extensions", 
but *not* (technically) on "Examples".

You will notice that the tools partially pull on the examples in the "Examples" 
module, and thus we have a *logical* dependency here. Currently, there are many 
similarities between the two modules, but expect those two modules to deviate 
from one another in the course of the future releases to come.

## What is This Repo's Relationship with the Other Repos?

* This is a module-level repository which is part of a multi-module project, i.e. it has a parent and several siblings. 

  [Parent](https://github.com/jross765/JKMyMoneyLibNTools.git)

* Under normal circumstances, you cannot compile it on its own (at least not without further preparation), but instead, you should clone it together with the other repos and use the parent repo's build-script.

* This repository contains no history before V. 1.7 (cf. notes in parent repo).

## Major Changes 
### V. 0.7 &rarr; 0.8
* New tools: 
  * Dump

* Existing tools:
  * GetAccountInfo: Now lists newly-introduced list of account-reconciliations.
  * GetTrxSpltInfo: Now returns number and payee-ID.

### V. 0.6 &rarr; 0.7
* New tools: 
  * UpdSec, GenAcct (finally!), GetCurrList, GetPrcList.
  * Tag tools (new entity): GenTag, GetTagInfo, getTatList, UpdTag.

* Existing tools: 
  * For parsing command-line options: Replaced `GnuParser` by `DefaultParser` (the former has been deprecated).
  * Payees have transactions (cf. module "API") ==> adapted GetPyeInfo
  * Analogously: Institutions have accounts ==> adapted GetInstInfo
  * Fixed a few small bugs

### V. 0.5 &rarr; 0.6
Created and added a number of tools:

* Package `...get`: Tools for getting information from KMyMoney files:
	* package `...gen.simple`: Tools that generate exactly one entry of a given entity, with virtually no business logic involved (i.e., the user provides all data as is). No convenience.
	* package `...gen.complex`: Tools that generate one or more entries of one of more given entities, with business logic involved. Convenience where possible.

* Package `...gen`: Tools for generating new entries in KMyMoney files:
	* package `...gen.list`: Simple tools that print an unfiltered list of all entries of a given entity. Rather low-level.
	* package `...gen.info`: Simple tools that print the information of one entry of one entity. No bells, no whistles. A little bit of convenience, however, in how selecting the entry (not just by ID).
	* package `...gen.sonstige`: Specialized tool (currently, only one) that retrieve specific information from the KMyMoney file.

* Package `...upd`: Tools for updating entries in KMyMoney files:

	Simple tools that update specific fields of one entry of a given entity. As in package `gen.info`: No bells, no whistles. A little bit of convenience, however, in how selecting the entry (not just by ID).

## Planned
./.

## Known Issues
* The programs that generate new objects (`gen.simple.GenXYZ`) currently only work (reliably) when at least one object of the same type (an institution, say) is already in the file (cf. according note on issue in README file for package "API").
