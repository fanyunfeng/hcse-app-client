package com.hcse.app.client.d2;

import org.apache.commons.cli.CommandLine;

import com.hcse.app.client.CommonClient;
import com.hcse.app.client.ExitExeption;

public class ClientBase extends CommonClient {

	@Override
	protected void run(CommandLine cmd) throws ExitExeption {
		// TODO Auto-generated method stub
		
	}

    protected String getDefaultUrl(){
    	return "index://127.0.0.1:3000";
    }
}
