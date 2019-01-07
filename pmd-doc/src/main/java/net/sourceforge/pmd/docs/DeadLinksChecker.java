/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.docs;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;


/**
 * Checks links to local pages for non-existing link-targets.
 */
public class DeadLinksChecker {
    private static final Logger LOG = Logger.getLogger(DeadLinksChecker.class.getName());

    private static final String CHECK_EXTERNAL_LINKS_PROPERTY = "pmd.doc.checkExternalLinks";
    private static final boolean CHECK_EXTERNAL_LINKS = Boolean.parseBoolean(System.getProperty(CHECK_EXTERNAL_LINKS_PROPERTY));

    // Markdown-Link: something in []'s followed by something in ()'s
    private static final Pattern LOCAL_LINK_PATTERN = Pattern.compile("\\[.*?\\]\\((.*?)\\)");

    // Markdown permalink-header and captions
    private static final Pattern MD_HEADER_PERMALINK = Pattern.compile("permalink:\\s*(.*)");
    private static final Pattern MD_CAPTION = Pattern.compile("^##+\\s+(.*)$", Pattern.MULTILINE);

    // list of link targets, where the link detection doesn't work
    private static final Pattern EXCLUDED_LINK_TARGETS = Pattern.compile(
        "^pmd_userdocs_cli_reference\\.html.*" // anchors in the CLI reference are a plain HTML include
    );

    // the link is actually pointing to a file in the pmd project
    private static final String LOCAL_FILE_PREFIX = "https://github.com/pmd/pmd/blob/master/";

    // don't check links to PMD bugs/issues/pull-requests  (performance optimization)
    private static final List<String> IGNORED_URL_PREFIXES = Collections.unmodifiableList(Arrays.asList(
        "https://github.com/pmd/pmd/issues/",
        "https://github.com/pmd/pmd/pull/",
        "https://sourceforge.net/p/pmd/bugs/"
    ));

    // prevent checking the same link multiple times
    private final Map<String, Integer> linkResultCache = new ConcurrentHashMap<>();

    private final ExecutorService executorService = Executors.newCachedThreadPool();


    public void checkDeadLinks(Path rootDirectory) {
        final Path pagesDirectory = rootDirectory.resolve("docs/pages");

        if (!Files.isDirectory(pagesDirectory)) {
            LOG.warning("can't check for dead links, didn't find \"pages\" directory at: " + pagesDirectory);
            System.exit(1);
        }

        // read all .md-files in the pages directory
        final List<Path> mdFiles = listMdFiles(pagesDirectory);

        // Stores file path to the future deadlinks. If a future evaluates to null, the link is not dead
        final Map<Path, List<Future<String>>> fileToDeadLinks = new HashMap<>();
        // make a list of all valid link targets
        final Set<String> htmlPages = extractLinkTargets(mdFiles);

        // scan all .md-files for dead local links
        int scannedFiles = 0;
        int foundExternalLinks = 0;
        int checkedExternalLinks = 0;

        for (Path mdFile : mdFiles) {
            final String pageContent = fileToString(mdFile);
            scannedFiles++;

            // iterate line-by-line for better reporting the line numbers
            final String[] lines = pageContent.split("\r?\n|\n");
            for (int index = 0; index < lines.length; index++) {
                final String line = lines[index];
                final int lineNo = index + 1;

                final Matcher matcher = LOCAL_LINK_PATTERN.matcher(line);
                linkCheck:
                while (matcher.find()) {
                    final String linkText = matcher.group();
                    final String linkTarget = matcher.group(1).replaceAll("^/+", ""); // remove the leading "/"
                    boolean linkOk;

                    if (linkTarget.startsWith(LOCAL_FILE_PREFIX)) {
                        String localLinkPart = linkTarget.substring(LOCAL_FILE_PREFIX.length());
                        if (localLinkPart.contains("#")) {
                            localLinkPart = localLinkPart.substring(0, localLinkPart.indexOf('#'));
                        }

                        final Path localFile = rootDirectory.resolve(localLinkPart);
                        linkOk = Files.isRegularFile(localFile);
                        if (!linkOk) {
                            LOG.warning("local file not found: " + localFile);
                            LOG.warning("  linked by: " + linkTarget);
                        }

                    } else if (linkTarget.startsWith("http://") || linkTarget.startsWith("https://")) {
                        foundExternalLinks++;

                        if (!CHECK_EXTERNAL_LINKS) {
                            LOG.finer("ignoring check of external url: " + linkTarget);
                            continue;
                        }

                        for (String ignoredUrlPrefix : IGNORED_URL_PREFIXES) {
                            if (linkTarget.startsWith(ignoredUrlPrefix)) {
                                LOG.finer("not checking link: " + linkTarget);
                                continue linkCheck;
                            }
                        }

                        checkedExternalLinks++;
                        linkOk = true;

                        // process in parallel
                        // It's important not to use the matcher in the callable!
                        // It may be exhausted at the time of execution
                        addDeadLink(fileToDeadLinks, mdFile, executorService.submit(() -> externalLinkIsDead(linkTarget)
                                                                         ? String.format("%8d: %s", lineNo, linkText)
                                                                         : null));

                    } else {
                        // ignore local anchors
                        if (linkTarget.startsWith("#")) {
                            continue;
                        }

                        // ignore some pages where automatic link detection doesn't work
                        if (EXCLUDED_LINK_TARGETS.matcher(linkTarget).matches()) {
                            continue;
                        }

                        linkOk = linkTarget.isEmpty() || htmlPages.contains(linkTarget);
                    }

                    if (!linkOk) {
                        addDeadLink(fileToDeadLinks, mdFile, new FutureTask<>(() -> String.format("%8d: %s", lineNo, linkText)));
                    }
                }
            }
        }

        executorService.shutdown();

        // all appenders have been pushed

        System.out.println("Scanned " + scannedFiles + " files for dead links.");
        System.out.println("  Found " + foundExternalLinks + " external links, " + checkedExternalLinks + " of those where checked.");

        if (!CHECK_EXTERNAL_LINKS) {
            System.out.println("External links weren't checked, set -D" + CHECK_EXTERNAL_LINKS_PROPERTY + "=true to enable it.");
        }

        Map<Path, List<String>> joined = joinFutures(fileToDeadLinks);

        if (joined.isEmpty()) {
            System.out.println("No errors found!");
        } else {
            System.err.println("Found dead link(s):");
            for (Path file : joined.keySet()) {
                System.err.println(rootDirectory.relativize(file).toString());
                joined.get(file).forEach(System.err::println);
            }
            throw new AssertionError("Dead links detected");
        }
    }


    private Map<Path, List<String>> joinFutures(Map<Path, List<Future<String>>> map) {
        Map<Path, List<String>> joined = new HashMap<>();

        for (Path p : map.keySet()) {

            List<String> evaluatedResult = map.get(p).stream()
                                              .map(f -> {
                                                  try {
                                                      return f.get();
                                                  } catch (InterruptedException | ExecutionException e) {
                                                      e.printStackTrace();
                                                      return null;
                                                  }
                                              })
                                              .filter(Objects::nonNull)
                                              .sorted(Comparator.naturalOrder())
                                              .collect(Collectors.toList());

            if (!evaluatedResult.isEmpty()) {
                joined.put(p, evaluatedResult);
            }
        }
        return joined;
    }


    private void addDeadLink(Map<Path, List<Future<String>>> fileToDeadLinks, Path file, Future<String> line) {
        fileToDeadLinks.computeIfAbsent(file, k -> new ArrayList<>()).add(line);
    }


    private Set<String> extractLinkTargets(List<Path> mdFiles) {
        final Set<String> htmlPages = new HashSet<>();
        for (Path mdFile : mdFiles) {
            final String pageContent = fileToString(mdFile);

            // extract the permalink header field
            final Matcher permalinkMatcher = MD_HEADER_PERMALINK.matcher(pageContent);
            if (!permalinkMatcher.find()) {
                continue;
            }

            final String pageUrl = permalinkMatcher.group(1)
                                                   .replaceAll("^/+", ""); // remove the leading "/"

            // add the root page
            htmlPages.add(pageUrl);

            // add all captions as anchors
            final Matcher captionMatcher = MD_CAPTION.matcher(pageContent);
            while (captionMatcher.find()) {
                final String anchor = captionMatcher.group(1)
                                                    .toLowerCase(Locale.ROOT)
                                                    .replaceAll("[^a-z0-9_]+", "-") // replace all non-alphanumeric characters with dashes
                                                    .replaceAll("^-+|-+$", ""); // trim leading or trailing dashes

                htmlPages.add(pageUrl + "#" + anchor);
            }
        }
        return htmlPages;
    }


    private List<Path> listMdFiles(Path pagesDirectory) {
        final List<Path> mdFiles = new ArrayList<>();
        try {
            Files.walk(pagesDirectory)
                 .filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".md"))
                 .forEach(mdFiles::add);
        } catch (IOException ex) {
            throw new RuntimeException("error listing files in " + pagesDirectory, ex);
        }
        return mdFiles;
    }


    private String fileToString(Path mdFile) {
        try (InputStream inputStream = Files.newInputStream(mdFile)) {
            return IOUtils.toString(inputStream, Charset.forName("UTF-8"));
        } catch (IOException ex) {
            throw new RuntimeException("error reading " + mdFile, ex);
        }
    }


    private boolean externalLinkIsDead(String url) {
        LOG.fine("checking url: " + url + " ...");
        if (linkResultCache.containsKey(url)) {
            LOG.fine("response: HTTP " + linkResultCache.get(url) + " (CACHED) on " + url);
            return linkResultCache.get(url) >= 400;
        }

        try {
            final HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(15000);
            httpURLConnection.connect();
            final int responseCode = httpURLConnection.getResponseCode();

            String response = "HTTP " + responseCode;
            if (httpURLConnection.getHeaderField("Location") != null) {
                response += ", Location: " + httpURLConnection.getHeaderField("Location");
            }

            LOG.fine("response: " + response + " on " + url);
            linkResultCache.put(url, responseCode);

            // success (HTTP 2xx) or redirection (HTTP 3xx)
            return responseCode >= 400;

        } catch (IOException ex) {
            LOG.fine("response: " + ex.getClass().getName() + " on " + url + " : " + ex.getMessage());
            linkResultCache.put(url, 599);
            return true;
        }
    }


    public static void main(String[] args) throws IOException {
        final Path rootDirectory = Paths.get(args[0]).resolve("..").toRealPath();

        DeadLinksChecker deadLinksChecker = new DeadLinksChecker();
        deadLinksChecker.checkDeadLinks(rootDirectory);
    }



}
