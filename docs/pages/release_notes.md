---
title: PMD Release Notes
permalink: pmd_release_notes.html
keywords: changelog, release notes
---

## ????? - 6.1.0-SNAPSHOT

The PMD team is pleased to announce PMD 6.1.0.

This is a minor release.

### Table Of Contents

* [New and noteworthy](#new-and-noteworthy)
* [Fixed Issues](#fixed-issues)
* [API Changes](#api-changes)
* [External Contributions](#external-contributions)

### New and noteworthy

### Fixed Issues

*   all
    *   [#848](https://github.com/pmd/pmd/issues/848): \[doc] Test failures when building pmd-doc under Windows
    *   [#569](https://github.com/pmd/pmd/issues/569): \[core] XPath support requires specific toString implementations
*   doc
    *   [#791](https://github.com/pmd/pmd/issues/791): \[doc] Documentation site reorganisation

### API Changes

#### Changes to the Node interface

The method `getXPathNodeName` is added to the `Node` interface, which removes the
use of the `toString` of a node to get its XPath element name (see [#569](https://github.com/pmd/pmd/issues/569)).
A default implementation is provided in `AbstractNode`, to stay compatible
with existing implementors.

The `toString` method of a Node is not changed for the time being, and still produces
the name of the XPath node. That behaviour may however change in future major releases,
e.g. to produce a more useful message for debugging.

### External Contributions


*   [#790](https://github.com/pmd/pmd/pull/790): \[java] Added some comments for JDK 9 - [Tobias Weimer](https://github.com/tweimer)
*   [#803](https://github.com/pmd/pmd/pull/803): \[doc] Added SpotBugs as successor of FindBugs - [Tobias Weimer](https://github.com/tweimer)
*   [#830](https://github.com/pmd/pmd/pull/830): \[java] UseArraysAsList: Description added - [Tobias Weimer](https://github.com/tweimer)
*   [#845](https://github.com/pmd/pmd/pull/845): \[java] Fix false negative PreserveStackTrace on string concatenation - [Alberto Fernández](https://github.com/albfernandez)

