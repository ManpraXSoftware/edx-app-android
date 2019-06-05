package org.tta.elementlocators;

public interface ISignUpLocators {

	String getOfflineMessageHeaderByName();
	
	String getOfflineMessageByName();
	
	public String getEdxLogoById();

	public String getSignUpButtonByName();

	public String getSignUpTextByName();

	public String getCloseButtonById();

	public String getCreateMyAccountById();

	public String getCreateMyAccountByName();

	public String getShowOptionalFieldByName();

	public String getAgreeToEULAById();

	public String getEULAById();

	// Android locators

	// Landing screen
	String Android_edXLogoId = "org.tta.mobile:id/edx_logo";
	String Android_signUpButtonName = "Sign up and start learning";
	String Android_signUpButtonId = "org.tta.mobile:id/sign_up_btn";
	String Android_signInByName = "Already have an account? Sign in";

	// Sign up page
	String Android_signUpTextByName = "Sign up for edX";
	String Android_closeBtnById = "org.tta.mobile:id/actionbar_close_btn";
	String Android_createAccountById = "org.tta.mobile:id/create_account_tv";
	String Android_showOptionalFieldByName = "Show optional fields";
	String Android_createAccountByName = "Create my account";
	String Android_txtAgreeToEULAById = "org.tta.mobile:id/by_creating_account_tv";
	String Android_EULAById = "org.tta.mobile:id/end_user_agreement_tv";
	
	//Offline mode
	String Android_offlineModeheaderMsg="Connection Error";
	String Android_offlineModeMsg="You are not connected to the Internet.";
}
