/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.vf;

import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersionHandler;
import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ast.Node;

/**
 * @author sergey.gorbaty
 *
 */
public class VfParserTest {

    @Test
    public void testSingleDoubleQuoteAndEL() {
        Node node = parse("<span escape='false' attrib=\"{!call}\">${!yes}</span>");
        Assert.assertNotNull(node);
    }

    @Test
    public void testSingleDoubleQuote() {
        Node node = parse("<span escape='false' attrib=\"{!call}\">${yes}</span>");
        Assert.assertNotNull(node);
    }

    private Node parse(String code) {
        LanguageVersionHandler jspLang = LanguageRegistry.getLanguage(VfLanguageModule.NAME).getDefaultVersion()
                .getLanguageVersionHandler();
        Parser parser = jspLang.getParser(jspLang.getDefaultParserOptions());
        Node node = parser.parse(null, new StringReader(code));
        return node;
    }
}
