package site.duqian.spring.git_helper;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.springframework.util.CollectionUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import site.duqian.spring.utils.Md5Util;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class MethodParserUtils {

    /**
     * 解析类获取类的所有方法
     */
    public static List<MethodInfoResult> parseMethods(String classFile) {
        try {
            List<MethodInfoResult> list = new ArrayList<>();
            FileInputStream in = new FileInputStream(classFile);
            CompilationUnit cu = StaticJavaParser.parse(in);
            //由于jacoco不会统计接口覆盖率，没比较计算接口的方法，此处排除接口类
            final List<?> types = cu.getTypes();
            boolean isInterface = types.stream().filter(t -> t instanceof ClassOrInterfaceDeclaration).anyMatch(t -> ((ClassOrInterfaceDeclaration) t).isInterface());
            if (isInterface) {
                return list;
            }
            cu.accept(new MethodVisitor(), list);
            return list;
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * javaparser工具类核心方法，主要通过这个类遍历class文件的方法，此方法主要是获取出代码的所有方法，然后再去对比方法是否存在差异
     */
    private static class MethodVisitor extends VoidVisitorAdapter<List<MethodInfoResult>> {
        @Override
        public void visit(MethodDeclaration n, List<MethodInfoResult> list) {
            //删除注释
            n.removeComment();
            //计算方法体的hash值，疑问，空格，特殊转义字符会影响结果，导致相同匹配为差异？建议提交代码时统一工具格式化
            String md5 = Md5Util.string2MD5(n.toString());
            //参数处理
            StringBuilder params = new StringBuilder();
            NodeList<Parameter> parameters = n.getParameters();
            if (!CollectionUtils.isEmpty(parameters)) {
                for (int i = 0; i < parameters.size(); i++) {
                    String param = parameters.get(i).getType().toString();
                    params.append(param.replaceAll(" ", ""));
                    if (i != parameters.size() - 1) {
                        params.append("&");
                    }
                }
            }
            MethodInfoResult result = MethodInfoResult.builder()
                    .md5(md5)
                    .methodName(n.getNameAsString())
                    .parameters(params.toString())
                    .build();
            list.add(result);
            super.visit(n, list);
        }

    }
}
