/*
 * interface_analysis.h
 *
 * 用于分析以及列出，接口中所有依赖的枚举，结构体
 *
 *  Created on: 2015-7-31
 *      Author: wileywang
 */

#ifndef INTERFACE_ANALYSIS_H_
#define INTERFACE_ANALYSIS_H_

#include "parse/parse.h"
#include <map>
#include <vector>

class InterfaceAnalysis {
public:
	InterfaceAnalysis();

	const std::map<std::string, StructPtr>& getAllStructs() const;
	const std::map<std::string, EnumPtr>& getAllEnums() const;
	const std::map<std::string, ConstPtr>& getAllConsts() const;

	void analysis(const InterfacePtr& interfacePtr);
	void analysis(const vector<InterfacePtr>& interfacePtrs);

private:
	void analysis(const StructPtr& structPtr);
	void analysis(const TypePtr& typePtr);

private:
	std::map<std::string, StructPtr> mAllStructs;
	std::map<std::string, EnumPtr> mAllEnums;
	std::map<std::string, ConstPtr> mAllConsts;
};



#endif /* INTERFACE_ANALYSIS_H_ */
