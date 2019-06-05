package org.tta.elementlocators;

public interface ILoginLocators_Offline {
	
	public String getOfflineModeErrorMsg();

	public String getSignInLocatorId();

	public String getEmailLocatorId();

	public String getCloseEULAId();

	public String getPasswordLocatorId();

	public boolean isAndroid();
	
	public String getLogoutId();
	
	public String getHeaderId_Name();
	
	public String getForgotPasswordId();
	
	public String getEULALinkId();
	
	public String getFaceBookBtnId();
	
	public String getGmailBtnId();
	
	public String getNewUserSignUpId();
	
	public String getForgotPasswordErrorMsgName();
	
	public String getOkPopUpId();
	
	public String getTxtEULAHeaderName();
	
	public String gettxtBySigningInName();
	public String getTxtOrSignInWithName();
	
	/* Android Locator Ids */
	String Android_btnLogOutId = "org.tta.mobile:id/logout_button";
	String Android_btnHeaderId = "android:id/up";
	String Android_btnOkPopupId = "org.tta.mobile:id/positiveButton";
	String ANDROID_ID_EMAIL = "org.tta.mobile:id/email_et";
	String Android_tbPasswordId = "org.tta.mobile:id/password_et";
	String Android_btnSigninId = "org.tta.mobile:id/login_button_layout";
	String Android_btnForgotPasswordId = "org.tta.mobile:id/forgot_password_tv";
	String Android_btnEULAId = "org.tta.mobile:id/end_user_agreement_tv";
	String Android_btnNeedAnAccountId = "org.tta.mobile:id/new_user_tv";
	String Android_lbSignupId = "org.tta.mobile:id/by_signing_up_tv";
	String Android_lbNewUserId = "org.tta.mobile:id/signup_text";
	String Android_btnCancelPopupId = "org.tta.mobile:id/negativeButton";
	String Android_txtEULAHeaderId = "org.tta.mobile:id/tv_dialog_title";
	String Android_btnfacebookId="org.tta.mobile:id/img_facebook";
	String Android_btnGmailId="org.tta.mobile:id/img_google";
	

	/* iOS Locator Ids */
	String tbEmailIdiOS = "tbUserName";
	String tbPasswordIdiOS = "tbPassword";
	String btnSigninIdiOS = "btnSignIn";
	String hlnkForgotPasswordIdiOS = "lnforgot";
	String hlnkEULAIdiOS = "lnEULA";
	String hlnkSignupIdiOS = "btnNeedAnAccount";
	String btnCloseEULAIdiOS = "btnClose";
	String webLinkIdiOS = "URL";
	String btnOkPopupIdiOS = "OK";
	String btnHeaderIdiOS = "btnNavigation";
	String btnLogOutIdiOS = "btnLogout";
	String btnfacebookIdiOS = "btnFacebook";
	String btnGmailIdiOS = "btnGoogle";
	
	//Common locators
	String offlineModeMsg = "You are not connected to the Internet.";
	String popUpForgotPasswordHeaderName="Connection Error";
	String popUpForgotPasswordBodyName="You are not connected to the Internet. Please check your Internet connection.";
	String txtEULAHeaderName="END USER LICENSE AGREEMENT";
	String txtOrSignInWithName="Or Sign in with";
	String txtBySigningInName="By signing in to this app, you agree to the";
	
}
