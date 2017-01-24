{
	'includes' : [
		'build/common.gypi',
	],
	'target_defaults' : {
		'include_dirs' : [
			'src',
			'src/taffcomm',
			'src/libparse',
		],
		'defines' : [
			'TAF_VERSION="3.2.1.4"',
		],
	},
	'targets' : [
		{
			'target_name' : 'jce2java',
			'type' : 'executable',
			'sources' : [
				'src/generator/main.cpp',
				'src/generator/jce2java.cpp',
				'src/generator/jce2java.h',
				'src/generator/interface_analysis.cpp',
				'src/generator/interface_analysis.h',
				'src/generator/jce_filter.cpp',
				'src/generator/jce_filter.h'
			],
			'dependencies' : [
				'jceparse',
			]
		},
		{
			'target_name' : 'taffcomm',
			'type' : 'static_library',
			'sources' : [
				'src/taffcomm/util/tc_common.cpp',
				'src/taffcomm/util/tc_common.h',
				'src/taffcomm/util/tc_ex.cpp',
				'src/taffcomm/util/tc_ex.h',
				'src/taffcomm/util/tc_file.cpp',
				'src/taffcomm/util/tc_file.h',
				'src/taffcomm/util/tc_loki.h',
				'src/taffcomm/util/tc_md5.cpp',
				'src/taffcomm/util/tc_md5.h',
				'src/taffcomm/util/tc_option.cpp',
				'src/taffcomm/util/tc_option.h',
			],
		},
		{
			'target_name' : 'jceparse',
			'type' : 'static_library',
			'sources' : [
				'src/libparse/parse/element.h',
				'src/libparse/parse/parse.h',
				'src/libparse/element.cpp',
				'src/libparse/parse.cpp',
				'<(INTERMEDIATE_DIR)/lex.yy.cpp',
				'<(INTERMEDIATE_DIR)/jce.tab.cpp',
				'<(INTERMEDIATE_DIR)/jce.tab.hpp',
			],
			'include_dirs' : [
				'<(INTERMEDIATE_DIR)'
			],
			'conditions' : [
				['OS=="win"', {
					'actions' : [ 
						{
							'action_name' : 'flex',
							'inputs' : [
								'src/libparse/jce.l',
							],
							'outputs' : [
								'<(INTERMEDIATE_DIR)/lex.yy.cpp',
							],
							'action' : ['flex', '--nounistd', '--outfile=<(INTERMEDIATE_DIR)/lex.yy.cpp', '<@(_inputs)'],
						},
						{
							'action_name' : 'bison',
							'inputs' : [
								'src/libparse/jce.y',
							],
							'outputs' : [
								'<(INTERMEDIATE_DIR)/jce.tab.cpp',
								'<(INTERMEDIATE_DIR)/jce.tab.hpp',
							],
							'action' : ['bison', '-y',  '-o' '<(INTERMEDIATE_DIR)/jce.tab.cpp', '<@(_inputs)'],
						}
				]} , {
					'actions' : [
						{
							'action_name' : 'lex',
							'inputs' : [
								'src/libparse/jce.l',
							],
							'outputs' : [
								'<(INTERMEDIATE_DIR)/lex.yy.cpp',
							],
							'action' : ['lex', '--nounistd', '--outfile=<(INTERMEDIATE_DIR)/lex.yy.cpp', '<@(_inputs)'],
						},
						{
							'action_name' : 'yacc',
							'inputs' : [
								'src/libparse/jce.y',
							],
							'outputs' : [
								'<(INTERMEDIATE_DIR)/jce.tab.cpp',
								'<(INTERMEDIATE_DIR)/jce.tab.hpp',
							],
							'action' : ['yacc', '-o' '<(INTERMEDIATE_DIR)/jce.tab.cpp', '<@(_inputs)'],
						},
					],
				}],
			],
			'dependencies' : [
				'taffcomm'
			]
		},
	]
}