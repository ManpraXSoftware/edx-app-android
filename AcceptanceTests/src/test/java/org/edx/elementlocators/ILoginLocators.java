/**
 * 
 */
package org.tta.elementlocators;

/**
 * @author divakarpatil
 * 
 */
public interface ILoginLocators {

	String getSignInTextName();
	String getSignInButtonId();

	public String getWebLinkId();

	public String getPasswordResetEmailMessage();

	public String getMyCoursesName();

	public String getSignINButtonChangeText();

	public String getNeedAnAccountId();

	public String getEULALinkId();

	public String getMsgForgotPasswordWithWrongEmailId();

	public String getOkPopUpId();

	public String getForgotPasswordMailId_ClassName();

	public String getForgotPasswordId();

	public String getLogoutId();

	public String getHeaderId_Name();

	public String getMsgSignInWithNoEmailOrCredentials();

	public String getMsgSignInWithNoPassword();

	public String getMsgInvalidCredentials();

	public String getPasswordLocatorId();

	public boolean isAndroid();

	public String getSignInLocatorId();

	public String getEmailLocatorId();

	public String getCloseEULAId();

	public String getCancelPopUpId();

	public String getGmailBtnId();

	public String getFacebookBtnId();

	public String getWebViewClassName();

	/*
	 * Android id's
	 */
	
	//Landing Screen
	String ANDROID_ID_btnSIGNINId="org.tta.mobile:id/sign_in_tv";
	
	String Android_Name_signinText="Sign in to edX";
	
	// Login to app id
	String ANDROID_ID_EMAIL = "org.tta.mobile:id/email_et";
	String Android_tbPasswordId = "org.tta.mobile:id/password_et";
	String Android_btnSigninId = "org.tta.mobile:id/login_button_layout";

	// forgot password id
	String Android_hlnkForgotPasswordId = "org.tta.mobile:id/forgot_password_tv";
	String Android_tbForgotPasswordId = "org.tta.mobile:id/email_edit";
	String Android_btnOkPopupId = "org.tta.mobile:id/positiveButton";
	String Android_btnCancelPopupId = "org.tta.mobile:id/negativeButton";

	// Logout id
	String Android_btnHeaderId = "android:id/up";
	String Android_btnLogOutId = "org.tta.mobile:id/logout_button";

	// EULA id
	String Android_hlnkEULAId = "org.tta.mobile:id/end_user_agreement_tv";
	String Android_txtEULAHeaderId = "org.tta.mobile:id/tv_dialog_title";
	String Android_WebViewClassName = "android.webkit.WebView";

	// Need an account id
	String Android_hlnkNeedAnAccountId = "org.tta.mobile:id/new_user_tv";
	String Android_lbSignupId = "org.tta.mobile:id/by_signing_up_tv";
	String Android_lbNewUserId = "org.tta.mobile:id/signup_text";//
	String Android_webLinkId = "com.android.chrome:id/url_bar";

	// Facebook and Gmail button id
	String Android_btnfacebookId = "org.tta.mobile:id/img_facebook";
	String Android_btnGmailId = "org.tta.mobile:id/img_google";

	/* iOS Locator Ids */
	// Login id
	String tbEmailIdiOS = "tbUserName";
	String tbPasswordIdiOS = "tbPassword";
	String btnSigninIdiOS = "btnSignIn";

	// Forgot password id
	String hlnkForgotPasswordIdiOS = "lnforgot";
	String btnCloseIdiOS = "btnClose";
	String btnOkPopupIdiOS = "OK";
	String btnCancelPopupIdiOS = "Cancel";
	String tbForgotPasswordClassnameiOS = "UIATextField";

	// EULA id
	String hlnkEULAIdiOS = "lnEULA";
	String webViewClassNameiOS = "UIAWebView";

	// Need an account id
	String hlnkNeedAnAccountIdiOS = "btnNeedAnAccount";
	String webLinkIdiOS = "URL";

	// Logout id
	String btnHeaderIdiOS = "btnNavigation";
	String btnLogOutIdiOS = "btnLogout";

	// Facebook and Gmail button id
	String btnfacebookIdiOS = "btnFacebook";
	String btnGmailIdiOS = "btnGoogle";

	// Common Locators
	String btnSignInChangingName = "SIGNING IN...";
	String txtMyCoursesName = "My Courses";
	String msgInvalidCredentials = "Please make sure that your user name or e-mail address and password are correct and try again.";
	String msgSignInWithNoPassword = "Please enter your password and try again.";
	String msgSignInWithNoEmailOrCredentials = "Please enter your user name or e-mail address and try again.";
	String msgForgotPasswordWithWrongEmailId = "Please make sure your e-mail address is formatted correctly and try again.";
	String msgForgotPasswordWithCorrectEmailId = "PASSWORD RESET E-MAIL SENT";
}
