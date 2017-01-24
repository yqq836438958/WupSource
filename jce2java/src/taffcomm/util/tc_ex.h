#ifndef __TC_EX_H
#define __TC_EX_H

#include <string>
#include <stdexcept>
using namespace std;

namespace taf
{
/////////////////////////////////////////////////
/** 
* @file  tc_ex.h 
* @brief �쳣�� 
* @author  jarodruan@tencent.com  
*/           
/////////////////////////////////////////////////

/**
* @brief �쳣��.
*/
class TC_Exception : public exception
{
public:
    /**
	 * @brief ���캯���ṩ��һ�����Դ���errno�Ĺ��캯�� 
	 *  
	 *  	  �쳣�׳�ʱֱ�ӻ�ȡ�Ĵ�����Ϣ
	 *  
	 * @param buffer �쳣�ĸ澯��Ϣ 
     */
	explicit TC_Exception(const string &buffer);

    /**
	 * @brief ���캯��,�ṩ��һ�����Դ���errno�Ĺ��캯�� 
	 *  
	 *  	  �쳣�׳�ʱֱ�ӻ�ȡ�Ĵ�����Ϣ
	 *  
     * @param buffer �쳣�ĸ澯��Ϣ 
     * @param err    ������, ����strerror��ȡ������Ϣ
     */
	TC_Exception(const string &buffer, int err);

    /**
     * @brief ������
     */
    virtual ~TC_Exception() throw();

    /**
     * @brief ������Ϣ.
     *
     * @return const char*
     */
    virtual const char* what() const throw();

    /**
     * @brief ��ȡ������
     * 
     * @return �ɹ���ȡ����0
     */
    int getErrCode() { return _code; }

private:
    void getBacktrace();

private:
    /**
	 * �쳣�������Ϣ
     */
    string  _buffer;

	/**
	 * ������
     */
    int     _code;

};

}
#endif

