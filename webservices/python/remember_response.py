#!/usr/bin/python
print "Content-type:text/html\n\n"

import MySQLdb
from cgi import parse_qs, escape, FieldStorage

args = FieldStorage()
request = args['request'].value
response = args['response'].value

try:
 conn = MySQLdb.connect (
  host = "gilmorec.ipowermysql.com",
  user = "cato",
  passwd = "2015iztheyeer",
  db = "gilmorec_pda")

except MySQLdb.Error, e:
 print "Error %d: %s" % (e.args[0], e.args[1])
 sys.exit (1)

#print "connected to the database"

with conn: 

    add_employee = ("INSERT INTO catos_memory "
               "(request, response) "
               "VALUES (%s, %s)")

    data_employee = (request, response)
    
    cur = conn.cursor()
    cur.execute(add_employee, data_employee)
