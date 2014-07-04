#!/bin/bash
BASEDIR=$(perl -e "use Cwd 'abs_path';print abs_path('$0');")
BASEDIR=$(dirname "$BASEDIR")
CONFDIR="$BASEDIR/conf"
KEYFILE="$CONFDIR/mycert-key.pem"

JAVA=${JAVA:-java}

[ -n "$JAVA_HOME" ] && JAVA="$JAVA_HOME/bin/java"

JAVA_OPTS=(-classpath "$BASEDIR/lib/*")
JAVA_OPTS+=(-Dlogback.configurationFile="$CONFDIR/logback.xml")
JAVA_OPTS+=(-DBASEDIR="$BASEDIR")

case "$1" in
    delete)		CMD=DeleteFile		;;
    get-checksums)	CMD=GetChecksums	;;
    get-file)		CMD=GetFile		;;
    get-file-ids)	CMD=GetFileIDs		;;
    put-file)		CMD=PutFile		;;
    replace-file)	CMD=ReplaceFile		;;
    *)
	exec 1>&2
	echo "usage: $0 CMD PARAMS"
	echo "  CMD is one of"
	echo "    delete"
	echo "    get-checksums"
	echo "    get-file"
	echo "    get-file-ids"
	echo "    put-file"
	echo "    replace-file"
	exit 1
	;;
esac
CMD=org.bitrepository.commandline.$CMD
shift
exec "$JAVA" "${JAVA_OPTS[@]}" $CMD "-k$KEYFILE" "-s$CONFDIR" "$@"
