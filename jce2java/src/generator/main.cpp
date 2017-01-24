#include "util/tc_option.h"
#include "util/tc_file.h"
#include "jce2java.h"

void usage()
{
    cout << "Usage : jce2java [OPTION] jcefile" << endl;
    cout << "  jce2java support type: bool byte short int long float double vector map" << endl;
    cout << "supported [OPTION]:" << endl;
    cout << "  --help                           help,print this" << endl;
    cout << "  --dir=DIRECTORY                  generate java file to DIRECTORY(default to current)" << endl;
    cout << "  --base-package=NAME              package prefix, default(com.qq.)" << endl;
    cout << "  --with-servant                   also generate servant class" << endl;
    cout << "  --with-wsp                       also generate wsp class " << endl;
	cout << "  --with-async                     generate ///@async tag" << endl;
	cout << "  --force-array                    default changed byte vector to list, use this for byte[]" << endl;
    cout << "  --no-holder                      don't create Holder class" << endl;
    cout << "  --check-default=<true,false>     optional field not package(default package)" << endl;
    cout << "  --extends-package=NAME           set the extends package name"<< endl;
    cout << "  --with-charset                   set charset, default GBK" << endl;
    cout << "  --with-JavaBeanRule              support javabeab, default not support" << endl;
    cout << "  --with-compact                   no className(), fullClassName(), get(),set(), clone(), equals(), hashCode(), display() method" << endl;
    cout << "  --enum-compact                   int for enums" << endl;

    cout << "  --generate-interface-dependencies  generate interface dependencies" << endl;
    cout << "  --filter-rom-jce                 extends jce struct for rom jce" << endl;
    cout << "  --no-prx                         no prx generate for interface" << endl;
	cout << "  --with-stubandroid               generate XXXStubAndroid File" << endl;
	cout << "  --gen-android                    fast option for generate android files " << endl;

    cout << endl;
    exit(0);
}

void check(vector<string> &vJce)
{
    for(size_t i  = 0; i < vJce.size(); i++)
    {
        string ext  = taf::TC_File::extractFileExt(vJce[i]);
        if(ext == "jce")
        {
            if(!taf::TC_File::isFileExist(vJce[i]))
            {
                cerr << "file '" << vJce[i] << "' not exists" << endl;
				usage();
                exit(0);
            }
        }
        else
        {
            cerr << "only support jce file." << endl;
            exit(0);
        }
    }
}

int main(int argc, char* argv[])
{
    if(argc < 2)
    {
        usage();
    }

    taf::TC_Option option;
    option.decode(argc, argv);
    vector<string> vJce = option.getSingle();

    check(vJce);

    if(option.hasParam("help"))
    {
        usage();
    }

    Jce2Java j2j;

    //�Ƿ������taf��ͷ
    g_parse->setTaf(option.hasParam("with-taf"));

    //��������ļ��ĸ�Ŀ¼
    if(option.getValue("dir") != "")
    {
        j2j.setBaseDir(option.getValue("dir"));
    }
    else
    {
        j2j.setBaseDir(".");
    }

    //����ǰ׺
    if(option.hasParam("base-package"))
    {
        j2j.setBasePackage(option.getValue("base-package"));
    }
    else
    {
        j2j.setBasePackage("com.qq.");
    }

    //�Ƿ���ɷ������,Ĭ�ϲ����
    if(option.hasParam("with-servant"))
    {
        j2j.setWithServant(true);
    }
    else
    {
        j2j.setWithServant(false);
    }

    //�Ƿ����wsp
    if(option.hasParam("with-wsp"))
    {
        j2j.setWithWsp(true);
    }
    else
    {
        j2j.setWithWsp(false);
    }


	//�Ƿ�����첽��ǩ
	if (option.hasParam("with-async"))
	{
		j2j.setWithAysnc(true);
	}
	else
	{
		j2j.setWithAysnc(false);
	}

	//ǿ��ת�������ѡ��
	if (option.hasParam("force-array"))
	{
		j2j.setForceArray(true);
	}
	else
	{
		j2j.setForceArray(false);
	}

    //���ð���
    if (option.hasParam("extends-package"))
    {
        j2j.setTafPacket(option.getValue("extends-package"));
    }

    j2j.setHolder(!option.hasParam("no-holder"));
    //j2j.setCheckDefault(taf::TC_Common::lower(option.getValue("check-default")) == "false"?false:true);
    //modify by edwardsu
    //Ĭ��optional���ȫ��������
    j2j.setCheckDefault(taf::TC_Common::lower(option.getValue("check-default")) == "true"?true:false);


    if (option.hasParam("with-charset"))
    {
        j2j.setCharset(option.getValue("with-charset"));
    }
	else
	{
		j2j.setCharset("GBK");
	}

	//�Ƿ�֧��JavaBean�淶
    if(option.hasParam("with-JavaBeanRule"))
    {
        j2j.setWithJbr(true);
    }
    else
    {
        j2j.setWithJbr(false);
    }

	//�Ƿ�ʹ�þ����ʽ������android�ͻ���
    //��ɾ���Ĵ��룬ȥ�����Ե�get()��set()��  ��clone()��equals()��hashCode()��display()����
    if(option.hasParam("with-compact"))
    {
        j2j.setWithCompact(true);
    }
    else
    {
        j2j.setWithCompact(false);
    }


    if(option.hasParam("enum-compact"))
    {
        j2j.setEnumCompact(true);
    }
    else
    {
        j2j.setEnumCompact(false);
    }

    if (option.hasParam("with-stubandroid")) {
    	j2j.setWithStubAndroid(true);
    } else {
    	j2j.setWithStubAndroid(false);
    }

    if (option.hasParam("generate-interface-dependencies")) {
    	j2j.setWithGenerateInterfaceDependencies(true);
    } else {
    	j2j.setWithGenerateInterfaceDependencies(false);
    }

    if (option.hasParam("filter-rom-jce")) {
    	j2j.setWithFilterRomJce(true);
    } else {
    	j2j.setWithFilterRomJce(false);
    }

    if (option.hasParam("gen-android")) {
    	j2j.setWithStubAndroid(true);
    	j2j.setBasePackage("");
    	j2j.setHolder(false);
    	j2j.setWithPrx(false);
    	j2j.setWithGenerateInterfaceDependencies(true);
    	j2j.setWithFilterRomJce(true);
    }

	try
	{
	    for(size_t i = 0; i < vJce.size(); i++)
	    {
	        g_parse->parse(vJce[i]);
	        j2j.createFile(vJce[i]);
	    }
	}catch(exception& e)
	{
		cerr<<e.what()<<endl;
	}

    return 0;
}

