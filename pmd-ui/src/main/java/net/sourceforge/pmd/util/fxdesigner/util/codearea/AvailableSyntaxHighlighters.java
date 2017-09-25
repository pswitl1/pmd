/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.fxdesigner.util.codearea;

import java.util.Arrays;
import java.util.Optional;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.util.fxdesigner.util.codearea.syntaxhighlighting.ApexSyntaxHighlighter;
import net.sourceforge.pmd.util.fxdesigner.util.codearea.syntaxhighlighting.JavaSyntaxHighlighter;
import net.sourceforge.pmd.util.fxdesigner.util.codearea.syntaxhighlighting.XPathSyntaxHighlighter;
import net.sourceforge.pmd.util.fxdesigner.util.codearea.syntaxhighlighting.XmlSyntaxHighlighter;

/**
 * Lists the available syntax highlighter engines by language.
 *
 * @author Clément Fournier
 * @since 6.0.0
 */
public enum AvailableSyntaxHighlighters {
    JAVA("java", JavaSyntaxHighlighter.INSTANCE),
    APEX("apex", ApexSyntaxHighlighter.INSTANCE),
    XML("xml", XmlSyntaxHighlighter.INSTANCE),
    XPATH("xpath", XPathSyntaxHighlighter.INSTANCE);


    private final String language;
    private final SyntaxHighlighter computer;


    AvailableSyntaxHighlighters(String languageTerseName, SyntaxHighlighter computer) {
        this.language = languageTerseName;
        this.computer = computer;
    }


    /**
     * Gets the highlighter for a language if available, otherwise returns null.
     *
     * @param language Language to look for
     *
     * @return A highlighter if available, otherwise null
     */
    public static SyntaxHighlighter getComputerForLanguage(Language language) {
        Optional<AvailableSyntaxHighlighters> found = Arrays.stream(AvailableSyntaxHighlighters.values())
                                                            .filter(e -> e.language.equals(language.getTerseName()))
                                                            .findFirst();
        if (found.isPresent()) {
            return found.get().computer;
        } else {
            return null;
        }

    }
}
