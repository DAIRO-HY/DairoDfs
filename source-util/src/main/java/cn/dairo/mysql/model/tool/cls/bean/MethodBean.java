package cn.dairo.mysql.model.tool.cls.bean;

import java.util.List;

/**
 * @author Long-PC 方法bean
 */
public class MethodBean {
    /**
     * 方法名
     */
    private String name;

    /**
     * 访问权限
     */
    private String permission;

    /**
     * 返回值类型
     */
    private String returnType;

    /**
     * 参数列表
     */
    private List<ClassParamBean> params;

    /**
     * 跑出异常类型
     */
    private String throwType;

    /**
     * 方法内容
     */
    private String content;

    /**
     * 是否静态
     */
    private boolean isStatic;

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }


    public List<ClassParamBean> getParams() {
        return params;
    }

    public void setParams(List<ClassParamBean> params) {
        this.params = params;
    }

    public String getThrowType() {
        return throwType;
    }

    public void setThrowType(String throwType) {
        this.throwType = throwType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
