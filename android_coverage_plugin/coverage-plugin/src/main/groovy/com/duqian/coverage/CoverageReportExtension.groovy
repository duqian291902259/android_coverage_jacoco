package com.duqian.coverage
/**
 * Description:相关配置、过滤规则
 * @author n20241 Created by 杜小菜 on 2021/9/10 - 18:29 .
 * E-mail: duqian2010@gmail.com
 */
class CoverageReportExtension {

    ReportConfiguration csv
    ReportConfiguration html
    ReportConfiguration xml
    //生成报告的目录
    String destination
    //风味
    String flavorName = "debug"

    CoverageReportExtension() {
        this.html = new ReportConfiguration(true)
        this.csv = new ReportConfiguration(false)
        this.xml = new ReportConfiguration(false)
        this.destination = null
    }

    public static final Collection<String> thirdLibExcludes =
            ['**/io/realm/**.class',
             '**/site/duqian/**.*',
             '**/*_Factory.class',
            ].asImmutable()

    public static final Collection<String> androidDataBindingExcludes =
            ['android/databinding/**/*.class',
             'androidx/databinding/**/*.class',
             '**/android/databinding/*Binding.class',
             '**/androidx/databinding/*Binding.class',
             '**/DataBindingInfo.class',
             '**/DataBinderMapperImpl**.class',
             '**/DataBinderMapperImpl**.class',
             '**/BR.*'].asImmutable()

    public static final Collection<String> androidExcludes =
            ['**/R.class',
             '**/R$*.class',
             '**/BuildConfig.*',
             '**/*JavascriptBridge.class',
             '**/Manifest*.*'].asImmutable()

    public static final Collection<String> defaultExcludes = (thirdLibExcludes + androidDataBindingExcludes + androidExcludes).asImmutable()

    public static final Collection<String> srcIncludes =
            ['**.java',
             '**.kt',
             '**/**.java', '**/**.kt'
            ].asImmutable()

    public static Collection<String> srcExcludes = []

    public static final Collection<String> defaultSrcIncludes = (srcIncludes).asImmutable()
    public static final Collection<String> defaultSrcExcludes = (getSrcExcludes()).asImmutable()

    private static Collection<String> getSrcExcludes() {
        if (srcExcludes.size() <= 0) {
            List<String> srcExcludeList = new ArrayList<>()
            defaultExcludes.forEach {
                def excludeSrc = it.replace(".class", ".java")
                //println("dq-jacoco excludeSrc="+excludeSrc)
                srcExcludeList.add(excludeSrc)
            }
            srcExcludes = srcExcludes.asCollection()
        }
        return srcExcludes
    }

    //jacoco开关
    boolean isJacocoEnable = true
    boolean isDiffJacoco = false //是否差量更新

    //需要对比class的分支名,默认master
    String branchName = "master"
    //需要插桩的文件
    List<String> includes

    //String gitPushShell = "${project.projectDir}/shell/gitPushShell.sh" //commit&push命令
    //String pullDiffClassShell = "${project.projectDir}/shell/pullDiffClass.sh" //获取差异class

    //git-bash的路径，如果找不到，自行配置
    private String gitBashPath
    String packageName = "site.duqian"

    String getGitBashPath() {
        if (gitBashPath == null || gitBashPath.isEmpty()) {
            Process process = 'where git'.execute()
            String path = process.inputStream.text
            process.closeStreams()
            String[] paths = path.split('\n')
            String temp = ''
            paths.each {
                try {
                    File file = new File(it)
                    def parentFile = file.getParentFile()
                    if (parentFile != null) {
                        File gitBash = new File(parentFile.getParent() + File.separator + 'git-bash.exe')
                        println("dq-jacoco GitBashPath:$gitBash exist:${gitBash.exists()}")
                        if (gitBash.exists()) {
                            temp = gitBash.absolutePath
                            return temp
                        }
                    }
                } catch (Exception e) {
                    println("dq-jacoco GitBashPath:$e")
                }
            }
            return temp
        }
        return gitBashPath
    }
}

