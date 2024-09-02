package cn.dairo.mysql.model.tool.cls.bean;

/**
 * @author Long-PC 参数bean
 */
public class ClassParamBean {
    /**
     * 参数名
     */
    private String name;

    /**
     * 参数类型名
     */
    private String type;

    /**
     * 访问权限
     */
    private String permission;

    /**
     * 默认值
     */
    private String value;

    /**
     * 备注
     */
    private String comment;

    /**
     * 是否主键
     */
    private boolean isPrimaryKey;

    /**
     * 注解
     */
    private String annotation;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }
}
