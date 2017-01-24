#!/bin/sh

pushd tools/gyp

chmod u+x gyp
chmod u+x gyp_main.py

popd

tools/gyp/gyp --depth=. -f make --generator-output=out

if [ $? -ne 0 ];then
	echo "gyp failed!"
	exit 1
fi

pushd out
make
popd
