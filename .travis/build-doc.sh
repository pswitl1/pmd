#!/bin/bash
set -e

source .travis/common-functions.sh
source .travis/logger.sh

VERSION=$(./mvnw -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.5.0:exec | tail -1)
log_info "Building PMD Documentation ${VERSION} on branch ${TRAVIS_BRANCH}"

if ! travis_isPush; then
    log_info "Not building site, since this is not a push!"
    exit 0
fi

pushd docs

# run jekyll
echo -e "\n\n"
log_info "Building documentation using jekyll..."
bundle install
bundle exec jekyll build

# create pmd-doc archive
echo -e "\n\n"
log_info "Creating pmd-doc archive..."
mv _site pmd-doc-${VERSION}
zip -qr pmd-doc-${VERSION}.zip pmd-doc-${VERSION}/

(
    # disable fast fail, exit immediately, in this subshell
    set +e

    if [[ "${TRAVIS_TAG}" != "" || "${VERSION}" == *-SNAPSHOT ]]; then
        echo -e "\n\n"
        log_info "Uploading pmd doc distribution to sourceforge..."
        rsync -avh pmd-doc-${VERSION}.zip ${PMD_SF_USER}@web.sourceforge.net:/home/frs/project/pmd/pmd/${VERSION}/
        if [ $? -ne 0 ]; then
            log_error "Couldn't upload pmd-doc-${VERSION}.zip!"
            log_error "Please upload manually: https://sourceforge.net/projects/pmd/files/pmd/"
        fi
    fi

    # rsync site to pmd.sourceforge.net/snapshot
    if [[ "${VERSION}" == *-SNAPSHOT && "${TRAVIS_BRANCH}" == "master" ]]; then
        echo -e "\n\n"
        log_info "Uploading snapshot site to pmd.sourceforge.net/snapshot..."
        travis_wait rsync -ah --stats --delete pmd-doc-${VERSION}/ ${PMD_SF_USER}@web.sourceforge.net:/home/project-web/pmd/htdocs/snapshot/
        if [ $? -ne 0 ]; then
            log_error "Couldn't upload the snapshot documentation. It won't be current on http://pmd.sourceforge.net/snapshot/"
        else
            log_success "Successfully uploaded snapshot documentation: http://pmd.sourceforge.net/snapshot/"
        fi
    fi

    true
)




has_docs_change() {
    if [[ $(git diff --name-only ${TRAVIS_COMMIT_RANGE}) = *"docs/"* ]]; then
        log_info "Checking for changes in docs/ (TRAVIS_COMMIT_RANGE=${TRAVIS_COMMIT_RANGE}): changes found"
        return 0
    else
        log_info "Checking for changes in docs/ (TRAVIS_COMMIT_RANGE=${TRAVIS_COMMIT_RANGE}): no changes"
        return 1
    fi
}


#
# Push the generated site to gh-pages branch
#
if [[ "${VERSION}" == *-SNAPSHOT && "${TRAVIS_BRANCH}" == "master" ]] && has_docs_change; then
    echo -e "\n\n"
    log_info "Pushing the new site to github pages..."
    git clone --branch gh-pages --depth 1 git@github.com:pmd/pmd.git pmd-gh-pages
    # clear the files first
    rm -rf pmd-gh-pages/*
    # copy the new site
    cp -a pmd-doc-${VERSION}/* pmd-gh-pages/
    (
        cd pmd-gh-pages
        git config user.name "Travis CI (pmd-bot)"
        git config user.email "andreas.dangel+pmd-bot@adangel.org"
        git add -A
        git commit -q -m "Update documentation"
        git push git@github.com:pmd/pmd.git HEAD:gh-pages
        log_success "Successfully pushed site to https://pmd.github.io/pmd/"
    )
fi

popd
