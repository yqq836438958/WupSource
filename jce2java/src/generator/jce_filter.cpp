/*
 * jce_filter.cpp
 *
 *  Created on: 2015-7-31
 *      Author: wileywang
 */
#include "jce_filter.h"

JceFilter::JceFilter() {
	addSid("TRom::E_ROM_DEVICE_TYPE");
	addSid("TRom::EAPNTYPE");
	addSid("TRom::EIPType");
	addSid("TRom::ELOGINRET");
	addSid("TRom::ENETTYPE");
	addSid("TRom::IPListReq");
	addSid("TRom::IPListRsp");
	addSid("TRom::JoinIPInfo");
	addSid("TRom::LoginReq");
	addSid("TRom::LoginRsp");
	addSid("TRom::RomBaseInfo");
	addSid("TRom::SECPROXY_RETCODE");
	addSid("TRom::SecureReq");
	addSid("TRom::SecureRsp");
}

void JceFilter::addSid(const std::string& sid) {
	mFilterJceSidMap.insert(std::pair<std::string, bool>(sid, true));
}

void JceFilter::filterStructs(
		const std::map<std::string, StructPtr>& orignalMap
		, std::map<std::string, StructPtr>& resultMap) const {
	for (std::map<std::string, StructPtr>::const_iterator it = orignalMap.begin()
			; it != orignalMap.end(); ++it) {
		if (mFilterJceSidMap.find(it->first) != mFilterJceSidMap.end()) {
			continue;
		}
		resultMap.insert(std::pair<std::string, StructPtr>(it->first, it->second));
	}
}

void JceFilter::filterEnums(
		const std::map<std::string, EnumPtr>& orignalMap
		, std::map<std::string, EnumPtr>& resultMap) const {
	for (std::map<std::string, EnumPtr>::const_iterator it = orignalMap.begin()
			; it != orignalMap.end(); ++it) {
		if (mFilterJceSidMap.find(it->first) != mFilterJceSidMap.end()) {
			continue;
		}
		resultMap.insert(std::pair<std::string, EnumPtr>(it->first, it->second));
	}
}
