# Coverage_JacocoWebServer
Code coverage platform for Android project.
Base on jacoco,SpringBoot,Vue,java,kotlin....

项目地址：[https://github.com/duqian291902259/android_coverage_jacoco](https://github.com/duqian291902259/android_coverage_jacoco)


1. web-vue 前端工程
1. src后端逻辑
1. docker：mvn插件自动生成镜像的目录
1. 根目录的Dockerfile是使用cmd命令操作build、run镜像


### 概述
代码覆盖率（Code coverage）是软件测试中的一种度量，描述程序中原始码被测试的比例和程度，所得比例称为代码覆盖率。 代码覆盖是由系统化软件测试所派生的方式。代码覆盖率统计，主要用于测试人员进行功能测试、集成测试、回归测试等场景，以及研发人员的开发自测后查看覆盖率情况。
通过自研，开发了一套代码覆盖率报告管理系统，该系统具备收集移动端覆盖率信息，并实现全量、增量覆盖率报告的生成与管理功能。

### 解决的问题
1.	统一覆盖率报告的生成与管理，管理平台提供多平台支持。
2.	支持生成全量、增量的覆盖率报告。
3.	支持按分支提交点生成覆盖率报告，支持在线预览报告和下载报告。
4.	报告的检索与预览便捷，降低研发与QA的测试成本。
5.	系统易集成，可拓展到其他产品或者其他终端。
### 覆盖率统计平台的意义
1.	满足了QA、研发人员对全量、增量覆盖率统计的需要。
2.	平台化，屏蔽各端生成报告的流程差异，让流程更加自动、便捷。
3.	分析未覆盖部分的代码，从而反推程序逻辑、测试用例是否合理。
4.	及时发现程序中的冗余逻辑，提升代码质量。
5.	对测试过程提供有效的数据支持。

### 现有方案对比
覆盖率报告的生成，有很多现有的实现方案，现状如下：
1.	不同编程语言有各自的实现方案，生成流程和表现形式良莠不齐。
2.	各端本地生成静态报告，缺乏系统性，不方便统一管理。
3.	开发集成的覆盖率工具，适用于开发人员，不符合QA的工作流程。
4.	本地开发阶段开启覆盖率功能，很耗性能和时间，无法大规模应用。

通过研究，Java语言比较成熟的覆盖率统计实现方案有Jacoco，但是在Android大型项目的落地方面，有很多问题需要处理解决。
经过不断开发迭代，将Android覆盖率统计的功能，做了IDE环境生成和云端生成。云端生成，可将复杂且耗时的功能交给服务器处理，聚合各端的覆盖率报告，一定程度上解决了以上痛点。

### 技术思路

![技术实现思路](https://upload-images.jianshu.io/upload_images/2001922-7ae18caac64392ce.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

1)	客户端预处理。
各端在编译构建阶段，对项目中的代码插桩，插入探针，用于监测代码执行情况。

2)	打包构建，将编译后的相关产物上传到服务器。
如Android上传.class字节码文件和java文件，ios上传.gcno和.gcda文件，用于后续服务器根据不同的工具生成报告。

3)	安装&运行程序，收集覆盖率信息并上传到服务器。
安装插桩后的程序包，使用对应的功能（待测试的功能），记录代码运行的情况，并生成记录了覆盖率信息的文件，各端用统一的接口，上传文件到服务器，用于后续生成报告。

4)	web前端页面，提交表单，生成覆盖率报告并在线预览。
页面显示出当前支持的应用、平台、已经支持覆盖率功能的分支，以及生成报告对应的Git提交点等信息，用户安装好对应的客户端，上传好覆盖率文件后，选择对应平台的某个应用，提交请求，生成覆盖率报告。

5)	覆盖率server端，负责报告的生成和管理逻辑。
Server提供API接口给各端使用；提供已有覆盖率报告的管理功能：查询，预览，下载和删除等。
用户自定义生成报告的条件，系统可根据条件，生成全量或者增量覆盖率报告。增量报告通过git diff功能找出增量修改的代码文件，用于报告的过滤处理。
覆盖率报告生成的逻辑，基于各自平台的命令行工具完成。

### 技术实现
整体流程

![覆盖率统计的整体流程.png](https://upload-images.jianshu.io/upload_images/2001922-69660c62a5599edf.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### 一.	Coverage-Server
覆盖率后台服务，通过SpringBoot MVC实现。后端服务，根据不同平台覆盖率生成的逻辑差异，需要分别处理。
比如Android项目，是java编程语言实现，所以可以直接调用jacoco框架的jar包，通过report方法生成报告，需要传入生成报告所需的各种参数，命令为：java -jar execPath --classfilessrcPath --html $reportPath --encoding=utf8。
服务架构如图所示。

![coverage-server](https://upload-images.jianshu.io/upload_images/2001922-7f308049844ab197.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

后端提供文件上传和下载服务，android客户端收集的覆盖率信息（.ec文件），编译后上传的.src文件和.class文件，都是通过这个统一的接口上传，按应用名称、git提交点的commitId值等来命名文件目录，文件的保存路径的命名，依赖于后续web提交表单的内容，需要一一对应，方便检索到对应的文件。
生成覆盖率报告，依赖的文件在服务器都能找到时，就可以使用jacococli.jar生成对应的html报告，返回给前端展示。

#### 二.	Coverage-Web
前端展示报告的服务，是通过Node.js开启一个http-server服务，指定服务器某个文件路径，即可打开改路径下的静态页面。
表单页面使用Vue+Webpack实现，与后端逻辑分离，页面只是用来收集生成报告所需的条件，并请求后端的接口，待后端生成报告后，返回报告相关的数据，web前端展示报告即可。
页面包含覆盖率生成的条件表单编辑、提交功能，有已生成报告的在线预览、下载、管理界面。部分请求表单如下图所示：
![覆盖率系统前端界面](https://upload-images.jianshu.io/upload_images/2001922-780303401a6f6a7f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

选择对应的分支、两个提交点commitId，并勾选增量覆盖率，就可以通过使用gitlab的api，或者git diff命令，计算出两个提交点之间的diff文件列表，通过分支名和这些文件列表，去检索前面上传的.ec文件、src源码和class文件，从而针对这些增量修改的文件统计对应的覆盖率。如果不勾选增量覆盖率，就是统计所选分支项目代码的全量覆盖率。
![覆盖率报告管理界面.png](https://upload-images.jianshu.io/upload_images/2001922-768bb4248ea731d6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


#### 三.	客户端技术实现
以Android、Java工程为例，一般覆盖率报告的生成过程如下：
[图片上传失败...(image-d3a151-1650857287076)]

Android端，Java开发，采用Jacoco实现覆盖率报告，Off-line模式。
集成jacoco的plugin，在功能开启时，实现对自定义类的插桩，在debug包构建过程中，插入探针，便于记录每行代码执行情况。安装对应的apk，运行app，首次打开应用或者进入设置界面可dump覆盖率数据保存到本地存储目录，然后上传ec覆盖率文件给该系统对应的服务器。

如何实现插桩呢？
Java的Class初始化时，Jacoco初始化了一个Boolean类型的数组，用于记录每一个方法内部逻辑的执行情况，默认值是false表示没有执行，如果代码执行过，就会记录为true，这些探针会dump到本地文件保存下来。Android保存的覆盖率文件的拓展名为.ec文件，后续会上传到覆盖率平台。
下图，以一个源代码为例说明代码注入探针的过程：
![插入探针的过程.png](https://upload-images.jianshu.io/upload_images/2001922-6c02368ed2b3cd6f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

反编译代码，查看插桩后的代码逻辑，可以看到如下图所示的逻辑变化，构造函数初始化时，Jacoco也做了一些初始化的工作。
 [图片上传中...(image.png-271fd0-1650857113830-0)]

Jacoco包含了多种维度的覆盖率计数器：指令级计数器（C0 coverage）、分支级计数器（C1 coverage）、圈复杂度、行覆盖、方法覆盖、类覆盖等。 
生成的静态报告页面如下，可以查看对应的覆盖率数据和源码执行情况：
![image.png](https://upload-images.jianshu.io/upload_images/2001922-f5159c5debd3171e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

HTML静态报告，内容用不同的背景颜色区分，表示不同的覆盖情况。解释说明如下：
1.	绿色：表示行覆盖充分。
2.	红色：表示未覆盖的行。
3.	黄色棱形：表示分支覆盖不全。
4.	绿色棱形：表示分支覆盖完全。

### 应用场景
系统主要应用在以下使用场景：
1.新开发功能的分支测试
1）全部都跑一遍该分支新增全部功能的测试用例，收集各个功能点的覆盖率信息，计算出覆盖率。
2）修复bug后看覆盖率，只看当前分支的提交，对比修bug前的版本的提交记录，得出增量修改的代码并计算出覆盖率。
3）测试分支过程中同步了dev分支的代码，通过计算当前分支相比dev的修改部分，计算出覆盖率。
2.master分支全回归测试
1）全部回归完后看覆盖率，统一App版本的回归，可以看全量覆盖率。
2）修复bug后看覆盖率，前后对比diff，需要修改部分的代码覆盖率。 

QA根据覆盖率报告可针对性测试，并且发现一些程序bug或优化点。
有些逻辑永远执行不到，经核实是该类中无用的方法，如：
![image.png](https://upload-images.jianshu.io/upload_images/2001922-15ff8ec269a14cf7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

QA应用后反馈：平台优点是可以看测试过的覆盖率与代码增删是否一致，没有测试到改动处，可以再去测试。

### 方案效果
本覆盖率报告生成与管理的系统，带来的收益：
1)通过流程改造，解决了移动端覆盖率统计的多种难点。
2)端上自定义覆盖率统计的逻辑，实现增量覆盖率的统计。如Android通过Gradle插件开发，用于自定义class、src路径，并上传文件，再基于gitlab的api实现获取项目增量修改，从而满足全量、增量覆盖率统计的需求。
3)移动端项目，配合web前端和服务端，使得覆盖率系统链路完整，平台化，让整个流程更加规范、便捷、高效。
4)Web形式展示报告，方便各端人员查阅报告并分析未覆盖部分的代码，从而反推程序逻辑、测试用例是否合理，对测试过程提供有效的数据支持。


### 应用案例
可以在团中广泛应用，开发人员与测试人员都能根据代码覆盖率信息验证问题、发现问题并解决。

### 其他
DMEO工程为初期项目，但包含了Android端覆盖率统计平台化的思想，实际项目会有很大的改进和优化。仅供学习交流，如有问题，请联系杜小菜：duqian2010@gmail.com