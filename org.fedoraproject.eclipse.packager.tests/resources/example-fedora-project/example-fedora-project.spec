%global eclipse_base   %{_libdir}/eclipse
%global install_loc    %{_datadir}/eclipse/dropins/packager

# This is essentially a snapshot of Fedora's eclipse-fedorapackager RPM spec file
# Don't use this for real ;-)
Name:           example-fedora-project
Version:        0.1.11
Release:        1%{?dist}
Summary:        Fedora Packager Tools

Group:          Development/Tools
License:        EPL
URL:            http://fedorahosted.org/eclipse-fedorapackager
Source0:        project_sources.zip
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

BuildArch: noarch

BuildRequires: java-devel
BuildRequires: eclipse-pde >= 1:3.4.0
BuildRequires: not-yet-commons-ssl
BuildRequires: json >= 3-3
BuildRequires: eclipse-changelog
BuildRequires: eclipse-rpm-editor
BuildRequires: jakarta-commons-codec
BuildRequires: jakarta-commons-httpclient >= 3.1
BuildRequires: xmlrpc3-client
BuildRequires: xmlrpc3-common
BuildRequires: ws-commons-util
# As of eclipse-fedorapackager 0.1.10 we require
# eclipse-egit >= 0.10.0, due to API changes in JGit/EGit
BuildRequires: eclipse-egit >= 0.10.0
Requires: eclipse-platform >= 3.4.0
Requires: json >= 3-3
Requires: not-yet-commons-ssl
Requires: eclipse-rpm-editor
Requires: eclipse-changelog
Requires: jakarta-commons-httpclient >= 3.1
Requires: jakarta-commons-codec
Requires: xmlrpc3-client
Requires: xmlrpc3-common
Requires: ws-commons-util
# As of eclipse-fedorapackager 0.1.10 we require
# eclipse-egit >= 0.10.0, due to API changes in JGit/EGit
Requires: eclipse-egit >= 0.10.0

%description
Eclipse Fedora Packager is an Eclipse plug-in, which helps
Fedora contributors to interact with Fedora infrastructure
such as Koji, Bodhi and Git.

%prep
%setup -q -n eclipse-fedorapackager
rm -fr org.apache*
rm -fr org.json*
mkdir orbit
pushd orbit
ln -s %{_javadir}/xmlrpc3-client.jar
ln -s %{_javadir}/xmlrpc3-common.jar
ln -s %{_javadir}/json.jar org.json.jar
ln -s %{_javadir}/ws-commons-util.jar
ln -s %{_javadir}/not-yet-commons-ssl.jar commons-ssl.jar
popd

%build
%{eclipse_base}/buildscripts/pdebuild \
                -f org.fedoraproject.eclipse.packager \
                -o `pwd`/orbit \
                -d "rpm-editor changelog jgit egit"

%install
%{__rm} -rf %{buildroot}
install -d -m 755 %{buildroot}%{install_loc}

%{__unzip} -q -d %{buildroot}%{install_loc} \
     build/rpmBuild/org.fedoraproject.eclipse.packager.zip

# Remove old and create new symlinks to Import-Packages 
# in %%{_datadir}/eclipse/dropins/packager
pushd $RPM_BUILD_ROOT%{install_loc}/eclipse/plugins
rm -rf xmlrpc3-client.jar xmlrpc3-common.jar org.json.jar \
       ws-commons-util.jar commons-ssl.jar
ln -s %{_javadir}/xmlrpc3-client.jar
ln -s %{_javadir}/xmlrpc3-common.jar
ln -s %{_javadir}/json.jar org.json.jar
ln -s %{_javadir}/ws-commons-util.jar
ln -s %{_javadir}/not-yet-commons-ssl.jar commons-ssl.jar
popd 

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root,-)
%{install_loc}
%doc org.fedoraproject.eclipse.packager-feature/*.html

%changelog
* Wed Jan 26 2011 Severin Gehwolf <sgehwolf@redhat.com> 0.1.11-1
- Changed %description.
- Update to upstream 0.1.11
- Fix Trac #42, #55, #58.
- Update doc plug-in.
- Work around JGit bug #317411 (Trac #58).

* Mon Jan 17 2011 Severin Gehwolf <sgehwolf@redhat.com> 0.1.10-1
- Update to upstream 0.1.10 release.
- Adapted code for better fedpkg compatibility (Trac #37, #38).
  Local branch names are now "f14"-like instead of "f14/master"
  with remote tracking configured.
- Refactored relevant JGit/EGit reliant code due to
  EGit/JGit 0.10.1 release.
- Added working set support to clone dialog (Trac #57,
  patch by mbooth).
- Check for unpushed changes before Koji build (Trac #53).

* Wed Dec 15 2010 Severin Gehwolf <sgehwolf@redhat.com> 0.1.9-1
- Add explicit autokarma=true when pushing Bodhi updates.
- Add pop-up with link to the pushed update.
- Upstream 0.1.9 release.

* Wed Oct 20 2010 Severin Gehwolf <sgehwolf@redhat.com> 0.1.8-1
- Merged changes from rawhide. Changes include:
- Improve error checking in import wizard.
- Update user documentation.
- Include potential fix for Ticket #34 (download performance
  problem).
- Remove eclipse-jgit R/BR since JGit is required by EGit
- Note: EGit API isn't stable yet. So, this package (0.1.6) might
  or might not work with EGit 0.10.x. It's known to work with
  EGit 0.9.x.
- Fixes for Trac #35, #38:
  https://fedorahosted.org/eclipse-fedorapackager/ticket/35
  https://fedorahosted.org/eclipse-fedorapackager/ticket/38

* Mon Oct 4 2010 Severin Gehwolf <sgehwolf@redhat.com> 0.1.3-1
- Merge rawhide changes.

* Mon Oct 4 2010 Severin Gehwolf <sgehwolf@redhat.com> 0.1.3-0.1
- Better error checking in FedoraCheckoutWizard.java
- Fixes https://fedorahosted.org/eclipse-fedorapackager/ticket/31
- Fixes https://fedorahosted.org/eclipse-fedorapackager/ticket/36

* Fri Oct 1 2010 Severin Gehwolf <sgehwolf@redhat.com> 0.1.2-1
- Fix getDistDefines() in FedoraHandlerUtils.

* Thu Sep 30 2010 Severin Gehwolf <sgehwolf@redhat.com> 0.1.1-1
- Merge changes from master.

* Fri Aug 27 2010 Severin Gehwolf <sgehwolf@redhat.com> 0.1.0-0.3
- Updated Eclipse help for Eclipse Fedora Packager.

* Thu Aug 26 2010 Severin Gehwolf <sgehwolf@redhat.com> 0.1.0-0.2
- Fix feature and bundle version, egit/jgit dependencies.

* Thu Aug 26 2010 Severin Gehwolf <sgehwolf at, redhat.com> 0.1.0-0.1
- Rebase to 0.1.0 (introduces dist-git support).

* Tue Jul 13 2010 Severin Gehwolf <sgehwolf at, redhat.com> 0.0.3-6
- Push release for tagging

* Tue Jul 13 2010 Severin Gehwolf <sgehwolf at, redhat.com> 0.0.3-5
- Fix getMD5(). Wrong method has been used

* Tue Jul 13 2010 Severin Gehwolf <sgehwolf at, redhat.com> 0.0.3-4
- Fix NoSuchMethodError in getMD5()

* Mon Jul 12 2010 Severin Gehwolf <sgehwolf at, redhat.com> 0.0.3-3
- Updated to latest upstream
- Added get-eclipse-fedorapackager-sources.sh

* Wed Jun 23 2010 Severin Gehwolf <sgehwolf at, redhat.com> 0.0.3-2
- Fixed ambiguous Source0/Source1
- Removed -D -v switches of pdebuild call
- Bumped release version

* Tue Jun 22 2010 Severin Gehwolf <sgehwolf at, redhat.com> 0.0.3-1
- Initial release