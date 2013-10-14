/**
 * 
 */
package com.framgia.takasukamera.social;

/**
 * This interface for implement callback to update UI.
 * 
 * @author TuyenDN
 * 
 */
public interface FbLoginChangeListener {

	/**
	 * This method used for update UI when application login from Facebook
	 * Server.
	 * */
	public void updateLogin(boolean loginStatus);
}
