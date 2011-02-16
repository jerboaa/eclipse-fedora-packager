#!/bin/bash
#
# Automate Eclipse Fedora Packager update site creation.
# More info on p2 composite repositories is here:
#  http://wiki.eclipse.org/Equinox/p2/Composite_Repositories_%28new%29
#
# Example call: ./build_update_site.sh master /usr/lib64/eclipse
#
#set -xv  # Debugging

#
# Configuration
#
SITE_XML="`pwd`/site.xml"
COMPOSITE_CONTENT_XML="`pwd`/compositeContent.xml"
COMPOSITE_ARTIFACTS_XML="`pwd`/compositeArtifacts.xml"
DEFAULT_DEST="`pwd`/eclipse-fedorapackager-p2-composite-repo"

checkConfig() {
	if [ "${SITE_DEST}_" == "_" ]; then
		SITE_DEST="$DEFAULT_DEST"
	else
		SITE_DEST="$SITE_DEST/eclipse-fedorapackager-p2-composite-repo"
	fi
	# check if site.xml etc. are available
	if [ ! -e "$SITE_XML" ] || [ ! -e "$COMPOSITE_ARTIFACTS_XML" ] ||
	[ ! -e "$COMPOSITE_CONTENT_XML" ]; then
		echo 1>&2 "$SITE_XML, $COMPOSITE_CONTENT_XML, $COMPOSITE_ARTIFACTS_XML do not exist, please fix this first. Aborting."
		exit 1
	fi
	if [ -e "$SITE_DEST" ]; then
		echo 1>&2 "Destination, $SITE_DEST, already exists! Aborting."
		exit 1
	fi
	if ! mkdir -p "$SITE_DEST"; then
		echo 1>&2 "Could not create $SITE_DEST! Aborting."
		exit 1
	fi
}

usage() {
cat <<END

SYNOPSYS
        $0 REVISION ECLIPSE_PATH [DEST]
        
DESCRIPTION
        A shell script to build a composite p2 repository for
        Eclipse Fedora Packager.

        REVISION        The Git revision which should be used
                        for Eclipse Fedora Packager source
                        retrieval.
        ECLIPSE_PATH    The absolute path to the Fedora Eclipse
                        installation.
        DEST            Destination folder where p2 repo should be
                        created.

END
}


# Need at least 2 parameters
if [ $# -lt 2 ]; then
	usage
	exit 1
fi

REV=$1
ECLIPSE_BASE=$2

if [ $# -gt 2 ]; then
	SITE_DEST=$3
fi
# Make sure we have a reasonable setup
checkConfig

if [ "${REV}_" == "_" ] || [ "${ECLIPSE_BASE}_" == "_" ]; then
	usage
	exit 1
fi

# Do the work in /tmp
CURR_DIR="`pwd`"
WORKDIR="`mktemp -d`"
cd "$WORKDIR"

# Get Eclipse Fedora Packager sources
TAR_NAME="eclipse-fedorapackager-$REV.tar.xz"
REPO="git://git.fedorahosted.org/eclipse-fedorapackager.git"
git clone -q $REPO && \
pushd eclipse-fedorapackager > /dev/null && \
git archive --format=tar --prefix="eclipse-fedorapackager-$REV/" "$REV" |xz > "$TAR_NAME" && \
mv "$TAR_NAME" ../ && \
popd > /dev/null && rm -rf eclipse-fedorapackager

tar -xJf $TAR_NAME
pushd "eclipse-fedorapackager-$REV" > /dev/null

# Link orbit deps
mkdir orbit
pushd orbit > /dev/null 
ln -s /usr/share/java/xmlrpc3-client.jar
ln -s /usr/share/java/xmlrpc3-common.jar
ln -s /usr/share/java/json.jar org.json.jar
ln -s /usr/share/java/ws-commons-util.jar
ln -s /usr/share/java/not-yet-commons-ssl.jar commons-ssl.jar
popd > /dev/null 

PDE_BUILD="$ECLIPSE_BASE/buildscripts/pdebuild"

# Build Eclipse Fedora Packager
$PDE_BUILD -f org.fedoraproject.eclipse.packager \
           -o `pwd`/orbit \
           -d "rpm-editor changelog egit jgit" > build.log 2>&1

# Of course ant, returns a status code of 127 no matter if it
# fails or succeeds. :) Well, work around it that way.
if `tail -n50 build.log | grep -q "BUILD FAILED"`; then
	cat build.log
	echo
	echo 1>&2 "Build failed, fix build errors first. Aborting."
	exit 1
fi

# Get built plug-ins
pushd build/rpmBuild > /dev/null 
unzip -qq -o -d packager org.fedoraproject.eclipse.packager.zip


# Copy site.xml, deps and generate local update site
pushd packager > /dev/null 
cp -rL ../../../orbit/* eclipse/plugins/
cp $SITE_XML eclipse
java -jar $ECLIPSE_BASE/plugins/org.eclipse.equinox.launcher_*.jar -application org.eclipse.equinox.p2.publisher.UpdateSitePublisher -metadataRepository file:/`pwd`/repo -artifactRepository file:/`pwd`/repo -source `pwd`/eclipse -publishArtifacts -compress > /dev/null 2>&1
popd > /dev/null 

# Create compositite p2 repository so that EGit and Linux Tools deps
# are properly referenced
# Move local update site under composite destination
mv packager/repo "$SITE_DEST"/efp
# Final stich-up
cp "$COMPOSITE_ARTIFACTS_XML" "$COMPOSITE_CONTENT_XML" "$SITE_DEST"

# Go back
cd "$CURR_DIR"

# Cleanup
rm -rf $WORKDIR


exit 0 # All good :)
