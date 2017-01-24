#ifndef _JCE2JAVA_H
#define _JCE2JAVA_H

#include "parse/parse.h"

#include <cassert>
#include <string>

#define JCE_PACKAGE     ".taf.jce"
#define PROXY_PACKAGE   ".taf.proxy"
#define HOLDER_PACKAGE  ".taf.holder"
#define SERVER_PACKAGE  ".taf.server"
#define WUP_PACKAGE     ".jce.wup"
#define WSP_PACKAGE     ".tac2.wsp.build.WspStruct"


/**
 * ���jce���java�ļ�
 * �����ṹ�ı�����Լ����Proxy��Servant
 */
class Jce2Java
{
public:
    Jce2Java();

    /**
     * ���ô�����ɵĸ�Ŀ¼
     * @param dir
     */
    void setBaseDir(const string &dir);

    /**
     * ���ð�ǰ׺
     * @param prefix
     */
    void setBasePackage(const string &prefix);

    /**
     * �����Ƿ���Ҫ����˴���
     */
    void setWithServant(bool bWithServant) { _bWithServant = bWithServant;}

    /**
     * �����Ƿ�֧��wsp
     */
    void setWithWsp(bool bWsp) { _bWithWsp = bWsp;}

	/**
	* �����Ƿ����///@Async��ǩ
	*/
	void setWithAysnc(bool bAsync) { _bWithAsync = bAsync; }

	/**
	* Ĭ�Ͻ���byte��vectorתlist ���ϸò����ת����
	*/
	void setForceArray(bool bArray) { _bForceArray = bArray; }

	/**
	 * �����Ƿ����Holder��
	 *
	 * @author kevintian (2010-9-3)
	 *
	 * @param bHolder
	 */
	void setHolder(bool bHolder) { _bHolder = bHolder; }


    void setCheckDefault(bool bCheck) { _bCheckDefault = bCheck; }

	/*֧���Զ����ַ�by johnson*/
    void setCharset(string charset) { _sCharset = charset; }
	/*֧��javabean�淶��by johnson*/
    void setWithJbr(bool bJbr) { _bWithJbr = bJbr;}
	/*ȥ�����Ե�read write������ cloneable��equals��hashCode��clone��display*/
    void setWithCompact(bool bCompact) { _bWithCompact = bCompact;}

	/*���ö����ʱ������int��ʽʵ��*/
    void setEnumCompact(bool bCompact) { _bEnumCompact = bCompact;}

    void setWithPrx(bool bWithPrx) { _bWithPrx = bWithPrx; }
    void setWithGenerateInterfaceDependencies(bool bWithGenerateInterfaceDependencies) {
    	_bWithGenerateInterfaceDependencies = bWithGenerateInterfaceDependencies;
    }
    void setWithFilterRomJce(bool bWithFilterRomJce) {
    	_bWithFilterRomJce = bWithFilterRomJce;
    }
    void setWithStubAndroid(bool bWithStubAndroid) { _bWithStubAndroid = bWithStubAndroid; }

    /**
     * ���
     * @param file
     * @param isFramework �Ƿ��ǿ��
     */
    void createFile(const string &file);

    /**
     * ����TAF��ı���
     */
    void setTafPacket(const std::string & sPacket)
    {
        s_JCE_PACKAGE 		= sPacket + JCE_PACKAGE;
        s_PROXY_PACKAGE		= sPacket + PROXY_PACKAGE;
        s_HOLDER_PACKAGE	= sPacket + HOLDER_PACKAGE;
        s_SERVER_PACKAGE	= sPacket + SERVER_PACKAGE;
        s_WUP_PACKAGE		= sPacket + WUP_PACKAGE;
        s_WSP_PACKAGE		= sPacket + WSP_PACKAGE;
    }



protected:
    /**
     * �������ռ��ȡ�ļ�·��
     * @param ns ����ռ�
     *
     * @return string
     */
    string getFilePath(const string &ns) const;

    string 	_packagePrefix;
    string 	_baseDir;
    bool   	_bWithServant;
    bool  	_bWithWsp;
	bool  	_bWithAsync;
	bool  	_bForceArray;
	bool  	_bHolder;
    bool    _bCheckDefault;
    string  _sCharset;
	bool    _bWithJbr;
	bool    _bWithCompact;
	bool    _bEnumCompact;

	// added by wileywang
	bool	_bWithPrx;
	bool	_bWithStubAndroid;
	bool	_bWithGenerateInterfaceDependencies;
	bool	_bWithFilterRomJce;
    //�����Ǳ�����Դ�����
protected:

    /**
     * ���ĳ���͵Ľ���Դ��
     * @param pPtr
     *
     * @return string
     */
    string writeTo(const TypeIdPtr &pPtr) const;

    /**
     * ���ĳ���͵ı���Դ��
     * @param pPtr
     *
     * @return string
     */
    string readFrom(const TypeIdPtr &pPtr) const;

    /**
     *
     * �������Ŀǰ���ò�����
     * @param pPtr
     *
     * @return string
     */
    string display(const TypeIdPtr &pPtr) const;

    //����������������Դ�����
protected:

    /*
     * ���ĳ���͵ĳ�ʼ���ַ�
     * @param pPtr
     *
     * @return string
     */
    string toTypeInit(const TypePtr &pPtr) const;

    /**
     * ���ĳ���͵Ķ�Ӧ������ַ�����Դ��
     * @param pPtr
     *
     * @return string
     */
    string toObjStr(const TypePtr &pPtr) const;

    /**
     * �ж��Ƿ��Ƕ�������
     */
    bool isObjType(const TypePtr &pPtr) const;

    /**
     * ���ĳ���͵��ַ�����Դ��
     * @param pPtr
     *
     * @return string
     */
    string tostr(const TypePtr &pPtr) const;

    /**
     * ����ڽ����͵��ַ�Դ��
     * @param pPtr
     *
     * @return string
     */
    string tostrBuiltin(const BuiltinPtr &pPtr) const;
    /**
     * ���vector���ַ�����
     * @param pPtr
     *
     * @return string
     */
    string tostrVector(const VectorPtr &pPtr) const;

    /**
     * ���map���ַ�����
     * @param pPtr
     *
     * @return string
     */
    string tostrMap(const MapPtr &pPtr, bool bNew = false) const;

    /**
     * ���ĳ�ֽṹ�ķ�����
     * @param pPtr
     *
     * @return string
     */
    string tostrStruct(const StructPtr &pPtr) const;

    /**
     * ���ĳ��ö�ٵķ�����
     * @param pPtr
     *
     * @return string
     */
    string tostrEnum(const EnumPtr &pPtr) const;

    /**
     * ������ͱ����Ľ���Դ��
     * @param pPtr
     *
     * @return string
     */
    string decode(const TypeIdPtr &pPtr) const;

    /**
     * ������ͱ����ı���Դ��
     * @param pPtr
     *
     * @return string
     */
    string encode(const TypeIdPtr &pPtr) const;

    //������h��java�ļ��ľ������
protected:
    /**
     * �ṹ��md5
     * @param pPtr
     *
     * @return string
     */
    string MD5(const StructPtr &pPtr) const;

    /**
     * ��ɽṹ��Holder�࣬�������ô���
     * @param pPtr
     * @param nPtr
     *
     * @return string
     */
    string generateHolder(const StructPtr &pPtr, const NamespacePtr &nPtr) const;

    /**
     * ����Ĭ��Ԫ������ʶ��map/list����
     * @param pPtr
     * @param sElemName Ԫ�����
     *
     * @return string
     */
    string generateDefautElem(const TypePtr &pPtr, const string & sElemName) const;

    /**
     * ��ɽṹ��java�ļ�����
     * @param pPtr
     *
     * @return string
     */
    string generateJava(const StructPtr &pPtr, const NamespacePtr &nPtr) const;

    /**
     * ���������javaԴ��
     * @param pPtr
     *
     * @return string
     */
    string generateJava(const ContainerPtr &pPtr) const;

    /**
     * ��ɲ���������java�ļ�����
     * @param pPtr
     *
     * @return string
     */
    string generateJava(const ParamDeclPtr &pPtr) const;

    /**
     * ��ɲ���holder��java�ļ�����
     * @param pPtr
     *
     * @return string
     */
    string generateHolder(const ParamDeclPtr &pPtr) const;

    /**
     * �����������proxy��java�ļ�����
     * @param pPtr
     * @param cn
     *
     * @return string
     */
    string generateJava(const OperationPtr &pPtr, const string &cn) const;

    /**
     * ��ɲ���java�ļ�������÷ַ���Դ��
     * @param pPtr
     * @param cn
     *
     * @return string
     */
    string generateDispatchJava(const OperationPtr &pPtr, const string &cn) const;


    /**
     * ��ɽӿڵ�java�ļ���Դ��
     * @param pPtr
     * @param nPtr
     *
     * @return string
     */
    string generateJava(const InterfacePtr &pPtr, const NamespacePtr &nPtr) const;

    /**
     * ���Proxy�ӿڵ�java�ļ���Դ��
     * @param pPtr
     * @param nPtr
     *
     * @return string
     */
    string generatePrx(const InterfacePtr &pPtr, const NamespacePtr &nPtr) const;

    /**
     * ���Proxy�������java�ļ���Դ��
     * @param pPtr
     * @param nPtr
     *
     * @return string
     */
    string generatePrxHelper(const InterfacePtr &pPtr, const NamespacePtr &nPtr) const;

    /**
     * ���Proxy�ص����java�ļ���Դ��
     * @param pPtr
     * @param nPtr
     *
     * @return string
     */
    string generatePrxCallback(const InterfacePtr &pPtr, const NamespacePtr &nPtr) const;

    /**
     * ��ɷ��������java�ļ���Դ��
     * @param pPtr
     * @param nPtr
     *
     * @return string
     */
    string generateServant(const InterfacePtr &pPtr, const NamespacePtr &nPtr) const;

    // added by wileywang
    // for android
    string generateConstructor(const TypePtr& vType) const;
    string generateAndroidJavaParams(const vector<ParamDeclPtr>& vParamDecl, bool needParamType, bool needOutParam) const;
    string generateAndroidStub(const InterfacePtr &pPtr, const NamespacePtr &nPtr) const;
    // added by wileywang end

    /**
     * ���ö�ٵ�ͷ�ļ�Դ��
     * @param pPtr
     *
     * @return string
     */
    string generateJava(const EnumPtr &pPtr, const NamespacePtr &nPtr) const;

    /**
     * ��ɳ���javaԴ��
     * @param pPtr
     *
     * @return string
     */
    void generateJava(const ConstPtr &pPtr, const NamespacePtr &nPtr) const;

    /**
     * ������ֿռ�java�ļ�Դ��
     * @param pPtr
     *
     * @return string
     */
    void generateJava(const NamespacePtr &pPtr) const;

    /**
     * ���ÿ��jce�ļ���java�ļ�Դ��
     * @param pPtr
     *
     * @return string
     */
    void generateJava(const ContextPtr &pPtr) const;

private:
    std::string s_JCE_PACKAGE;
    std::string s_PROXY_PACKAGE;
    std::string s_HOLDER_PACKAGE;
    std::string s_SERVER_PACKAGE;
    std::string s_WUP_PACKAGE;
    std::string s_WSP_PACKAGE;
};

#endif


