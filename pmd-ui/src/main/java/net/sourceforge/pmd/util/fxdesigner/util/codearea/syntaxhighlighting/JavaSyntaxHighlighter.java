/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.fxdesigner.util.codearea.syntaxhighlighting;

import java.util.Map;
import java.util.regex.Pattern;

import net.sourceforge.pmd.util.fxdesigner.util.codearea.SimpleRegexSyntaxHighlighter;
import net.sourceforge.pmd.util.fxdesigner.util.codearea.SyntaxHighlighter;

/**
 * Syntax highlighter for Java.
 *
 * @author Clément Fournier
 * @since 6.0.0
 */
public class JavaSyntaxHighlighter extends SimpleRegexSyntaxHighlighter {


    private static final String[] KEYWORDS = new String[] {
        "abstract", "assert", "boolean", "break", "byte",
        "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else",
        "enum", "extends", "final", "finally", "float",
        "for", "goto", "if", "implements", "import",
        "instanceof", "int", "interface", "long", "native",
        "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super",
        "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "void", "volatile", "while",
        };


    public static final SyntaxHighlighter INSTANCE
        = builder("java",
                  "single-line-comment", "//[^\n]*")
        .or("multi-line-comment", "/\\*.*?\\*/")
        .or("annotation", "@[\\w]+")
        .or("paren", "[()]")
        .or("number", "\\b\\d+[fdlFDL]*\\b")
        .or("brace", "[{}]")
        .or("bracket", "[\\[]]")
        .or("semicolon", ";")
        .or("keyword", "\\b(" + String.join("|", KEYWORDS) + ")\\b")
        .or("class-ident", "\\b[A-Z][\\w_$]*\\b")
        .or("string", "\"([^\"\\\\]|\\\\.)*\"")
        .create(Pattern.DOTALL);


    private JavaSyntaxHighlighter(String languageName, Pattern pattern, Map<String, String> namesToCssClass) {
        super(languageName, pattern, namesToCssClass);
    }

}
