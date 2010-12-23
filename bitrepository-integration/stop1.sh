PIDS=$(ps -wwfe | grep dk.bitmagasin | grep -v grep | grep Mockup | awk "{print \$2}")
if [ -n "$PIDS" ] ; then
    kill -9 $PIDS;
fi

rm *.log
