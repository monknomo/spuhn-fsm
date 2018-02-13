package com.gunnargissel.spuhn;

public interface State<Context> {

	void enter(Context ctx);
	
}
