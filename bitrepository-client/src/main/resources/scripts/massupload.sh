#!/bin/bash
if [ $# -lt 3 ] ; then
    exec 1>&2
    echo "Usage: $0 COLLECTION PREFIX FILES"
    exit 1
fi
BASEDIR=$(perl -e "use Cwd 'abs_path';print abs_path('$0');")
BASEDIR=$(dirname "$BASEDIR")

collection="$1"
prefix="$2"
shift 2


for file ; do
    name="$prefix${file##*/}"
    echo '***' uploading \""$file"\" as \""$name"\" '***'
    "$BASEDIR/bitmag.sh" put-file -c $collection -f "$file" -i "$name"
    echo
done
