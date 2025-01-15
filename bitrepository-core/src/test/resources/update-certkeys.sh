#!/bin/bash
# Run this script to update test-pem-files with certificates/keys
# I.e. './update-certkeys.sh' or './bitrepository-core/src/test/resources/update-certkeys.sh'
# The script is just a slightly modified version of KBs 'cert-admin.sh' that is usually used to create self-signed certs

genkey () {
    ## KEYFILE
    openssl genrsa -out "${1%.pem}.pem" 2048
    chmod go= "${1%.pem}.pem"
}

gencert () {
    ## KEYFILE CERTFILE [CN]
    ## KEYFILE must exist
    if [ -n "$3" ] ; then
	openssl req -sha256 -new -x509 -key "${1%.pem}.pem" -out "${2%.pem}.pem" \
	    -days 3650 \
	    -subj /C=DK/ST=Denmark/L=Aarhus/O="Bitrepository.org"/CN=$3
    else
	openssl req -sha256 -new -x509 -key "${1%.pem}.pem" -out "${2%.pem}.pem" \
	    -days 3650
    fi
}

combine () {
    ## KEYFILE CERTFILE CERTKEYFILE
    cat ${2%.pem}.pem ${1%.pem}.pem > ${3%.pem}.pem
    openssl pkey -in ${1%.pem}.pem >> ${3%.pem}.pem
    chmod go= "${3%.pem}.pem"
}

genall () {
    ## PREFIX CN
    ## make PREFIX-key.pem PREFIX-cert.pem and PREFIX-certkey.pem
    genkey $1-key
    gencert $1-key $1-cert $2
    combine $1-key $1-cert $1-certkey
    # Don't care about individual files - REMOVE THIS IF YOU WANT THE SEPARATE FILES
    rm $1-key.pem $1-cert.pem
}

SOURCE=$(dirname ${BASH_SOURCE[0]})
pushd $SOURCE > /dev/null
genall client80 client80
genall client90 client90
genall client100 client100
popd > /dev/null