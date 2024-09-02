package cn.dairo.mysql.model.tool.code.tool;

import java.util.List;

import cn.dairo.mysql.model.tool.cls.bean.ClassBean;
import cn.dairo.mysql.model.tool.cls.bean.ClassParamBean;
import cn.dairo.mysql.model.tool.cls.bean.MethodBean;

public class MakeStr {
    /**
     * 生成Java类字符串
     *
     * @param cb
     * @return
     */
    public static String makeJavaStr(ClassBean cb) {
        StringBuilder sb = new StringBuilder();

        sb.append("package ").append(cb.getPkg()).append('\n');
        List<String> iList = cb.getImpoertList();

        // 导包
        if (iList != null && iList.size() > 0) {
            for (String im : iList) {
                sb.append("import ").append(im).append('\n');
            }
        }

        //添加注解
        if (cb.getAnnotations() != null) {
            for (String annotation : cb.getAnnotations()) {
                sb.append("@").append(annotation).append('\n');
            }
        }

        sb.append(cb.getType() == 0 ? " class " : " interface ").append(cb.getName());

        // 继承类
        if (cb.getExtend() != null) {
            sb.append(" : ").append(cb.getExtend());
        }

        // 继承接口
        if (cb.getImplement() != null) {
            sb.append(" : ").append(cb.getImplement());
        }
        sb.append("{").append('\n');

        List<ClassParamBean> pList = cb.getParams();

        // 成员变量
        if (pList != null && pList.size() > 0) {
            for (ClassParamBean p : pList) {

                // 成员变量备注
                if (p.getComment() != null) {
                    sb.append("\n/** \n* ").append(p.getComment()).append("\n*/ \n");
                }

                if (p.getAnnotation() != null) {
                    sb.append("@").append(p.getAnnotation()).append("\n");
                }

                // 默认私有变量
                if (p.getPermission() == null) {
                    sb.append("private");
                } else {
                    sb.append(p.getPermission());
                }

                sb.append(" ").append("var ").append(p.getName()).append(": ").append(p.getType()).append("?");

                if (p.getValue() != null) {
                    sb.append(" = ").append(p.getValue());
                } else {
                    sb.append(" = null");
                }
                sb.append('\n');
            }
        }

        // 成员方法
        List<MethodBean> mList = cb.getMethods();
        if (mList != null && mList.size() > 0) {
            for (MethodBean mb : mList) {

                // 默认私有变量
                if (mb.getPermission() == null) {
                    sb.append("private");
                } else {
                    sb.append(mb.getPermission());
                }
                sb.append(" ");

                // 是否静态
                if (mb.isStatic()) {
                    sb.append("static ");
                }

                // 返回值
                if (mb.getReturnType() == null) {
                    sb.append("void");
                } else {
                    sb.append(mb.getReturnType());
                }
                sb.append(" ");

                sb.append(mb.getName());
                sb.append("(");

                // 添加参数
                List<ClassParamBean> pbList = mb.getParams();
                if (pbList != null && pbList.size() > 0) {
                    for (ClassParamBean pb : pbList) {
                        sb.append(pb.getType()).append(" ").append(pb.getName()).append(",");
                    }

                    sb.setLength(sb.length() - 1);
                }
                sb.append(")");
                sb.append("{").append('\n');
                sb.append(mb.getContent());
                sb.append("}").append('\n');
            }
        }

        sb.append("}").append('\n');

        return sb.toString();
    }

    /**
     * 生成hibernate映射xml
     *
     * @param cb
     * @return
     */
//	public static String makeHibernateXml(ClassBean cb) {
//
//		// 创建根节点 并设置它的属性 ;
//		Element root = new Element("hibernate-mapping");
//
//		// 创建节点 class;
//		Element classE = new Element("class");
//
//		// 添加属性
//		classE.setAttribute("name", cb.getPkg() + "." + cb.getName());
//		classE.setAttribute("table", cb.getTbName());
//		root.addContent(classE);
//
//		List<String> names = new ArrayList<String>();
//		for (ParamBean p : cb.getParams()) {
//			names.add(p.getName());
//		}
//
//		boolean hasId = false;
//		for (String item : names) {
//
//			// 如果有id字段
//			if (item.toLowerCase().equals("id")) {
//				hasId = true;
//
//				// 创建根id节点
//				Element id = new Element("id").setAttribute("name", item).setAttribute("column", item);
//				Element generator = new Element("generator").setAttribute("class", "native");
//				id.addContent(generator);
//				classE.addContent(id);
//				names.remove(item);
//				break;
//			}
//		}
//
//		if (!hasId) {
//
//			// 没有id字段，取第一个字段作为id
//			Element id = new Element("id").setAttribute("name", names.get(0)).setAttribute("column", names.get(0));
//			Element generator = new Element("generator").setAttribute("class", "native");
//			id.addContent(generator);
//			classE.addContent(id);
//			names.remove(0);
//		}
//
//		for (String item : names) {
//
//			// 创建根节点 并设置它的属性 ;
//			Element property = new Element("property").setAttribute("name", item).setAttribute("column", item);
//			classE.addContent(property);
//		}
//
//		// 使xml文件 缩进效果
//		Format format = Format.getPrettyFormat();
//		XMLOutputter XMLOut = new XMLOutputter(format);
//
//		// 将根节点添加到文档中；
//		Document doc = new Document(root, new DocType("hibernate-mapping", "-//Hibernate/Hibernate Mapping DTD 3.0//EN",
//				"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd"));
//
//		ByteArrayOutputStream byteRsp = new ByteArrayOutputStream();
//		try {
//			XMLOut.output(doc, byteRsp);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		String rs = byteRsp.toString();
//		return rs;
//	}
}
