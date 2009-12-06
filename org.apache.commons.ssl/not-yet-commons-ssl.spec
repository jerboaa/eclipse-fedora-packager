Name:           not-yet-commons-ssl
Version:        0.3.10
Release:        1%{?dist}
Summary:        SSL library for Jakarta Commons

Group:          Development/Libraries/Java
License:        ASL 2.0
URL:            http://juliusdavies.ca/commons-ssl/
Source0:        http://juliusdavies.ca/commons-ssl/%{name}-%{version}.zip
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

BuildRequires:  java-devel >= 1.4.0
BuildRequires:  jpackage-utils
BuildRequires:  jakarta-commons-httpclient >= 3.1.0
BuildRequires:  jakarta-commons-logging >= 1.0.4
BuildRequires:  log4j >= 1.2.13
BuildRequires:  junit >= 3.8.1
BuildRequires:  ant

Requires:  java >= 1.4.0
Requires:  jpackage-utils  

%description
Welcome to the SSL component of the Jakarta Commons
project.

This is not a real Jakarta Project yet.  I'm just 
trying to copy their directory structure while I work
on this proposal.

%package javadoc
Summary:        Javadocs for %{name}
Group:          Development Documentation
Requires:       %{name} = %{version}-%{release}
Requires:       jpackage-utils

%description javadoc
This package contains the API documentation for %{name}.


%prep
%setup -q
find -name '*.jar' -o -name '*.class' -exec rm -f '{}' \;


%build
ant jar javadocs


%install
rm -rf $RPM_BUILD_ROOT

mkdir -p $RPM_BUILD_ROOT%{_javadir}
cp -p build/commons-ssl.jar   \
$RPM_BUILD_ROOT%{_javadir}/%{name}-%{version}.jar

mkdir -p $RPM_BUILD_ROOT%{_javadocdir}/%{name}
cp -rp build/javadocs  \
$RPM_BUILD_ROOT%{_javadocdir}/%{name}


%clean
rm -rf $RPM_BUILD_ROOT


%files
%defattr(-,root,root,-)
%{_javadir}/*
%doc LICENSE.txt README.txt NOTICE.txt


%files javadoc
%defattr(-,root,root,-)
%{_javadocdir}/%{name}



%changelog
