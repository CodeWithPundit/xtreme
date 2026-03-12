#!/bin/bash

# Certificate renewal script for keystore
KEYSTORE_PATH="app/release.keystore"
KEYSTORE_PASSWORD=$1
KEY_ALIAS=$2
KEY_PASSWORD=$3
DAYS_VALID=365

# Check if keystore exists
if [ ! -f "$KEYSTORE_PATH" ]; then
    echo "Keystore not found. Generating new one..."
    keytool -genkey -v -keystore $KEYSTORE_PATH \
        -alias $KEY_ALIAS \
        -keyalg RSA \
        -keysize 2048 \
        -validity $DAYS_VALID \
        -storepass $KEYSTORE_PASSWORD \
        -keypass $KEY_PASSWORD \
        -dname "CN=Xtreme IPTV, OU=Development, O=Xtreme, L=City, S=State, C=US"
else
    echo "Checking certificate expiration..."
    keytool -list -v -keystore $KEYSTORE_PATH -storepass $KEYSTORE_PASSWORD | grep "until"
    
    # Renew if needed
    echo "To renew, delete the keystore and run this script again"
fi
