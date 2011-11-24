Stephen Crawley, Centre for Microscopy and Microanalysis, University of Queensland.

# Introduction

This document provides a technical specification of the network protocol that is used for communication between an ACLS server and a client application.  The current version of the protocol supports login and logout from an ACLS controlled facility, and other functions.

This document describes the versions of the protocol as implemented in ACLS versions 20.5.9 and 30.0, based on a reading of the source code for the login clients supplied to CMM.  Server side behaviour is largely deduced from what the client seems to expect.

## Termininology

The following terminology is specific to ACLS:

* Facility - a managed "thing", for example a microscope or some other resource.
* Virtual Facility - in ACLS version 30.0, one instance of the client can stand for multiple distinct sub-facilities.  The client is known as a virtual facility.  (The original purpose of this is to support cases where it is not appropriate to install the login program on the actual facility; e.g. because the facility is a lab space, etcetera.  This concept can also be used to implement proxy login; see below.)
* Subfacility - this is one of the actual managed "things" that is part of a virtual facility.  ACLS uses a identifier (not a name) to denote a subfacility. 
* Account - an account is what facility usage is accounted against for billing or other purposes.  An account may also be described to the end user as a project, depending on how ACLS is configured.
* Training Certificate - a training certificate (as used below) is actually an indication that the user is trained for the use of the specific facility rather than the certificate itself.
* Onsite Assistance - this is an indication that the user needs someone to help them use the facility.  (The client will ask for a staff login.)  Later versions of ACLS refer to this as "onsite supervision".
* System Password - this is a password that allows someone to unlock the screen of a facility without logging in.

## Message Meta-Syntax

Message formats are specified in a variation of [Extended Backus-Naur Form (EBNF)](http://en.wikipedia.org/wiki/EBNF):

  * The production symbol is "::=".  There is one non-terminal symbol on the left-hand side and a sequence of symbols on the right-hand side.  (This means that the LHS non-terminal symbol stands for a sequence of symbols that match the right-hand side.
  * The alternation symbol is "|".
  * Square brackets "![" and "]" enclose an optional group.
  * Curly brackets "{" and "}" enclose a repeating group.
  * Round brackets "(" and ")" are for simple grouping; e.g. to make it clear what an alternation applies to.
  * The space between symbols implies concatenation.
  * Symbols starting with an upper-case letter are either non-terminals or names of terminal symbols defined in the "message structure" section below.

# Protocol Overview

The ACLS protocol uses TCP/IP, with port 1024 its port.  According to IANA, port 1024 a reserved port, but it is used by a variety applications including (unfortunately) various kinds of malware.  (The current ACLS login client and server have the port number hard-wired.)

The protocol is simple form of client initiated remote procedure call.  The protocol has the following general structure:

1. The client opens the TCP/IP connection to the server
1. The server immediately responds with a status message.
1. The client sends a request to the server.  This is a line of text terminated by an end-of-line marker.
1. The server performs the requested action (if appropriate), and responds with another line of text followed by an end-of-line marker.
1. The client closes the TCP connection.

The client / server messages are encoded in ASCII.

The end-of-line sequence is ... *Need to use a protocol sniffer to find this out, I think.  Or read the Indy documentation to find out what ReadLn does on a Indy client connection.*

The TCP/IP connection does not use SSL/TLS or any other transport level security.

Implementation notes:

  i. The ACLS server and ACLS provided clients are implemented using the Delphi Indy modules. However, at the base level they are sending and receiving sequences of characters that conform to this specification.  Thus, there is no reason that 3rd party applications need to be implemented in Delphi.
  i. The ACLS server performs access control (in part) based on the IP address of the client.  If a proxy is inserted between the client and server, then ACLS needs to be configured (or reconfigured) so that the logical facilities are all associated with the IP address of the proxy.  
  i. Version 30.0 of ACLS supports "virtual facilities" in which one IP address corresponds to multiple facilities.  It looks like we can use this to implement proxying.

# Message Structure

As described above, all request and response messages consist of a line of text followed by an end-of-line sequence.  The overall protocol schema is that the client sends a request message, and the server then responds with a response message.

## Message Delimiters

Request and response elements consist of a number of different kinds of text string.  A text string consists of one or more characters, with special separator or delimiter characters starting / separating / ending each character sequence.

There are 10 kinds of delimiter:

* CommandDelimiter (':') terminates a protocol command string.  A protocol command string is a sequence of one or more decimal digits, as specified in the following sections.
* !Delimiter ('|')
* TimeDelimiter ('[')
* AccountDelimiter (']')
* AccountSeparator (';')
* OnsiteAssistDelimiter ('~') (version 20.5)
* CertificateDelimiter ('&')
* NoteDelimiter ('~') - (same as OnsiteAssistDelimiter)
* FacilityDelimiter ('?')
* SystemPassDelimiter ('/')

In fact, the majority of these so-called delimiters really function as position markers for particular arguments in the request or response message.  Only CommandDelimiter, !Delimiter and AccountSeparator are real delimiters in the normal usage of that term.  In general, the actual usage of these characters cannot be described in a systematic way.

Notes:

  i. The mechanisms used to parse messages mean that various delimiters and separator characters cannot safely be used in certain arguments.  The "micro-syntax" rules below should cover these cases.

## Acceptable values - micro-syntax rules

The following rules constrain the acceptable values for things like user (login) names, passwords and so on.

* A command string must be a decimal integer value as listed below.  (A negative value is syntactically legal, though no negative command numbers currently exist.)
* A user name / login name or password can contain any character except for Delimiter and CommandDelimiter.
* A facility name can contain any character except for Delimiter and FacilityDelimiter.
* A subfacility id can contain any character except for Delimiter, FacilityDelimiter and AccountSeparator.
* An account name can contain any character except for Delimiter, CertificateDelimiter, AccountDelimiter and AccountSeparator.
* An organization name can contain any character except for Delimiter and FacilityDelimiter.
* A note string may can contain any character except for Delimiter and NoteDelimiter.
* A system password string may can contain any character except for Delimiter and SystemPassDelimiter.
* A date / timestamp has the format "dd/mm/yyyy hh:mm:ss", where the subfields are the given number of decimal digits.
* A drive name, folder name, access name and access password may consist of any character except for those listed below.  (Naturally, these values also need to be meaningful to Windows, so further constraints apply.  However, these are not enforced / mandated at the protocol level, leaving open the theoretical option of expressing network drive mount details for a non-Windows context.)
   * drive name - AccountDelimiter
   * folder name - TimeDelimiter
   * access name - OnsiteAssistDelimiter
   * access password - !Delimiter
   

Notes:

  i. I am assuming that the server-side follows the constraints listed above in the reply messages that it sends.
  i. My reading of the client code is that it does not check the constraints.  Users can type in login name strings, password strings and notes that contain excluded characters.  It is not clear what effect this will have on the server; e.g. whether it will misparse the messages (and possibly throw an exception) or notice the problem and return a CommandError response.


## Command and Response numbers

* LoginRequest (1) - LoginAllowedRespond (11), LoginRefusedRespond (12)
* LogoutRequest (2) - LogoutAllowedRespond(21), LogoutRefusedRespond (22)
* AccountChosenRequest (3) - AccountAllowedRespond (31), AccountRefusedRespond (32)
* NotesEnterRequest (4) - NotesAllowedRespond (41), NotesRefusedRespond (42)
* FacilityRequest (5) - FacilityAllowedRespond (51), FacilityRefusedRespond (52)
* ProjectRequest (6) - ProjectYesRespond (61), ProjectNoRespond (62)
* TimerCtrlRequest (7) - TimerYesRespond (71), TimerNoRespond (72)
* FacilityTypeRequest (8) - FacilityTypeRespond (81) (version 30.0)
* FacilityNumberRequest (9) - FacilityNumberRespond (91) (version 30.0)
* FacilityListRequest (10) - FacilityListRespond (101) (version 30.0)
* VirtualLoginRequest (11) - VirtualLoginAllowedRespond (111), VirtualLoginRefusedRespond (112)  (version 30.0)
* VirtualLogoutRequest (12) - VirtualLogoutAllowedRespond (121), VirtualLogoutRefusedRespond (122)  (version 30.0)
* VirtualAccountChosenRequest (13) - VirtualAccountAllowedRespond (131), VirtualAccountRefusedRespond (132)  (version 30.0)
* NewVirtualLoginRequest (14) - NewVirtualLoginAllowedRespond (141), !newVirtualLoginRefusedRespond (142)  (version 30.0)
* NewVirtualAccountChosenRequest (15) - NewVirtualAccountAllowedRespond (151), NewVirtualAccountRefusedRespond (152)  (version 30.0)
* SystemPassRequest (20) - SystemPassYesRespond (201), SystemPassNoRespond (202)
* StaffLoginRequest (21) - StaffLoginAllowedRespond (211), StaffLoginRefusedRespond (212)
* NetDriveRequest (22) - NetDriveYesRespond (221), NetDriveNoRespond (222)
* FullScreenRequest (23) - FullScreenYesRespond (231), FullScreenNoRespond (232)  (version 20.5.6)

There is an additional response number that is sent if the server is unhappy with the format of a request message.

* CommandError (0) (version 20.2)

Notes:

 i. I would guess that some of the numbers marked above as "version 30.0" were actually introduced in an earlier version of ACLS.
 i. The difference between VirtualLoginRequest and NewVirtualLoginRequest is that the first form is issued when a (previous) user is logged in.  Hence, the first form really means "log the old user out and the new user in".   *Confirm this.*.
 i. There seems to be no real difference between VirtualAccountChosenRequest and NewVirtualAccountChosenRequest.  *Confirm this.*.
 i. There is a "collision" between the "command" numbers for some requests and some responses.  For instance 11 means both LoginAllowedRespond and VirtualLoginRequest.  (This doesn't matter, but it suggests that the terminology should be changed to make it clear that request and response command numbers are conceptually different domains.)

## Response strings

The following strings are used in certain contexts in response messages:

* NoValidAccount ("No Valid Account")
* ValidCertificate ("Valid Certificate")
* NoCertificate ("No Certificate")
* ExpiredCertificate ("Expired Certificate")
* Yes string ("YES" or "Yes")
* No string ("No")

Notes:

  i. The Yes strings returned by ACLS appear to be all-caps in some situations and capitalized in others.

## Status messages

A variety of strings could sent by the server as the initial status line.  

  * AcceptedIPTag string (“IP Accepted”) means that the server is prepared to accept requests from the client's IP address.
  * UnAcceptedIPTag string ("Client IP Rejected: Please report to System Administrator") means that the server doesn't recognize the client's IP address.
  * ServerNotReadyTag string ("Logon Server Not Ready: Please report to System Administrator") means that the server is not in a state where it can accept requests.

Notes: 

  i. It is not entirely clear what (other than AcceptedIPTag) the server will actually return as the status message.  The client side code tests for AcceptedIPTag, and shows other responses to the user.  In theory, they could say anything.
  i. The client side only pays attention to the status line when it is about to start a sequence of requests, and even then not for some sequences.

# Commands

This section documents the individual commands as reverse engineered from the client-side source code provided by Dong Zheng.  For each command we describe the request and response messages and their respective codes and arguments.  Each response will be preceded by a status line as described above.

Notes:

  i. In general, it is not clear from the client-side code whether argument or result arguments can be empty strings.  It would therefore be prudent to assume that they _could be empty_, at least in the response messages.
  i. It is assumed that the server will send a CommandError response message if it is unhappy with the format of any request message.  The format is shown in the final subsection below.

## Login

In this exchange, the client tells the server that the person with the supplied user name and password wants to login.  The server checks to see if this is allowed.  If it is, then it responds giving a human friendly (I assume) names for the person and their organisation.  The response message also gives a list of accounts that the user may use, and information about the user's training certification.

	
	  LoginRequestMessage ::= LoginRequest CommandDelimiter 
	          user Delimiter password Delimiter
	
	  LoginResponseMessage ::= LoginAllowed | LoginRefused
	  LoginAllowed ::= LoginAllowedRespond CommandDelimiter LoginAllowedBody
	  LoginAllowedBody ::= userName Delimiter orgName Delimiter
	          AccountDelimiter AccountList Delimiter
	          CertificateDelimiter Certificate [ OnsiteAssistDelimiter ( Yes | other ) ] Delimiter
	  AccountList ::= ( NoValidAccount AccountSeparator ) | 
	                  ( account AccountSeparator { account AccountSeparator } )
	  Certificate ::= ValidCertificate | NoCertificate | ExpiredCertificate
	  LoginRefused ::= LoginRefusedRespond CommandDelimiter
	

Notes:

  i. The user name and password are sent in the clear over an unsecured TCP/IP connection.  
  i. The client side is responsible for deciding whether to allow someone with no current certificate (etc) to log in. 
  i. From the server's perspective, the user is only logged in once the AccountChosen exchange has completed.

## Virtual Login

There are two variations on the Login exchange in which the client also tells the server which virtual facility to log into.

	
	  VirtualLoginRequestMessage ::= VirtualLoginRequest CommandDelimiter 
	          user Delimiter password Delimiter
	          FacilityDelimiter subfacilityId Delimiter
	
	  VirtualLoginResponseMessage ::= VirtualLoginAllowed | VirtualLoginRefused
	  VirtualLoginAllowed ::= VirtualLoginAllowedRespond CommandDelimiter LoginAllowedBody
	  VirtualLoginRefused ::= VirtualLoginRefusedRespond CommandDelimiter
	

	
	  NewVirtualLoginRequestMessage ::= NewVirtualLoginRequest CommandDelimiter 
	          user Delimiter password Delimiter
	          FacilityDelimiter subfacilityId Delimiter
	
	  NewVirtualLoginResponseMessage ::= NewVirtualLoginAllowed | NewVirtualLoginRefused
	  NewVirtualLoginAllowed ::= NewVirtualLoginAllowedRespond CommandDelimiter LoginAllowedBody
	  NewVirtualLoginRefused ::= NewVirtualLoginRefusedRespond CommandDelimiter
	

Notes:

  i. The difference between VirtualLoginRequest and NewVirtualLoginRequest forms is that the first one is issued when a (previous) user is logged in.  Hence, it really means "log the old user out and the new user in".   *Confirm this.*.
  i. It is not clear if the onsite assist flag is included in a LoginAllowedBody for the virtual login cases.  *Confirm this.*  What is certain is that the "virtual facility" client doesn't extract the flag.

## Logout

In this exchange, the client tells the server that the user is logging out, or is being logged out by the application shutting down or the logout timer.

	
	  LogoutRequestMessage ::= LogoutRequest CommandDelimiter 
	          user Delimiter AccountDelimiter account Delimiter
	
	  LogoutResponseMessage ::= LogoutAllowed | LogoutRefused
	  LogoutAllowed ::= LogoutAllowedRespond CommandDelimiter
	  LogoutRefused ::= LogoutRefusedRespond CommandDelimiter
	

Notes:

  i. The server uses the logout request to "stop the meter" for accounting / billing purposes.

## Virtual Logout

This exchange is the "virtual facility" version of the logout exchange.

	
	  VirtualLogoutRequestMessage ::= VirtualLogoutRequest CommandDelimiter 
	          user Delimiter AccountDelimiter account Delimiter 
	          FacilityDelimiter subfacilityId Delimiter
	
	  VirtualLogoutResponseMessage ::= VirtualLogoutAllowed | VirtualLogoutRefused
	  VirtualLogoutAllowed ::= VirtualLogoutAllowedRespond CommandDelimiter
	  VirtualLogoutRefused ::= VirtualLogoutRefusedRespond CommandDelimiter
	

## Account Selection

In this exchange, the client tells the server which of user's accounts should be used.  Presumably the server uses this information to determine which account to record the usage against.  The response gives a login time that can be displayed.  

	
	  AccountChosenRequestMessage ::= AccountChosenRequest CommandDelimiter 
	          user Delimiter AccountDelimiter account Delimiter
	  
	  AccountChosenResponseMessage ::= AccountAllowed | AccountRefused
	  AccountAllowed ::= AccountAllowedRespond CommandDelimiter TimeDelimiter loginTimestamp Delimiter
	  AccountRefused ::= AccountRefusedRespond CommandDelimiter 
	

Notes:

  i. The purpose of the timestamp is to simply tell the user when he / she logged in.  The format of the timestamp is not specified.
  i. This exchange "turns on the meter" for the purposes of accounting and billing.

## Virtual Account Selection

These two exchanges are the "virtual facility" version of the account selection exchange.

	
	  VirtualAccountChosenRequestMessage ::= VirtualAccountChosenRequest CommandDelimiter 
	          user Delimiter AccountDelimiter account Delimiter 
	          FacilityDelimiter subfacilityId Delimiter
	  
	  VirtualAccountChosenResponseMessage ::= VirtualAccountAllowed | VirtualAccountRefused
	  VirtualAccountAllowed ::= VirtualAccountAllowedRespond CommandDelimiter TimeDelimiter loginTimestamp Delimiter
	  VirtualAccountRefused ::= VirtualAccountRefusedRespond CommandDelimiter 
	

	
	  NewVirtualAccountChosenRequestMessage ::= NewVirtualAccountChosenRequest CommandDelimiter 
	          user Delimiter AccountDelimiter account Delimiter 
	          FacilityDelimiter subfacilityId Delimiter
	  
	  NewVirtualAccountChosenResponseMessage ::= NewVirtualAccountAllowed | NewVirtualAccountRefused
	  NewVirtualAccountAllowed ::= NewVirtualAccountAllowedRespond CommandDelimiter TimeDelimiter loginTimestamp Delimiter
	  NewVirtualAccountRefused ::= NewVirtualAccountRefusedRespond CommandDelimiter 
	

Notes:

  i. There doesn't appear to be any semantic difference between the two forms of this request.  The distinction appears to simply to separate the states in the client-side state machine. *Confirm this.*.

## Note Creation

This exchange sends a "note" to the ACLS server where is is (presumably) recorded against the facility.

	
	  NotesEnterRequestMessage ::= NotesEnterRequest CommandDelimiter 
	          user Delimiter AccountDelimiter account Delimiter NoteDelimiter note Delimiter
	
	  NotesEnterReplyMessage ::= NotesAllowed | NotesRefused
	  NotesAllowed ::= NotesAllowedRespond CommandDelimiter
	  NotesRefused ::= NotesRefusedRespond CommandDelimiter
	

Notes:

  i. The "note" text is formed by replacing ASCII newline characters in the original notes text field with ';' characters.  Thus the user can't use ';' in the text.
  i. Since the text is terminated by the following ':' character, this can't be used in the notes text either.
  i. There isn't any way for the client to see notes created in previous sessions.

## Facility Query

In this exchange, the client server asks the server for the name of the facility; i.e. the instrument.

	
	  FacilityRequestMessage ::= FacilityRequest CommandDelimiter
	
	  FacilityResponseMessage ::= FacilityAllowed | FacilityRefused
	  FacilityAllowed ::= FacilityAllowedRespond CommandDelimiter FacilityDelimiter facilityName Delimiter
	  FacilityRefused ::= FacilityRefusedRespond CommandDelimiter
	

Notes:

  i. This exchange happens before the login exchange.  There is no additional server-side semantics.

## Project Query

In this exchange, the client server asks the server if accounts should be described as "Account" or "Project".

	
	  ProjectRequestMessage ::= ProjectRequest CommandDelimiter
	  
	  ProjectResponseMessage ::= ProjectYes | ProjectNo
	  ProjectYes ::= ProjectYesRespond CommandDelimiter
	  ProjectNo ::= ProjectNoRespond CommandDelimiter
	

## Timer Query

This exchange asks the server if the client should turn on a timer.  

	
	  TimerCtrlRequestMessage ::= TimerCtrlRequest CommandDelimiter
	
	  TimerCtryResponseMessage ::= TimerYes | TimerNo
	  TimerYes ::= TimerYesRespond CommandDelimiter.
	  TimerNo ::= TimerNoRespond CommandDelimiter.
	

Notes:

  i. The purpose of this timer is to implement a simple auto-logout facility which some installations use to deal with users who have forgotten to logout.  When the timer expires, the client automatically logs out the user.

## Facility Type Query

In this exchange, the client server asks the server for the type of the facility; i.e. whether the client IP address is registered with ACLS as a normal facility or a virtual facility with a collection of sub-facilities.

	
	  FacilityTypeRequestMessage ::= FacilityTypeRequest CommandDelimiter
	
	  FacilityTypeResponseMessage ::= FacilityTypeRespond CommandDelimiter FacilityDelimiter ( Yes | No ) Delimiter
	

Notes:

  i. A Yes response means that the client should configure itself as a virtual facility.  A No response means the client is a "real" facility. 
  i. This exchange happens during initialization.  There are no additional server-side semantics.

## Facility Number Query

In this exchange, the client asks the server how many sub-facilities there are.

	
	  FacilityNumberRequestMessage ::= FacilityNumberRequest CommandDelimiter
	
	  FacilityNumberResponseMessage ::= FacilityNumberRespond CommandDelimiter FacilityDelimiter number Delimiter
	

Notes:

  i. A response of zero means there are no sub-facilities. 
  i. This exchange happens during initialization.  There are no additional server-side semantics.
  i. The client-side code seems to assume that it only needs to refresh the sub-facility list if the number of facilities changes.  This is dubious.
  1. The client-side code has a hard-wired limit of 16 on the number of sub-facilities.  It is not clear if this limit exists on the server side.  *Check.*

## Facility List Query

In this exchange, the client asks the server for the list of sub-facility names.


	
	  FacilityListRequestMessage ::= FacilityListRequest CommandDelimiter
	
	  FacilityListResponseMessage ::= FacilityListRespond CommandDelimiter 
	          FacilityDelimiter { subfacilityId AccountSeparator } Delimiter
	

Notes:

  i. The AccountSeparator (not a typo) is actually used as a terminator.  Anything after the last AccountSeparator will be silently ignored by the version 30.0 client.

## System Password Query

This exchange requests a system password from the ACLS server.  

	
	  SystemPassRequestMessage ::= SystemPassRequest CommandDelimiter
	
	  SystemPassResponseMessage ::= SystemPassYes | SystemPassNo
	  SystemPassYes ::= SystemPassYesRespond CommandDelimiter systemPassword Delimiter
	  SystemPassNo ::= SystemPassNoRespond CommandDelimiter
	

Notes:

  i. The system password provides a way to unlock the screen without logging in.  A per-facility password is created using a random number generator on the server, and the server provides a UI that allows lab managers to lookup the password.
  i. The system password gets saved in a file on the facility in clear text.
  i. An alternative approach is to use F8 to unlock the screen.  Apparently, lab managers find the system password approach inconvenient to use.

## Staff Login

This exchange requests permission for staff login in the Training login process.

	
	  StaffLoginRequestMessage ::= StaffLoginRequest CommandDelimiter 
	          name Delimiter password Delimiter
	
	  StaffLoginResponseMessage ::= StaffLoginAllowed | StaffLoginRefused
	  StaffLoginAllowed ::= StaffLoginAllowedRespond CommandDelimiter
	  StaffLoginRefused ::= StaffLoginRefusedRespond CommandDelimiter
	

Notes:

  i. This is not really a staff login as such.  Rather it is a staff member authorizing an untrained user to login.
  i. As with the LoginRequest, the client is responsible for the ultimate decision making, and the server only recognizes the user's session as having started when the AccountChosen exchange has completed.

## Net Drive Query

This exchange requests details of a shared drive to be mounted and used by the facility.

	
	   NetDriveRequestMessage ::= NetDriveRequest CommandDelimiter
	
	   NetDriveResponseMessage ::= NetDriveYes | NetDriveNo
	   NetDriveYes ::= NetDriveYesRespond CommandDelimiter driveName 
	         AccountDelimiter folderName TimeDelimiter accessName OnsiteAssistDelimiter accessPassword Delimiter
	   NetDriveNo ::= NetDriveNoRespond CommandDelimiter
	

Notes:

  i. The response message _really is_ reusing "delimiter" characters from the LoginAcceptRespond message.  It is not a typo in this document.
  i. It appears as if this response could return no information.
  i. If the driveName is empty, the client makes no attempt to mount a shared drive.
  i. Dong mentioned that a future version of ACLS will be able to return different net drive information on a per-user basis, allowing the facility to mount the user's home drive.  This will require a change to this message, I believe.

## Command Error

A CommandError response is sent by the server if it is unhappy with the format of a request message; e.g. if it doesn't recognize the command number or if required arguments are or delimiters are missing.

	
	   CommandErrorResponseMessage ::= CommandError CommandDelimiter
	

Notes:

  i. The stated purpose of the message is to fend off hacker attempts and to deal with transport-level data corruption.  It also serves to deal with non-conformant client-side implementations in some cases.
  i. A CommandErrorResponseMessage contains no information about the actual problem that triggered the error.  The problem is reported to the user as "Server Response Error #1".

# Command Sequences

The following subsections describe the main ACLS client/server interaction sequences from the perspective of the client.

Notes:

  i. There is an implied state machine controlling this except that it relies on a message sequence that cannot definitively discerned from looking at just the client code.  Indeed, the client does not check that it receives the expected response message type for the state that it currently is in.  If the client and server were to somehow get out of sync, the results would be unpredictable.)
  i. In fact, it is possible that the server might send a CommandError message in response to any unacceptable request.  The client responds to this by simply displaying an error message.
  i. Many of the requests have the option of a "refuse".  The client normally handles unanticipated "refuse" responses by displaying an error message.  (The login and staff login responses are an exception.  These cause the client displays an explicit message saying that the username / password are incorrect, and then returns the client to the "waiting for login" state.)

## Initialization

In the "normal facility" client (version 20.5.9), the sequence is as follows:

  1. The application connects to the server to check the status.
  1. It sends a ProjectRequestMessage to the server, and adjusts the UI accordingly.
  1. It sends a SystemPasswordMessage to ask for the system password.  The password, if available is saved in a file.
  1. It sends a FacilityRequestMessage to obtain the name of the current facility (instrument) from the ACLS server and displays it in the UI.

In the "virtual facility" client (version 30.0), the sequence is as follows:

  1. The application connects to the server to check the status.
  1. It sends a FacilityTypeMessage to the server, and goes into a failed state if it returns "No".
  1. It sends a FacilityNumberMessage to the server, and goes into a failed state if it returns zero.
  1. It sends a FacilityListMessage to the server and uses the resulting sub-facility names to populate the UI.

Notes:

  i. The "virtual facility" client will also refresh the sub-facility menu under certain circumstances.

## Logging in

The application responds to the initial "login" event as follows:

  1. It sends a FacilityRequestMessage to obtain the name of the current facility (instrument) from the ACLS server and displays it in the UI.
  2. It sends a LoginRequestMessage containing the user name and password.
  3. If the response is a LoginRefusedMessage, then login is refused for some unstated reason, a message is displayed and the application goes back to the initial state.
  4. If the response is LoginAllowedMessage, then:
     a. If the onsite assist flag is Yes then on-site assistance is required, and we switch to the Trainer login procedure.
     b. If the account list has one entry that is equal to NoValidAccount, login is refused.
     c. If the certificate is NoCertificate or ExpiredCertificate, login is refused.  (The messages say that the "training certificate" is unavailable or has expired.)
     d. Otherwise proceed to the next step.
  5. The account / project name or names are extracted from the previous response.  (A configuration switch determines whether this is described to the user as an "account" or a "project".)
     a. If there is only one account, this will becomes the selected account.
     b. If there are multiple accounts, the list is used to populate the account selection form and the user selects the account.
  6. An AccountChosenRequestMessage is sent giving the selected account name.  The response gives a login time that is displayed in the UI.  In some configurations, the response to this message is what causes the screen to unlock.
  7. A TimerCtrlRequestMessage is sent to start the session / logout timer.  (I can't figure out what the timer does, but the response message is apparently a simple yes / no, so any knowledge of session session lengths must be hard-wired ... assuming that's what this is all about.)
  8. A NetDriveRequestMessage is sent to get the details of the shared drive to be mounted.  If the request succeeds, the application attempts to mount the shared drive using the drive name, folder name, account and password returned in the response.

Notes:

  i. The "virtual facility" code is similar except that the onsite assist flag is ignored.

## Trainer login

If the "Logging in" sequence gets to the point where it sees Yes in the onsite assist flag, it launches a staff login form to request a staff userName and password.  When these have been provided, it does the following:

  1. It sends a StaffLoginRequestMessage containing the staff username and password.
  2. If the response is StaffLoginRefused, the procedure exits.  (Start again with the user login ...)
  3. If the response is StaffLoginAllowed, then the account list from the earlier LoginAllowedMessage is used as follows:
    a. If the first account is NoValidAccount, then the login fails.
    b. If there is only one account, that is used below.
    c. If there are multiple accounts, the list is used to populate the account selection form and the user selects the account.
  4. An AccountChosenRequestMessage is sent giving the selected account name and the current user name (not the staff user name!).
  5. A TimerCtrlRequestMessage and a NetDriveRequestMessage are sent as processed as above.

If this procedure completes, the screen will be unlocked so that the user and trainer can use the facility.

Notes:

  i. Trainer / staff login is not implemented in the "virtual facility" client codebase.

## Logging out

This sequence occurs when the user clicks the Logout button, the logout timer goes off, or the application / facility is shut down or restarted:

  1. The application sends a LogoutRequestMessage containing the current username and account.
  1. If the server responds with a LogoutAccepted message, then the application disconnects the shared drive (if an attempt was made to connect it when the user logged in).  Then it clocks the screen and returns the system to the "waiting for login" state.


## Creating a note

This sequence occurs when the user clicks the Note button:

  1.  The application captures the note text from the form and processes it to replace embedded newlines with ';' characters.
  2.  It sends a NotesEnterRequestMessage to the server containing the user name and account, and the note text.
  3.  If the response is NotesEnterAllowed, then the note has been recorded.  Otherwise it has not.

Notes:

  i. Note creation is not supported by the "virtual facility" codebase, and the message structure doesn't support it.

  