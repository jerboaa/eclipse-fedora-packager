#!/usr/bin/python
#
# A simple script to simulate lookaside cache behaviour.
#
# General structure of lookaside cache
#
# <packagename>
#   |
#    `-<filename>
#        |
#         `-<checksum>
#              |
#               `-<filename>
#

import cgi, os, sys
import cgitb; cgitb.enable()

# Custom error class
class InvalidParamsError(Exception):
    def __init__(self, value):
        self.value = value
    def __str__(self):
        return  repr(self.value)

LOOKASIDE_LOCATION="/tmp/eclipse-fedorapackager-lookaside"

# Make sure lookaside dir is there
try:
    os.lstat(LOOKASIDE_LOCATION)
except OSError:
    try:
        os.mkdir(LOOKASIDE_LOCATION)
    except OSError:
        print "Content-type: text/plain\n"
        print "IOError unable to create lookaside dir!"
    

orig_cwd = os.getcwd()
form = cgi.FieldStorage()

def save_uploaded_file (form, form_field, upload_dir, checksum, name):
    """This saves an upload file into path "upload_dir/name/filename/checksum"
       using the name of the file as it was provided in the POST request.
    """
    if not form.has_key(form_field): return None
    fileitem = form[form_field]
    if not fileitem.file: return None
    filename = os.path.basename(fileitem.filename)
    try:
        os.lstat(os.path.join(upload_dir, name))
    except OSError:
        # package dir does not exist, so create it
        os.mkdir(os.path.join(upload_dir, name))
    try:
        os.lstat(os.path.join(upload_dir, name, filename))
    except OSError:
        # filename dir does not exist, so create it
        os.mkdir(os.path.join(upload_dir, name, filename))
    try:
        os.lstat(os.path.join(upload_dir, name, filename, checksum))
    except OSError:
        # filename dir does not exist, so create it
        os.mkdir(os.path.join(upload_dir, name, filename, checksum))
    fout = file(os.path.join(upload_dir, name, filename,
                             checksum, fileitem.filename), 'wb')
    while 1:
        chunk = fileitem.file.read(100000)
        if not chunk: break
        fout.write (chunk)
    fout.close()

def get_checksum_name_filename(form):
    """ Get required parameters form request """
    if ((not form.has_key("md5sum")) or
        (not form.has_key("filename")) or
        (not form.has_key("name"))):
         raise InvalidParamsError("Missing required parameters")
    return (form["md5sum"].value, form["name"].value, form["filename"].value)

def get_checksum_name(form):
    """ Get required parameters form request """
    if ((not form.has_key("md5sum")) or
        (not form.has_key("file")) or
        (not form.has_key("name"))):
         raise InvalidParamsError("Missing required parameters")
    return (form["md5sum"].value, form["name"].value)

def check_file_available(form):
    """Test if file is present in directory
       LOOKASIDE_LOCATION/<md5sum>/filename/<md5sum>. If it is
       return True. False, otherwise.
    """
    (checksum, name, filename) = get_checksum_name_filename(form)
    
    try:
        os.chdir(os.path.join(LOOKASIDE_LOCATION, name))
        os.chdir(filename)
        for f in os.listdir(checksum):
            if f == filename:
                return True
    except OSError as e:
        pass
    return False

response_text = None
available = False
try:
    available = check_file_available(form)
except InvalidParamsError as e:
    response_text = e.value
    
if not form.has_key("file") and response_text:
    print "Content-type: text/plain\n"
    print response_text
    os.chdir(orig_cwd)
    sys.exit(0)

if form.has_key("file"):
    (checksum, name) = get_checksum_name(form)
    save_uploaded_file(form, "file", LOOKASIDE_LOCATION, checksum, name)
    os.chdir(orig_cwd)
    print "Content-type: text/plain\n"
    print "Upload successful!"
    sys.exit(0)
elif not available:
    print "Content-type: text/plain\n"
    print "missing"
else:
    print "Content-type: text/plain\n"
    print "available"
os.chdir(orig_cwd)
