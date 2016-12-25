package cn.mcavoy.www.subwayticket.Model;

public class UserModel {
    private String id;
    private String email;
    private String name;
    private String introduction;
    private String mobile;
    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    public String getIntroduction() {
        return introduction;
    }

    public String getMobile() {
        return mobile;
    }

    public String getName() {
        return name;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
