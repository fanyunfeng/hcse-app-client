package com.hcse.app.client.cache;

import org.apache.commons.cli.CommandLine;

import com.hcse.app.client.CommonClient;
import com.hcse.app.client.ExitExeption;

public class ClientBase extends CommonClient {

	protected void run(CommandLine cmd) throws ExitExeption {
	}

    protected String getDefaultUrl(){
    	return "cache://127.0.0.1:3000";
    }

}
