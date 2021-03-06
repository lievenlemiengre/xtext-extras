/*
 * generated by Xtext
 */
package org.eclipse.xtext.common.types.xtext.ui.services;

import com.google.inject.Singleton;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.xtext.*;
import org.eclipse.xtext.service.GrammarProvider;
import org.eclipse.xtext.service.AbstractElementFinder.*;

import org.eclipse.xtext.common.types.xtext.ui.services.RefactoringTestLanguageGrammarAccess;
import org.eclipse.xtext.common.services.TerminalsGrammarAccess;

@Singleton
public class RefactoringTestLanguage1GrammarAccess extends AbstractGrammarElementFinder {
	
	
	public class EntryElements extends AbstractParserRuleElementFinder {
		private final ParserRule rule = (ParserRule) GrammarUtil.findRuleForName(getGrammar(), "org.eclipse.xtext.common.types.xtext.ui.RefactoringTestLanguage1.Entry");
		private final RuleCall cModelParserRuleCall = (RuleCall)rule.eContents().get(1);
		
		//Entry Model:
		//	Model
		@Override public ParserRule getRule() { return rule; }

		//Model
		public RuleCall getModelParserRuleCall() { return cModelParserRuleCall; }
	}
	
	
	private final EntryElements pEntry;
	
	private final Grammar grammar;

	private final RefactoringTestLanguageGrammarAccess gaRefactoringTestLanguage;

	private final TerminalsGrammarAccess gaTerminals;

	@Inject
	public RefactoringTestLanguage1GrammarAccess(GrammarProvider grammarProvider,
		RefactoringTestLanguageGrammarAccess gaRefactoringTestLanguage,
		TerminalsGrammarAccess gaTerminals) {
		this.grammar = internalFindGrammar(grammarProvider);
		this.gaRefactoringTestLanguage = gaRefactoringTestLanguage;
		this.gaTerminals = gaTerminals;
		this.pEntry = new EntryElements();
	}
	
	protected Grammar internalFindGrammar(GrammarProvider grammarProvider) {
		Grammar grammar = grammarProvider.getGrammar(this);
		while (grammar != null) {
			if ("org.eclipse.xtext.common.types.xtext.ui.RefactoringTestLanguage1".equals(grammar.getName())) {
				return grammar;
			}
			List<Grammar> grammars = grammar.getUsedGrammars();
			if (!grammars.isEmpty()) {
				grammar = grammars.iterator().next();
			} else {
				return null;
			}
		}
		return grammar;
	}
	
	@Override
	public Grammar getGrammar() {
		return grammar;
	}
	

	public RefactoringTestLanguageGrammarAccess getRefactoringTestLanguageGrammarAccess() {
		return gaRefactoringTestLanguage;
	}

	public TerminalsGrammarAccess getTerminalsGrammarAccess() {
		return gaTerminals;
	}

	
	//Entry Model:
	//	Model
	public EntryElements getEntryAccess() {
		return pEntry;
	}
	
	public ParserRule getEntryRule() {
		return getEntryAccess().getRule();
	}

	//Model:
	//	referenceHolder+=ReferenceHolder*;
	public RefactoringTestLanguageGrammarAccess.ModelElements getModelAccess() {
		return gaRefactoringTestLanguage.getModelAccess();
	}
	
	public ParserRule getModelRule() {
		return getModelAccess().getRule();
	}

	//ReferenceHolder:
	//	name=ID defaultReference=[types::JvmType|FQN];
	public RefactoringTestLanguageGrammarAccess.ReferenceHolderElements getReferenceHolderAccess() {
		return gaRefactoringTestLanguage.getReferenceHolderAccess();
	}
	
	public ParserRule getReferenceHolderRule() {
		return getReferenceHolderAccess().getRule();
	}

	//FQN:
	//	ID ('.' ID)* ('$' ID)*;
	public RefactoringTestLanguageGrammarAccess.FQNElements getFQNAccess() {
		return gaRefactoringTestLanguage.getFQNAccess();
	}
	
	public ParserRule getFQNRule() {
		return getFQNAccess().getRule();
	}

	//terminal ID:
	//	'^'? ('a'..'z' | 'A'..'Z' | '_') ('a'..'z' | 'A'..'Z' | '_' | '0'..'9')*;
	public TerminalRule getIDRule() {
		return gaTerminals.getIDRule();
	} 

	//terminal INT returns ecore::EInt:
	//	'0'..'9'+;
	public TerminalRule getINTRule() {
		return gaTerminals.getINTRule();
	} 

	//terminal STRING:
	//	'"' ('\\' . | !('\\' | '"'))* '"' |
	//	"'" ('\\' . | !('\\' | "'"))* "'";
	public TerminalRule getSTRINGRule() {
		return gaTerminals.getSTRINGRule();
	} 

	//terminal ML_COMMENT:
	//	'/ *'->'* /';
	public TerminalRule getML_COMMENTRule() {
		return gaTerminals.getML_COMMENTRule();
	} 

	//terminal SL_COMMENT:
	//	'//' !('\n' | '\r')* ('\r'? '\n')?;
	public TerminalRule getSL_COMMENTRule() {
		return gaTerminals.getSL_COMMENTRule();
	} 

	//terminal WS:
	//	' ' | '\t' | '\r' | '\n'+;
	public TerminalRule getWSRule() {
		return gaTerminals.getWSRule();
	} 

	//terminal ANY_OTHER:
	//	.;
	public TerminalRule getANY_OTHERRule() {
		return gaTerminals.getANY_OTHERRule();
	} 
}
