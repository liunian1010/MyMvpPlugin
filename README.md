# MVPPlugin
Android studio插件，帮助快速生成MVP架构
[使用教程](http://yugai.github.io/2017/02/27/AndroidStudio-MVPPlugin/#more)
</br> 以上为原作者说明（尊重作者，保留他留下的东西），以下为我做的修改（自动功能过于单一，所以我新增了我想要的功能）
</br> 2017/10/27 新增onCreate方法生成和layout布局文件生成，修改P层逻辑，省略使用时的mView==null判断
</br> 2017/11/1 新增读取app包名功能，并自动添加R文件的引用到Activity中，新增自动注册Activity到AndroidMainfest.xml中（MVPPlugin.zip不用解压，安装方式和之前的一样，直接硬盘安装即可。为什么不是jar结尾呢，是因为引入了Dom4j，所以打包出来的是Zip）