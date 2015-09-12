#!/usr/bin/env python
# -*- coding: UTF-8 -*-

import cgi
import fileinput
import os
import sys
import time

print "Content-Type: text/plain;charset=utf-8"
print

todo = ''
fs = cgi.FieldStorage()
if(fs.has_key('param')):
    todo = fs['param'].value
if(todo == ''):
    print "Nothing to do"
    sys.exit()
print "Adding todo..." + todo

todo_file_path = '../mike/pda/'
todo_file_name = 'whatgoeson.note'
time_stamp = time.strftime('%Y%m%d')
backup_file_name = todo_file_name + '.' + time_stamp
print "Backup file: " + backup_file_name

#make backup copy of todo file
os.chdir(todo_file_path)
os.rename(todo_file_name, backup_file_name)
print "Renamed " + todo_file_name + " to " + backup_file_name + " in " + todo_file_path

#open new todo backup file
new_todo_file = open(todo_file_name, 'wb')
print "Opened new file..."

#navigate to appropriate place in file and write todo
todo_added = False
print "Reading old file..."
for line in fileinput.input(backup_file_name):
    new_todo_file.write(line)
    if line.startswith('**') and todo_added == False:
        new_todo_file.write('<todo><added>' + time_stamp + '</added><due></due>' + todo + '</todo>\n')
        todo_added = True
print "New file written"

#close files
new_todo_file.close()
print "Closed new file"
