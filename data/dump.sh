cat *.wl | pcregrep -v "[^\x00-\x7F]" |  grep -E "^[a-z]{4,}$" > wordlist.txt
