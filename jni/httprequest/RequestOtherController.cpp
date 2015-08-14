/*
 * RequestOtherController.cpp
 *
 *  Created on: 2015-3-16
 *      Author: Samson.Fan
 */

#include "RequestOtherController.h"
#include "RequestDefine.h"
#include "RequestOtherDefine.h"
#include "../common/CommonFunc.h"
#include "../common/command.h"

RequestOtherController::RequestOtherController(HttpRequestManager *pHttpRequestManager, const RequestOtherControllerCallback& callback)
{
	// TODO Auto-generated constructor stub
	SetHttpRequestManager(pHttpRequestManager);
	m_Callback = callback;
}

RequestOtherController::~RequestOtherController()
{
	// TODO Auto-generated destructor stub
}

/* IHttpRequestManagerCallback */
void RequestOtherController::onSuccess(long requestId, string url, const char* buf, int size)
{
	FileLog("httprequest", "RequestOtherController::onSuccess( url : %s, buf( size : %d ) )", url.c_str(), size);
	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestOtherController::onSuccess(), buf: %s", buf);
	}

	if( url.compare(OTHER_EMOTIONCONFIG_PATH) == 0 ) {
		EmotionConfigCallbackHandle(requestId, url, true, buf, size);
	}
	else if ( url.compare(OTHER_GETCOUNT_PATH) == 0 ) {
		GetCountCallbackHandle(requestId, url, true, buf, size);
	}
	else if ( url.compare(OTHER_PHONEINFO_PATH) == 0 ) {
		PhoneInfoCallbackHandle(requestId, url, true, buf, size);
	}
	else if ( url.compare(OTHER_INTEGRALCHECK_PATH) == 0 ) {
		IntegralCheckCallbackHandle(requestId, url, true, buf, size);
	}
	else if ( url.compare(OTHER_VERSIONCHECK_PATH) == 0 ) {
		VersionCheckCallbackHandle(requestId, url, true, buf, size);
	}
	else if ( url.compare(OTHER_SYNCONFIG_PATH) == 0 ) {
		SynConfigCallbackHandle(requestId, url, true, buf, size);
	}
	else if ( url.compare(OTHER_ONLINECOUNT_PATH) == 0 ) {
		OnlineCountCallbackHandle(requestId, url, true, buf, size);
	}
	else if( url.compare(OTHER_UPLOAD_CRASH_PATH) == 0 ) {
		UploadCrashLogCallbackHandle(requestId, url, true, buf, size);
	}
	FileLog("httprequest", "RequestOtherController::onSuccess() end, url:%s", url.c_str());
}

void RequestOtherController::onFail(long requestId, string url)
{
	FileLog("httprequest", "RequestOtherController::onFail( url : %s )", url.c_str());
	/* request fail, callback fail */
	if( url.compare(OTHER_EMOTIONCONFIG_PATH) == 0 ) {
		EmotionConfigCallbackHandle(requestId, url, false, NULL, 0);
	}
	else if ( url.compare(OTHER_GETCOUNT_PATH) == 0 ) {
		GetCountCallbackHandle(requestId, url, false, NULL, 0);
	}
	else if ( url.compare(OTHER_PHONEINFO_PATH) == 0 ) {
		PhoneInfoCallbackHandle(requestId, url, false, NULL, 0);
	}
	else if ( url.compare(OTHER_INTEGRALCHECK_PATH) == 0 ) {
		IntegralCheckCallbackHandle(requestId, url, false, NULL, 0);
	}
	else if ( url.compare(OTHER_VERSIONCHECK_PATH) == 0 ) {
		VersionCheckCallbackHandle(requestId, url, false, NULL, 0);
	}
	else if ( url.compare(OTHER_SYNCONFIG_PATH) == 0 ) {
		SynConfigCallbackHandle(requestId, url, false, NULL, 0);
	}
	else if ( url.compare(OTHER_ONLINECOUNT_PATH) == 0 ) {
		OnlineCountCallbackHandle(requestId, url, false, NULL, 0);
	}
	else if( url.compare(OTHER_UPLOAD_CRASH_PATH) == 0 ) {
		UploadCrashLogCallbackHandle(requestId, url, false, NULL, 0);
	}
	FileLog("httprequest", "RequestOtherController::onFail() end, url:%s", url.c_str());
}

// ----------------------- EmotionConfig -----------------------
long RequestOtherController::EmotionConfig()
{
	HttpEntiy entiy;
	string url = OTHER_EMOTIONCONFIG_PATH;
	FileLog("httprequest", "RequestOtherController::EmotionConfig"
			"(url:%s)",
			url.c_str());

	return StartRequest(url, entiy, this);
}

void RequestOtherController::EmotionConfigCallbackHandle(long requestId, const string& url, bool requestRet, const char* buf, int size)
{
	OtherEmotionConfigItem item;
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;

	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson) ) {
			bFlag = item.Parsing(dataJson);

			if (!bFlag) {
				// parsing fail
				errnum = LOCAL_ERROR_CODE_PARSEFAIL;
				errmsg = LOCAL_ERROR_CODE_PARSEFAIL_DESC;

				FileLog("httprequest", "RequestOtherController::EmotionConfigCallbackHandle() parsing fail:"
						"(url:%s, size:%d, buf:%s)",
						url.c_str(), size, buf);
			}
		}
	}
	else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( m_Callback.onRequestOtherEmotionConfig != NULL ) {
		m_Callback.onRequestOtherEmotionConfig(requestId, bFlag, errnum, errmsg, item);
	}
}

// ----------------------- GetCount -----------------------
long RequestOtherController::GetCount(bool money, bool coupon, bool regStep, bool allowAlbum, bool admirerUr, bool integral)
{
	HttpEntiy entiy;
	string url = OTHER_GETCOUNT_PATH;

	string strAction("");
	if (money) {
		if (!strAction.empty()) {
			strAction += ",";
		}
		strAction += "money";
	}
	if (coupon) {
		if (!strAction.empty()) {
			strAction += ",";
		}
		strAction += "coupon";
	}
	if (regStep) {
		if (!strAction.empty()) {
			strAction += ",";
		}
		strAction += "regstep";
	}
	if (allowAlbum) {
		if (!strAction.empty()) {
			strAction += ",";
		}
		strAction += "allowalbum";
	}
	if (admirerUr) {
		if (!strAction.empty()) {
			strAction += ",";
		}
		strAction += "admirer_ur";
	}
	if (integral) {
		if (!strAction.empty()) {
			strAction += ",";
		}
		strAction += "integral";
	}


	entiy.AddContent(OTHER_REQUEST_ACTION, strAction.c_str());

	FileLog("httprequest", "RequestOtherController::GetCount"
			"(url:%s, action:%s)",
			url.c_str(), strAction.c_str());

	return StartRequest(url, entiy, this);
}

void RequestOtherController::GetCountCallbackHandle(long requestId, const string& url, bool requestRet, const char* buf, int size)
{
	OtherGetCountItem item;
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;

	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson) ) {
			bFlag = item.Parsing(dataJson);

			if (!bFlag) {
				// parsing fail
				errnum = LOCAL_ERROR_CODE_PARSEFAIL;
				errmsg = LOCAL_ERROR_CODE_PARSEFAIL_DESC;

				FileLog("httprequest", "RequestOtherController::GetCountCallbackHandle() parsing fail:"
						"(url:%s, size:%d, buf:%s)",
						url.c_str(), size, buf);
			}
		}
	}
	else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( m_Callback.onRequestOtherGetCount != NULL ) {
		m_Callback.onRequestOtherGetCount(requestId, bFlag, errnum, errmsg, item);
	}
}

// ----------------------- PhoneInfo -----------------------
long RequestOtherController::PhoneInfo(const string& manId, int verCode, const string& verName, int action, int siteId
		, double density, int width, int height
		, const string& lineNumber, const string& simOptName, const string& simOpt, const string& simCountryIso, const string& simState
		, int phoneType, int networkType, const string& deviceId)
{
	char temp[16] = {0};
	HttpEntiy entiy;
	string url = OTHER_PHONEINFO_PATH;

	// model
	string strModel = GetPhoneModel();
	entiy.AddContent(OTHER_REQUEST_MODEL, strModel.c_str());

	// manufacturer
	string strManufacturer = GetPhoneManufacturer();
	entiy.AddContent(OTHER_REQUEST_MODEL, strManufacturer.c_str());

	// os
	string strOS = "Android";
	entiy.AddContent(OTHER_REQUEST_OS, strOS.c_str());

	// release
	string strRelease = GetPhoneBuildVersion();
	entiy.AddContent(OTHER_REQUEST_RELEASE, strRelease.c_str());

	// sdk
	string strSDK = GetPhoneBuildSDKVersion();
	entiy.AddContent(OTHER_REQUEST_SDK, strSDK.c_str());

	// density
	snprintf(temp, sizeof(temp), "%f", density);
	entiy.AddContent(OTHER_REQUEST_DENSITY, temp);

	// densityDpi
	string strDensityDPI = GetPhoneDensityDPI();
	entiy.AddContent(OTHER_REQUEST_DENSITYDPI, strDensityDPI.c_str());

	// width
	snprintf(temp, sizeof(temp), "%d", width);
	entiy.AddContent(OTHER_REQUEST_WIDTH, temp);

	// height
	snprintf(temp, sizeof(temp), "%d", height);
	entiy.AddContent(OTHER_REQUEST_HEIGHT, temp);

	// data
	string strData = "P0:";
	strData += manId;
	entiy.AddContent(OTHER_REQUEST_DATA, strData.c_str());

	// versionCode
	snprintf(temp, sizeof(temp), "%d", verCode);
	entiy.AddContent(OTHER_REQUEST_VERCODE, temp);

	// versionName
	entiy.AddContent(OTHER_REQUEST_VERNAME, verName.c_str());

	// PhoneType
	snprintf(temp, sizeof(temp), "%d", phoneType);
	entiy.AddContent(OTHER_REQUEST_PHONETYPE, temp);

	// NetworkType
	snprintf(temp, sizeof(temp), "%d", networkType);
	entiy.AddContent(OTHER_REQUEST_NETWORKTYPE, temp);

	// language
	string strLanguage = GetPhoneLocalLanguage();
	entiy.AddContent(OTHER_REQUEST_LANGUAGE, strLanguage.c_str());

	// country
	string strCountry = GetPhoneLocalRegion();
	entiy.AddContent(OTHER_REQUEST_COUNTRY, strCountry.c_str());

	// siteid
	string strSite("");
	if (OTHER_SITE_CL == siteId) {
		strSite = OTHER_SYNCONFIG_CL;
	}
	else if (OTHER_SITE_IDA == siteId) {
		strSite = OTHER_SYNCONFIG_IDA;
	}
	else if (OTHER_SITE_CD == siteId) {
		strSite = OTHER_SYNCONFIG_CD;
	}
	else if (OTHER_SITE_LA == siteId) {
		strSite = OTHER_SYNCONFIG_LA;
	}
	entiy.AddContent(OTHER_REQUEST_SITEID, strSite);

	// action
	string strAction("");
	if (action < _countof(OTHER_ACTION_TYPE)) {
		strAction = OTHER_ACTION_TYPE[action];
		entiy.AddContent(OTHER_REQUEST_ACTION, strAction.c_str());
	}

	// line1Number
	entiy.AddContent(OTHER_REQUEST_NUMBER, lineNumber.c_str());

	// deviceId
	entiy.AddContent(OTHER_REQUEST_DEVICEID, deviceId.c_str());

	// SimOperatorName
	entiy.AddContent(OTHER_REQUEST_SIMOPTNAME, simOptName.c_str());

	// SimOperator
	entiy.AddContent(OTHER_REQUEST_SIMOPT, simOpt.c_str());

	// SimCountryIso
	entiy.AddContent(OTHER_REQUEST_SIMCOUNTRYISO, simCountryIso.c_str());

	// SimState
	entiy.AddContent(OTHER_REQUEST_SIMSTATE, simState.c_str());

	FileLog("httprequest", "RequestOtherController::PhoneInfo"
			"(url:%s, model:%s, manufacturer:%s, os:%s, release:%s, sdk:%s, "
			"density:%f, densityDpi:%s, width:%d, height:%d, "
			"data:%s, versionCode:%d, versionName:%s, PhoneType:%d, NetworkType:%d, "
			"language:%s, country:%s, siteid:%s, action:%s, line1Number:%s, "
			"deviceId:%s, SimOperatorName:%s, SimOperator:%s, SimCountryIso:%s, SimState:%s)",
			url.c_str(), strModel.c_str(), strManufacturer.c_str(), strOS.c_str(), strRelease.c_str(), strSDK.c_str()
			, density, strDensityDPI.c_str(), width, height
			, strData.c_str(), verCode, verName.c_str(), phoneType, networkType
			, strLanguage.c_str(), strCountry.c_str(), strSite.c_str(), strAction.c_str(), lineNumber.c_str()
			, deviceId.c_str(), simOptName.c_str(), simOpt.c_str(), simCountryIso.c_str(), simState.c_str());

	return StartRequest(url, entiy, this);
}

void RequestOtherController::PhoneInfoCallbackHandle(long requestId, const string& url, bool requestRet, const char* buf, int size)
{
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;

	if (requestRet) {
		// request success
		Json::Value dataJson;
		bFlag = HandleResult(buf, size, errnum, errmsg, &dataJson);
	}
	else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( m_Callback.onRequestOtherPhoneInfo != NULL ) {
		m_Callback.onRequestOtherPhoneInfo(requestId, bFlag, errnum, errmsg);
	}
}

// ----------------------- IntegralCheck -----------------------
long RequestOtherController::IntegralCheck(const string& womanId)
{
	HttpEntiy entiy;
	string url = OTHER_INTEGRALCHECK_PATH;

	entiy.AddContent(OTHER_REQUEST_WOMANID, womanId.c_str());

	FileLog("httprequest", "RequestOtherController::IntegralCheck"
			"(url:%s, womanid:%s)",
			url.c_str(), womanId.c_str());

	return StartRequest(url, entiy, this);
}

void RequestOtherController::IntegralCheckCallbackHandle(long requestId, const string& url, bool requestRet, const char* buf, int size)
{
	OtherIntegralCheckItem item;
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;

	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson) ) {
			bFlag = item.Parsing(dataJson);

			if (!bFlag) {
				// parsing fail
				errnum = LOCAL_ERROR_CODE_PARSEFAIL;
				errmsg = LOCAL_ERROR_CODE_PARSEFAIL_DESC;

				FileLog("httprequest", "RequestOtherController::IntegralCheckCallbackHandle() parsing fail:"
						"(url:%s, size:%d, buf:%s)",
						url.c_str(), size, buf);
			}
		}
	}
	else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( m_Callback.onRequestOtherIntegralCheck != NULL ) {
		m_Callback.onRequestOtherIntegralCheck(requestId, bFlag, errnum, errmsg, item);
	}
}

// ----------------------- VersionCheck -----------------------
long RequestOtherController::VersionCheck(int currVersion)
{
	char temp[16] = {0};
	HttpEntiy entiy;
	string url = OTHER_VERSIONCHECK_PATH;

	snprintf(temp, sizeof(temp), "%d", currVersion);
	entiy.AddContent(OTHER_REQUEST_CURRVER, temp);

	FileLog("httprequest", "RequestOtherController::VersionCheck"
			"(url:%s, currVersion:%d)",
			url.c_str(), currVersion);

	return StartRequest(url, entiy, this);
}

void RequestOtherController::VersionCheckCallbackHandle(long requestId, const string& url, bool requestRet, const char* buf, int size)
{
	OtherVersionCheckItem item;
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;

	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson) ) {
			bFlag = item.Parsing(dataJson);

			if (!bFlag) {
				// parsing fail
				errnum = LOCAL_ERROR_CODE_PARSEFAIL;
				errmsg = LOCAL_ERROR_CODE_PARSEFAIL_DESC;

				FileLog("httprequest", "RequestOtherController::VersionCheckCallbackHandle() parsing fail:"
						"(url:%s, size:%d, buf:%s)",
						url.c_str(), size, buf);
			}
		}
	}
	else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( m_Callback.onRequestOtherVersionCheck != NULL ) {
		m_Callback.onRequestOtherVersionCheck(requestId, bFlag, errnum, errmsg, item);
	}
}

// ----------------------- SynConfig -----------------------
long RequestOtherController::SynConfig()
{
	HttpEntiy entiy;
	string url = OTHER_SYNCONFIG_PATH;

	FileLog("httprequest", "RequestOtherController::SynConfig"
			"(url:%s)",
			url.c_str());

	return StartRequest(url, entiy, this);
}

void RequestOtherController::SynConfigCallbackHandle(long requestId, const string& url, bool requestRet, const char* buf, int size)
{
	OtherSynConfigItem item;
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;

	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson) ) {
			bFlag = item.Parsing(dataJson);

			if (!bFlag) {
				// parsing fail
				errnum = LOCAL_ERROR_CODE_PARSEFAIL;
				errmsg = LOCAL_ERROR_CODE_PARSEFAIL_DESC;

				FileLog("httprequest", "RequestOtherController::SynConfigCallbackHandle() parsing fail:"
						"(url:%s, size:%d, buf:%s)",
						url.c_str(), size, buf);
			}
		}
	}
	else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( m_Callback.onRequestOtherSynConfig != NULL ) {
		m_Callback.onRequestOtherSynConfig(requestId, bFlag, errnum, errmsg, item);
	}
}

// ----------------------- OnlineCount -----------------------
long RequestOtherController::OnlineCount(int site)
{
	HttpEntiy entiy;

	string strSite("");
	if ((OTHER_SITE_CL & site) == 1) {
		strSite += !strSite.empty() ? OTHER_SITE_DELIMITER : "";
		strSite += OTHER_SYNCONFIG_CL;
	}
	if ((OTHER_SITE_IDA & site) == 1) {
		strSite += !strSite.empty() ? OTHER_SITE_DELIMITER : "";
		strSite += OTHER_SYNCONFIG_IDA;
	}
	if ((OTHER_SITE_CD & site) == 1) {
		strSite += !strSite.empty() ? OTHER_SITE_DELIMITER : "";
		strSite += OTHER_SYNCONFIG_CD;
	}
	if ((OTHER_SITE_LA & site) == 1) {
		strSite += !strSite.empty() ? OTHER_SITE_DELIMITER : "";
		strSite += OTHER_SYNCONFIG_LA;
	}

	if (!strSite.empty()) {
		entiy.AddContent(OTHER_REQUEST_SITEID, strSite);
	}

	string url = OTHER_ONLINECOUNT_PATH;

	FileLog("httprequest", "RequestOtherController::OnlineCount"
			"(url:%s, site:%s)",
			url.c_str(), strSite.c_str());

	return StartRequest(url, entiy, this);
}

void RequestOtherController::OnlineCountCallbackHandle(long requestId, const string& url, bool requestRet, const char* buf, int size)
{
	OtherOnlineCountList countList;
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;

	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson) ) {
			if (dataJson[COMMON_DATA_LIST].isArray()) {
				bFlag = true;

				int i = 0;
				for (i = 0; i < dataJson[COMMON_DATA_LIST].size(); i++) {
					OtherOnlineCountItem item;
					if (item.Parsing(dataJson[COMMON_DATA_LIST].get(i, Json::Value::null))) {
						countList.push_back(item);
					}
				}
			}

			if (!bFlag) {
				// parsing fail
				errnum = LOCAL_ERROR_CODE_PARSEFAIL;
				errmsg = LOCAL_ERROR_CODE_PARSEFAIL_DESC;

				FileLog("httprequest", "RequestOtherController::OnlineCountCallbackHandle() parsing fail:"
						"(url:%s, size:%d, buf:%s)",
						url.c_str(), size, buf);
			}
		}
	}
	else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( m_Callback.onRequestOtherOnlineCount != NULL ) {
		m_Callback.onRequestOtherOnlineCount(requestId, bFlag, errnum, errmsg, countList);
	}
}

long RequestOtherController::UploadCrashLog(const string& deviceId, const string& file) {
	HttpEntiy entiy;

	if ( deviceId.length() > 0 ) {
		entiy.AddContent(OTHER_UPLOAD_CRASH_DEVICEID, deviceId);
	}

	if ( file.length() > 0 ) {
		entiy.AddFile(OTHER_UPLOAD_CRASH_CRASHFILE, file, "application/x-zip-compressed");
	}

	string url = OTHER_UPLOAD_CRASH_PATH;

	FileLog("httprequest", "RequestOtherController::UploadCrashLog( "
			"url : %s, "
			"deviceId : %s, "
			"file : %s "
			")",
			url.c_str(),
			deviceId.c_str(),
			file.c_str()
			);

	return StartRequest(url, entiy, this);
}

void RequestOtherController::UploadCrashLogCallbackHandle(long requestId, const string& url, bool requestRet, const char* buf, int size) {
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;

	if (requestRet) {
		// request success
		Json::Value dataJson;
		bFlag = HandleResult(buf, size, errnum, errmsg, &dataJson);
	}
	else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( m_Callback.onRequestOtherUploadCrashLog != NULL ) {
		m_Callback.onRequestOtherUploadCrashLog(requestId, bFlag, errnum, errmsg);
	}
}
