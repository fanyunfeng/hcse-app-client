#!/bin/bash
bin=`which $0`
bin=`dirname ${bin}`
bin=`cd "$bin"; pwd`

#echo ${bin}

java ${HCSE_SERVICE_OPTS} -cp ${bin}/../conf:${bin}/../lib/* com.hcse.app.ClientMgr
