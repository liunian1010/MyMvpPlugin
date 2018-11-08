package template;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA
 * User 13itch
 * Time 17/10/26.
 * 2018年11月8日14:33:04 去除默认空实现的DefaultView。改为动态代理
 */
public class AndroidMvpAction extends AnAction {
    Project project;
    VirtualFile selectGroup;

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getProject();
        String className = Messages.showInputDialog(project, "请输入类名称", "NewMvpGroup", Messages.getQuestionIcon());
        selectGroup = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        if (className == null || className.equals("")) {
            System.out.print("没有输入类名");
            return;
        }
        if (className.equals("mvp") || className.equals("MVP") || className.equals("Mvp")) {
            createMvpBase();
        } else {
            createClassMvp(className);
        }
        project.getBaseDir().refresh(false, true);
    }

    /**
     * 创建MVP的Base文件夹
     */
    private void createMvpBase() {
        String path = selectGroup.getPath() + "/mvp";
        String packageName = path.substring(path.indexOf("java") + 5, path.length()).replace("/", ".");

        String presenter = readFile("BasePresenter.txt").replace("&package&", packageName);
        String presenterImpl = readFile("BasePresenterImpl.txt").replace("&package&", packageName);
        String view = readFile("BaseView.txt").replace("&package&", packageName);
        String activity = readFile("MVPBaseActivity.txt").replace("&package&", packageName);
        String fragment = readFile("MVPBaseFragment.txt").replace("&package&", packageName);

        writetoFile(presenter, path, "BasePresenter.java");
        writetoFile(presenterImpl, path, "BasePresenterImpl.java");
        writetoFile(view, path, "BaseView.java");
        writetoFile(activity, path, "MVPBaseActivity.java");
        writetoFile(fragment, path, "MVPBaseFragment.java");

    }

    /**
     * 创建MVP架构
     */
    private void createClassMvp(String className) {
        boolean isFragment = className.endsWith("Fragment") || className.endsWith("fragment");
        if (className.endsWith("Fragment") || className.endsWith("fragment") || className.endsWith("Activity") || className.endsWith("activity")) {
            className = className.substring(0, className.length() - 8);
        }
        String mainPath = selectGroup.getPath().substring(0, selectGroup.getPath().indexOf("main") + 4);
        String ManifestPath = mainPath + "/AndroidManifest.xml";
        String R_Package = getPackage(ManifestPath);

        String layout = mainPath + "/res/layout/";
        String layout_contract = readFile("layout.xml");

        String path = selectGroup.getPath() + "/" + className.toLowerCase();
        String packageName = path.substring(path.indexOf("java") + 5, path.length()).replace("/", ".");
        String mvpPath = FileUtil.traverseFolder(path.substring(0, path.indexOf("java")));
        mvpPath = mvpPath.substring(mvpPath.indexOf("java") + 5, mvpPath.length()).replace("/", ".").replace("\\", ".");

        className = className.substring(0, 1).toUpperCase() + className.substring(1);

        System.out.print(mvpPath + "---" + className + "----" + packageName);

//        String defaultView = readFile("DefaultView.txt").replace("&package&", packageName).replace("&mvp&", mvpPath).replace("&Contract&", className + "Contract");
        String contract = readFile("Contract.txt").replace("&package&", packageName).replace("&mvp&", mvpPath).replace("&Contract&", className + "Contract");
        String presenter = readFile("Presenter.txt").replace("&package&", packageName).replace("&mvp&", mvpPath).replace("&Contract&", className + "Contract").replace("&Presenter&", className + "Presenter");

        if (isFragment) {
            String fragment = readFile("Fragment.txt")
                    .replace("&package&", packageName)
                    .replace("&mvp&", mvpPath)
                    .replace("&Fragment&", className + "Fragment")
                    .replace("&R_Package&", R_Package)
                    .replace("&Layout&", className.toLowerCase() + "_frg")
                    .replace("&Contract&", className + "Contract").replace("&Presenter&", className + "Presenter");

            writetoFile(fragment, path, className + "Fragment.java");
            writetoFile(layout_contract, layout, className.toLowerCase() + "_frg.xml");
        } else {
            String activity = readFile("Activity.txt")
                    .replace("&package&", packageName).replace("&mvp&", mvpPath)
                    .replace("&Activity&", className + "Activity")
                    .replace("&Contract&", className + "Contract")
                    .replace("&Presenter&", className + "Presenter")
                    .replace("&R_Package&", R_Package)
                    .replace("&Layout&", className.toLowerCase() + "_act");

            writetoFile(activity, path, className + "Activity.java");
            writetoFile(layout_contract, layout, className.toLowerCase() + "_act.xml");
            write2Xml(ManifestPath, packageName+"."+className + "Activity");
        }
//        writetoFile(defaultView, path, "Default" + className + "ContractView.java"); 已去除，改为动态代码
        writetoFile(contract, path, className + "Contract.java");
        writetoFile(presenter, path, className + "Presenter.java");
    }

    private String getAndroidManifestPath() {
        String path = selectGroup.getPath();
        if (!path.contains("main")) {
            return null;
        }
        path = path.substring(0, path.indexOf("main") + 4) + "/AndroidManifest.xml";
        return path;
    }


    //获取出包名
    public String getPackage(String path) {
        String RPackgage = null;
        try {
            //将src下面的xml转换为输入流
//            InputStream inputStream = new FileInputStream(new File("D:/ExcelFormat/strings.xml"));
            //InputStream inputStream = this.getClass().getResourceAsStream("/module01.xml");//也可以根据类的编译文件相对路径去找xml
            //创建SAXReader读取器，专门用于读取xml
            SAXReader saxReader = new SAXReader();
            //根据saxReader的read重写方法可知，既可以通过inputStream输入流来读取，也可以通过file对象来读取
            //Document document = saxReader.read(inputStream);
            Document document = saxReader.read(new File(path));//必须指定文件的绝对路径
            //另外还可以使用DocumentHelper提供的xml转换器也是可以的。
            //Document document = DocumentHelper.parseText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><modules id=\"123\"><module> 这个是module标签的文本信息</module></modules>");

            //获取根节点对象
            Element rootElement = document.getRootElement();
            RPackgage = rootElement.attribute("package").getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RPackgage;
    }

    public void write2Xml(String path, String act) {
        SAXReader saxReader = new SAXReader();
        try {
            Document doc = saxReader.read(new File(path));
            Element root = doc.getRootElement();
            Element childElement = root.element("application");
            Element actElement = childElement.addElement("activity");
            actElement.addAttribute("android:name", act);
            actElement.addAttribute("android:screenOrientation", "portrait");
            OutputFormat opf = new OutputFormat("\t", true, "UTF-8");
            opf.setTrimText(true);
            XMLWriter writer = new XMLWriter(new FileOutputStream(new File(path)), opf);
            writer.write(doc);
            writer.close();
            //System.out.println(root.getName());
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private String readFile(String filename) {
        InputStream in = null;
        in = this.getClass().getResourceAsStream("code/" + filename);
        String content = "";
        try {
            content = new String(readStream(in));
        } catch (Exception e) {
        }
        return content;
    }

    private void writetoFile(String content, String filepath, String filename) {
        try {
            File floder = new File(filepath);
            // if file doesnt exists, then create it
            if (!floder.exists()) {
                floder.mkdirs();
            }
            File file = new File(filepath + "/" + filename);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
                System.out.println(new String(buffer));
            }

        } catch (IOException e) {
        } finally {
            outSteam.close();
            inStream.close();
        }
        return outSteam.toByteArray();
    }

}
