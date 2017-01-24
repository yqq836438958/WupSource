/*
 * jce_filter.h
 *
 *  Created on: 2015-7-31
 *      Author: wileywang
 */

#ifndef JCE_FILTER_H_WILEY
#define JCE_FILTER_H_WILEY

#include "parse/parse.h"
#include <map>

class JceFilter {
public:
	JceFilter();

	void filterStructs(const std::map<std::string, StructPtr>& orignalMap
			, std::map<std::string, StructPtr>& resultMap) const;
	void filterEnums(const std::map<std::string, EnumPtr>& orignalMap
			, std::map<std::string, EnumPtr>& resultMap) const;

private:
	void addSid(const std::string& sid);

	std::map<std::string, bool> mFilterJceSidMap;
};



#endif /* JCE_FILTER_H_ */
