#!/usr/bin/python
import sqlite3
import time
import os
from os.path import expanduser, join
 
conn = sqlite3.connect('wordlist.db')
c = conn.cursor()
c.execute("DELETE FROM words")
words = open(expanduser('wordlist.txt')).read().splitlines()
i = 0
for word in words:
    c.execute("INSERT INTO words VALUES(%d, '%s')" % (i, word))
    i += 1
    
c.execute("DELETE FROM version_info")
version = int(time.time())
c.execute("INSERT INTO version_info VALUES (%s)" % version)    
conn.commit()

c.close()
conn.close()

version_file = open(join('..', 'assets', 'wordlist.version'), 'w')
version_file.write("%s\n" % version)

print "Built DB with %d words." % (i-1) 
os.system("./split.sh")
