package cn.dairo.mysql.model.tool.cls.bean;

import java.util.List;

public class ClassBean {

    /**
     * 0:类,1:接口
     */
    private int type;

    /**
     * 包名
     */
    private String pkg;
    /**
     * 类名
     */
    private String name;

    /**
     * 导入包
     */
    private List<String> impoertList;

    /**
     * 访问权限
     */
    private String permission;

    /**
     * 继承类
     */
    private String extend;

    /**
     * 实现接口
     */
    private String implement;

    /**
     * 成员变量
     */
    private List<ClassParamBean> params;

    /**
     * 函数列表
     */
    private List<MethodBean> methods;

    private List<String> annotations;

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<String> getImpoertList() {
        return impoertList;
    }

    public void setImpoertList(List<String> impoertList) {
        this.impoertList = impoertList;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }


    public String getImplement() {
        return implement;
    }

    public void setImplement(String implement) {
        this.implement = implement;
    }

    public List<ClassParamBean> getParams() {
        return params;
    }

    public void setParams(List<ClassParamBean> params) {
        this.params = params;
    }

    public List<MethodBean> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodBean> methods) {
        this.methods = methods;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }
}
