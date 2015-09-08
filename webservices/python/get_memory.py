#!/usr/bin/python
print "Content-type:text/html\n\n"
import MySQLdb

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

    cur = conn.cursor()
    cur.execute("SELECT request, response FROM catos_memory")

    rows = cur.fetchall()

    for row in rows:
        print row
        