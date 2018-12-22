---
title: PMD Release Notes
permalink: pmd_release_notes.html
keywords: changelog, release notes
---

## {{ site.pmd.date }} - {{ site.pmd.version }}

The PMD team is pleased to announce PMD {{ site.pmd.version }}.

This is a {{ site.pmd.release_type }} release.

{% tocmaker is_release_notes_processor %}

### New and noteworthy

#### New Rules

*   The new Java rule {% rule "java/bestpractices/ForLoopVariableCount" %} (`java-bestpractices`) checks for
    the number of control variables in a for-loop. Having a lot of control variables makes it harder to understand
    what the loop does. The maximum allowed number of variables is by default 1 and can be configured by a
    property.

*   The new Java rule {% rule "java/bestpractices/AvoidReassigningLoopVariables" %} (`java-bestpractices`) searches
    for loop variables that are reassigned. Changing the loop variables additionally to the loop itself can lead to
    hard-to-find bugs.

#### Modified Rules

*   The Java rule {% rule "java/codestyle/LocalVariableCouldBeFinal" %} (`java-codestyle`) has a new
    property `ignoreForEachDecl`, which is by default disabled. The new property allows for ignoring
    non-final loop variables in a for-each statement.

### Fixed Issues

*   java-bestpractices
    *   [#658](https://github.com/pmd/pmd/issues/658): \[java] OneDeclarationPerLine: False positive for loops
    *   [#1518](https://github.com/pmd/pmd/issues/1518): \[java] New rule: AvoidReassigningLoopVariable
    *   [#1519](https://github.com/pmd/pmd/issues/1519): \[java] New rule: ForLoopVariableCount
*   java-codestyle
    *   [#1513](https://github.com/pmd/pmd/issues/1513): \[java] LocalVariableCouldBeFinal: allow excluding the variable in a for-each loop
*   java-errorprone
    *   [#1035](https://github.com/pmd/pmd/issues/1035): \[java] ReturnFromFinallyBlock: False positive on lambda expression in finally block
*   plsql
    *   [#1508](https://github.com/pmd/pmd/issues/1508): \[plsql] Parse Exception when using SELECT COUNT(*)
    *   [#1509](https://github.com/pmd/pmd/issues/1509): \[plsql] Parse Exception with OUTER/INNER Joins
    *   [#1511](https://github.com/pmd/pmd/issues/1511): \[plsql] Parse Exception with IS NOT NULL

### API Changes

### External Contributions

*   [#1503](https://github.com/pmd/pmd/pull/1503): \[java] Fix for ReturnFromFinallyBlock false-positives - [RishabhDeep Singh](https://github.com/rishabhdeepsingh)
*   [#1514](https://github.com/pmd/pmd/pull/1514): \[java] LocalVariableCouldBeFinal: allow excluding the variable in a for-each loop - [Kris Scheibe](https://github.com/kris-scheibe)
*   [#1516](https://github.com/pmd/pmd/pull/1516): \[java] OneDeclarationPerLine: Don't report multiple variables in a for statement. - [Kris Scheibe](https://github.com/kris-scheibe)
*   [#1520](https://github.com/pmd/pmd/pull/1520): \[java] New rule: ForLoopVariableCount: check the number of control variables in a for loop - [Kris Scheibe](https://github.com/kris-scheibe)
*   [#1521](https://github.com/pmd/pmd/pull/1521): \[java] Upgrade to ASM7 for JDK 11 support - [Mark Pritchard](https://github.com/markpritchard)
*   [#1530](https://github.com/pmd/pmd/pull/1530): \[java] New rule: AvoidReassigningLoopVariables - [Kris Scheibe](https://github.com/kris-scheibe)

{% endtocmaker %}

